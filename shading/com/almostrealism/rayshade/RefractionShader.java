/*
 * Copyright 2018 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.rayshade;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.almostrealism.IntersectionalLightingEngine;
import com.almostrealism.LightingEngine;
import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.Intersection;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.*;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

// TODO  Fix refraction algorithm.

/**
 * A {@link RefractionShader} provides a shading method for dielectric surfaces.
 * 
 * @author  Michael Murray
 */
public class RefractionShader implements Shader<ShaderContext>, Editable {
	public static Vector lastRay;
	
	public static boolean produceOutput = false;
  
	private static final String propNames[] = {"Index of refraction", "Red attenuation", "Green attenuation", "Blue attenuation"};
	private static final String propDesc[] = {"The index of refraction of the medium",
						"The attenuation factor for the red channel",
						"The attenuation factor for the green channel",
						"The attenuation factor for the blue channel"};
	private static final Class propTypes[] = {Double.class, Double.class, Double.class, Double.class};
  
	private double indexOfRefraction;
	private double ra, ga, ba;
	private double sampleDistance = 0.01;
	private int sampleCount = 1;
	private double lra, lga, lba;
  
	private int entered, exited;

	/** Constructs a new RefractionShader object. */
	public RefractionShader() {}
	
	/** Method specified by the Shader interface. */
	public ColorProducer shade(ShaderContext p, DiscreteField normals) {
		p.addReflection();
		
		ColorSum color = new ColorSum();

		Vector po;
		try {
			po = p.getIntersection().get(0).call().getOrigin();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		if (Math.random() < 0.01 &&
				po.getX() * po.getX() + po.getY() * po.getY() + po.getZ() * po.getZ() - 1.0 > 0.01)
			System.out.println(po);
		
		Vector n;
		
		try {
			n = normals.iterator().next().call().getDirection();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		n = n.divide(n.length());
		
		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			Vector point;
			
			try {
				point = p.getIntersection().get(0).call().getOrigin();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			ColorProducer c = this.shade(point, p.getIntersection().getNormalAt(point),
										p.getLightDirection(), p.getLight(), p.getOtherLights(), p.getSurface(),
										p.getOtherSurfaces(), n, p);
			if (c != null) color.add(c);
		}
		
		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			Vector point;
			
			try {
				point = p.getIntersection().get(0).call().getOrigin();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			
			ColorProducer c = this.shade(point, p.getIntersection().getNormalAt(point),
										p.getLightDirection(), p.getLight(), p.getOtherLights(), p.getSurface(),
										p.getOtherSurfaces(), n.minus(), p);
			if (c != null) color.add(c);
		}
		
		return GeneratedColorProducer.fromFunction(this, color);
	}
	
	protected ColorProducer shade(Vector point, Vector viewerDirection, Vector lightDirection,
								Light light, Light otherLights[], Callable<ColorProducer> surface,
								Callable<ColorProducer> otherSurfaces[], Vector n, ShaderContext p) {
		if (p.getReflectionCount() > ReflectionShader.maxReflections) {
			lastRay = null;
			return new RGB(0.0, 0.0, 0.0);
		}
		
		boolean entering = this.checkEntering(viewerDirection, n);
		double currentR = 0.0, nextR = 0.0;
		
		if (entering)
			p.addEntrance();
		else
			p.addExit();
		
		if (surface instanceof AbstractSurface) {
			currentR =
				this.sampleDensity((AbstractSurface) surface, point, n,
						this.sampleDistance, this.sampleCount, !entering);
			nextR =
				this.sampleDensity((AbstractSurface) surface, point, n,
						this.sampleDistance, this.sampleCount, entering);
		} else if (entering) {
			currentR = 1.0;
			nextR = this.indexOfRefraction;
		} else {
			currentR = this.indexOfRefraction;
			nextR = 1.0;
		}
		
//		if (Math.random() < 0.0001)
//			System.out.println(viewerDirection.length() +
//								" " + n.length() + " " + 
//								currentR + " " + nextR);
		
		// if (!entering) n = n.minus();
		
		Vector dv = viewerDirection;
		dv = dv.minus();
		
		Vector d = LightingEngine.refract(dv, n, currentR, nextR, (Math.random() < 0.0000));
		d.divideBy(d.length());
		
		// if (d.dotProduct(dv) > 0) d.multiplyBy(-1.0);
		
		// d = dv.minus();
		
		// if (entering) d.multiplyBy(-1.0);
		Ray r = new Ray(point, d);
		
		List<Callable<ColorProducer>> allSurfaces = Scene.combineSurfaces(surface, Arrays.asList(otherSurfaces));
		
		Light allLights[] = new Light[p.getOtherLights().length + 1];
		for (int i = 0; i < p.getOtherLights().length; i++) { allLights[i] = p.getOtherLights()[i]; }
		allLights[allLights.length - 1] = p.getLight();
		
//		if (Math.random() < 0.00001 && !entering) System.out.println(r.getDirection() + " " + lastRay);
		RefractionShader.lastRay = r.getDirection();

		IntersectionalLightingEngine l = new IntersectionalLightingEngine(allSurfaces);
		ColorProducer color = l.lightingCalculation(r, allSurfaces, allLights,
													p.fogColor, p.fogDensity, p.fogRatio, p);
		
//		if (color.equals(new RGB()) && Math.random() < 0.01) System.out.println(d.dotProduct(dv));
		
//		if (color == null) {
////			color = surface.getColorAt(point).multiply(new RGB(this.lra, this.lga, this.lba));
//		} else {
//			// if (!color.equals(new RGB()) && Math.random() < 0.001) System.out.println(color);
//			if (entering) color.multiplyBy(new RGB(this.ra, this.ga, this.ba));
//			
//		}
		
		return color;
	}
	
	public double sampleDensity(AbstractSurface s, Vector p, Vector n,
								double sd, int samples, boolean entering) {
		double totalR = 0.0;
		
		if (entering) {
			Vector v = n.multiply(-sd);
			v.addTo(p);
			totalR = s.getIndexOfRefraction(v);
		} else {
			Vector v = n.multiply(sd);
			v.addTo(p);
			totalR = s.getIndexOfRefraction(v);
		}
		
		for (int i = 1; i < samples; i++) {
			Vector d = Vector.uniformSphericalRandom();
			double dot = d.dotProduct(n);
			
			if ((entering && dot > 0.0) ||
					(!entering && dot < 0.0)) {
				d.multiplyBy(-1.0);
			}
			
			Ray r = new Ray(p, d);
			Intersection inter = s.intersectAt(r);
			
			if (inter == null || inter.getIntersections().length <= 0) {
				totalR += 1.0;
			} else {
				double id = inter.getClosestIntersection();
				totalR += s.getIndexOfRefraction(r.pointAt(id));
			}
		}
		
		if (samples > 1)
			return totalR / samples;
		else
			return totalR;
	}
	
	public boolean checkEntering(Vector d, Vector n) {
		double dot = d.dotProduct(n);
		
		if (dot > 0.0) {
			return true;
		} else {
			return false;
		}
	}
	
	public Vector refract(Vector n, Vector d, double rindex, double eindex) {
		Vector refracted;
		
		double ndoti, tndoti, ndoti2, a, b, b2, d2;
		
		ndoti = n.dotProduct(d);
		ndoti2 = ndoti * ndoti;
		
		// if (ndoti >= 0.0) {
		b = eindex / rindex; // } else { b = eindex / rindex; }
		b2 = b * b;
		
		d2 = 1.0 - b2 * (1.0 - ndoti2);
		
		if (d2 >= 0.0) {
			if (ndoti >= 0.0)
				a = b * ndoti - Math.sqrt(d2);
			else
				a = b * ndoti + Math.sqrt(d2);
			
			refracted = n.multiply(a).subtract(d.multiply(b));
		} else {
			tndoti = ndoti + ndoti;
			
			refracted = n.multiply(tndoti).subtract(d);
		}
		
		return refracted;
	}
	
	/**
	 * Sets the index of refraction value used by this RefractionShader object.
	 */
	public void setIndexOfRefraction(double n) { this.indexOfRefraction = n; }
	
	/**
	 * Sets the attenuation factors used by this RefractionShader object.
	 */
	public void setAttenuationFactors(double r, double g, double b) {
		this.ra = r;
		this.ga = g;
		this.ba = b;
		
		this.lra = Math.log(this.ra);
		this.lga = Math.log(this.ga);
		this.lba = Math.log(this.ba);
	}
	
	/**
	 * Returns the index of refraction value used by this RefractionShader object.
	 */
	public double getIndexOfRefraction() { return this.indexOfRefraction; }
	
	/**
	 * Returns the 3 attenuation factors (RGB) used by this RefractionShader object.
	 */
	public double[] getAttenuationFactors() {
		return new double[] {this.ra, this.ga, this.ba};
	}
	
	/**
	 * Returns an array of String objects with names for each editable property of this RefractionShader object.
	 */
	public String[] getPropertyNames() { return RefractionShader.propNames; }
	
	/**
	 * Returns an array of String objects with descriptions for each editable property of this RefractionShader object.
	 */
	public String[] getPropertyDescriptions() { return RefractionShader.propDesc; }
	
	/**
	 * Returns an array of Class objects representing the class types of each editable property of this RefractionShader object.
	 */
	public Class[] getPropertyTypes() { return RefractionShader.propTypes; }
	
	/**
	 * Returns the values of the properties of this ReflectionShader object as an Object array.
	 */
	public Object[] getPropertyValues() {
		return new Object[] {new Double(this.indexOfRefraction), new Double(this.ra), new Double(this.ga), new Double(this.ba)};
	}
	
	/**
	 * Sets the value of the property of this RefractionShader object at the specified index to the specified value.
	 * 
	 * @throws IllegalArgumentException  If the object specified is not of the correct type.
	 * @throws IndexOutOfBoundsException  If the index specified does not correspond to an editable property of this
	 *                                    RefractionShader object.
	 */
	public void setPropertyValue(Object value, int index) {
		if (value instanceof Double == false)
			throw new IllegalArgumentException("Illegal argument: " + value.toString());
		
		if (index == 0) {
				this.setIndexOfRefraction(((Double)value).doubleValue());
		} else if (index == 1) {
				this.setAttenuationFactors(((Double)value).doubleValue(),
								this.getAttenuationFactors()[1],
								this.getAttenuationFactors()[2]);
		} else if (index == 2) {
				this.setAttenuationFactors(this.getAttenuationFactors()[0],
								((Double)value).doubleValue(),
								this.getAttenuationFactors()[2]);
		} else if (index == 3) {
				this.setAttenuationFactors(this.getAttenuationFactors()[0],
								this.getAttenuationFactors()[1],
								((Double)value).doubleValue());
		} else {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
	}
	
	/**
	 * @return  An empty array.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[0]; }
	
	/**
	 * Does nothing.
	 */
	public void setInputPropertyValue(int index, Producer p) {}
	
	/**
	 * Sets the values of editable properties of this ReflectionShader object to those specified.
	 * 
	 * @throws IllegalArgumentException  If one of the objects specified is not of the correct type.
	 *                                   (Note: none of the values after the erroneous value will be set)
	 * @throws IndexOutOfBoundsException  If the length of the specified array is longer than permitted.
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++)
			this.setPropertyValue(values[i], i);
	}
	
	/**
	 * Returns "Refraction Shader".
	 */
	public String toString() { return "Refraction Shader"; }
}
