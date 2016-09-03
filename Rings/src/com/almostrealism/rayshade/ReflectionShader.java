/*
 * Copyright 2016 Michael Murray
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

import java.util.ArrayList;
import java.util.List;

import org.almostrealism.color.ColorMultiplier;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Ray;
import org.almostrealism.space.Vector;
import org.almostrealism.texture.Texture;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

import com.almostrealism.lighting.Light;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.ShadableSurface;

/**
 * A ReflectionShader object provides a shading method for reflective surfaces.
 * The ReflectionShader class uses a shading algorithm based on Shlick's
 * approximation to the Fresnel equations.
 * 
 * @author Mike Murray
 */
public class ReflectionShader extends ShaderSet implements Shader, Editable {
  public static int maxReflections = 4;
  
  private static final String propNames[] = {"Reflectivity", "Reflective Color",
  										"Blur factor", "Environment map"};
  private static final String propDesc[] = {"The reflectivity of the surface at a direct (normal) viewing angle, usually in the range [0,1].",
										"The base color of the reflection.", "Blur factor.",
										"Texture to use as an environment map."};
  private static final Class propTypes[] = {Double.class, ColorProducer.class, Double.class, Texture.class};
  
  private double reflectivity, blur;
  private ColorProducer reflectiveColor;
  private Texture eMap;

	/**
	 * Constructs a new ReflectionShader object with a reflectivity of 0.0
	 * and white as a reflective color.
	 */
	public ReflectionShader() {
		this.setReflectivity(0.0);
		this.setBlur(0.0);
		this.setReflectiveColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs a new ReflectionShader object with the specified reflectivity
	 * and reflective color.
	 */
	public ReflectionShader(double reflectivity, ColorProducer reflectiveColor) {
		this.setReflectivity(reflectivity);
		this.setReflectiveColor(reflectiveColor);
		this.setBlur(0.0);
	}
	
	/**
	 * Method specified by the Shader interface.
	 */
	public ColorProducer shade(ShaderParameters p) {
		if (p.getReflectionCount() > ReflectionShader.maxReflections)
			return this.reflectiveColor.evaluate(new Object[] {p})
					.multiply(p.getSurface().getColorAt(p.getPoint()).evaluate(null));
		
		p.addReflection();
		
		List<ShadableSurface> allSurfaces = new ArrayList<ShadableSurface>();
		for (int i = 0; i < p.getOtherSurfaces().length; i++) { allSurfaces.add(p.getOtherSurfaces()[i]); }
		allSurfaces.add(p.getSurface());
		
		Light allLights[] = new Light[p.getOtherLights().length + 1];
		for (int i = 0; i < p.getOtherLights().length; i++) { allLights[i] = p.getOtherLights()[i]; }
		allLights[allLights.length - 1] = p.getLight();
		
		RGB lightColor = p.getLight().getColorAt(p.getPoint()).evaluate(null);
		
		Vector n = p.getSurface().getNormalAt(p.getPoint());
		// TODO Should surface color be factored in to reflection?
//		RGB surfaceColor = p.getSurface().getColorAt(p.getPoint());
		
		ColorSum totalColor = new ColorSum();
		
		ColorProducer r = this.getReflectiveColor();
		if (super.size() > 0) r = new ColorMultiplier(r, super.shade(p));
		
		f: if (p.getSurface().getShadeFront()) {
			Vector ref = RayTracingEngine.reflect(p.getViewerDirection(), n);
			
			if (this.blur != 0.0) {
				double a = this.blur * (-0.5 + Math.random());
				double b = this.blur * (-0.5 + Math.random());
				
				Vector u, v, w = (Vector) n.clone();
				
				Vector t = (Vector)n.clone();
				
				if (t.getX() < t.getY() && t.getY() < t.getZ()) {
					t.setX(1.0);
				} else if (t.getY() < t.getX() && t.getY() < t.getZ()) {
					t.setY(1.0);
				} else {
					t.setZ(1.0);
				}
				
				w.divideBy(w.length());
				
				u = t.crossProduct(w);
				u.divideBy(u.length());
				
				v = w.crossProduct(u);
				
				ref.addTo(u.multiply(a));
				ref.addTo(v.multiply(b));
				ref.divideBy(ref.length());
			}
			
			Ray reflectedRay = new Ray(p.getPoint(), ref);
			
			ColorProducer color = RayTracingEngine.lightingCalculation(reflectedRay, allSurfaces, allLights,
														p.fogColor, p.fogDensity, p.fogRatio, p);
			
			if (color == null) {
				if (this.eMap == null)
					break f;
				else
					color = this.eMap.getColorAt(ref);
			}
			
			double c = 1 - p.getViewerDirection().minus().dotProduct(n) / (p.getViewerDirection().minus().length() * n.length());
			double reflectivity = this.reflectivity + (1 - this.reflectivity) * Math.pow(c, 5.0);
			color = new ColorMultiplier(color, new ColorMultiplier(r, reflectivity));
			
			totalColor.add(color);
		}
		
		b: if (p.getSurface().getShadeBack()) {
			n = n.minus();
			
			Vector ref = RayTracingEngine.reflect(p.getViewerDirection(), n);
			
			if (this.blur != 0.0) {
				double a = this.blur * (-0.5 + Math.random());
				double b = this.blur * (-0.5 + Math.random());
				
				Vector u, v, w = (Vector)n.clone();
				
				Vector t = (Vector)n.clone();
				
				if (t.getX() < t.getY() && t.getY() < t.getZ()) {
					t.setX(1.0);
				} else if (t.getY() < t.getX() && t.getY() < t.getZ()) {
					t.setY(1.0);
				} else {
					t.setZ(1.0);
				}
				
				w.divideBy(w.length());
				
				u = t.crossProduct(w);
				u.divideBy(u.length());
				
				v = w.crossProduct(u);
				
				ref.addTo(u.multiply(a));
				ref.addTo(v.multiply(b));
				ref.divideBy(ref.length());
			}
			
			Ray reflectedRay = new Ray(p.getPoint(), ref);
			
			ColorProducer color = RayTracingEngine.lightingCalculation(reflectedRay, allSurfaces, allLights, 
																	p.fogColor, p.fogDensity, p.fogRatio, p);
			
			if (color == null) {
				if (this.eMap == null)
					break b;
				else
					color = this.eMap.getColorAt(ref);
			}
			
			double c = 1 - p.getViewerDirection().minus().dotProduct(n) / (p.getViewerDirection().minus().length() * n.length());
			double reflectivity = this.reflectivity + (1 - this.reflectivity) * Math.pow(c, 5.0);
			color = new ColorMultiplier(color, new ColorMultiplier(r, reflectivity));
			
			totalColor.add(color);
		}
		
		return new ColorMultiplier(totalColor, lightColor);
	}
	
	/**
	 * Sets the reflectivity value used by this ReflectionShader object.
	 */
	public void setReflectivity(double reflectivity) { this.reflectivity = reflectivity; }
	
	/**
	 * Sets the blur factor used by this ReflectionShader object.
	 * 
	 * @param blur  Blur factor to use.
	 */
	public void setBlur(double blur) { this.blur = blur; }
	
	/**
	 * Sets the reflective color used by this ReflectionShader object
	 * to the color represented by the specified ColorProducer object.
	 */
	public void setReflectiveColor(ColorProducer color) { this.reflectiveColor = color; }
	
	/**
	 * Sets the Texture object used as an environment map for this ReflectionShader object.
	 * 
	 * @param map  The Texture object to use.
	 */
	public void setEnvironmentMap(Texture map) { this.eMap = map; }
	
	/**
	 * @return  The reflectivity value used by this ReflectionShader object.
	 */
	public double getReflectivity() { return this.reflectivity; }
	
	/**
	 * @return  The blur factor used by this ReflectionShader object.
	 */
	public double getBlur() { return this.blur; }
	
	/**
	 * Returns the reflective color used by this ReflectionShader object
	 * as ColorProducer object.
	 */
	public ColorProducer getReflectiveColor() { return this.reflectiveColor; }
	
	/**
	 * @return  The Texture object used as an environment map for this ReflectionShader object.
	 */
	public Texture getEnvironmentMap() { return this.eMap; }
	
	/**
	 * Returns an array of String objects with names for each editable property of this ReflectionShader object.
	 */
	public String[] getPropertyNames() { return ReflectionShader.propNames; }
	
	/**
	 * Returns an array of String objects with descriptions for each editable property of this ReflectionShader object.
	 */
	public String[] getPropertyDescriptions() { return ReflectionShader.propDesc; }
	
	/**
	 * Returns an array of Class objects representing the class types of each editable property of this ReflectionShader object.
	 */
	public Class[] getPropertyTypes() { return ReflectionShader.propTypes; }
	
	/**
	 * Returns the values of the properties of this ReflectionShader object as an Object array.
	 */
	public Object[] getPropertyValues() {
		return new Object[] {new Double(this.reflectivity), this.reflectiveColor, new Double(this.blur), this.eMap};
	}
	
	/**
	 * Sets the value of the property of this ReflectionShader object at the specified index to the specified value.
	 * 
	 * @throws IllegalArgumentException  If the object specified is not of the correct type.
	 * @throws IndexOutOfBoundsException  If the index specified does not correspond to an editable property of this 
	 *                                    ReflectionShader object.
	 */
	public void setPropertyValue(Object value, int index) {
		if (index == 0) {
			if (value instanceof Double)
				this.setReflectivity(((Double)value).doubleValue());
			else
				throw new IllegalArgumentException("Illegal argument: " + value);
		} else if (index == 1) {
			if (value instanceof ColorProducer)
				this.setReflectiveColor((ColorProducer)value);
			else
				throw new IllegalArgumentException("Illegal argument: " + value);
		} else if (index == 2) {
			if (value instanceof Double)
				this.setBlur(((Double)value).doubleValue());
			else
				throw new IllegalArgumentException("Illegal argument: " + value);
		} else if (index == 3) {
			if (value instanceof Texture)
				this.setEnvironmentMap((Texture)value);
			else
				throw new IllegalArgumentException("Illegal argument: " + value);
		} else {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
	}
	
	/**
	 * Sets the values of editable properties of this ReflectionShader object to those specified.
	 * 
	 * @throws IllegalArgumentException  If one of the objects specified is not of the correct type.
     *                                       (Note: none of the values after the erroneous value will be set)
	 * @throws IndexOutOfBoundsException  If the length of the specified array is longer than permitted.
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++)
			this.setPropertyValue(values[i], i);
	}
	
	/**
	 * @return  {reflective color}.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] {this.reflectiveColor}; }
	
	/**
	 * Sets the values of properties of this HighlightShader object to those specified.
	 * 
	 * @throws IllegalArgumentException  If the Producer object specified is not of the correct type.
	 * @throws IndexOutOfBoundsException  If the lindex != 0;
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.setPropertyValue(p, 1);
		else
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}
	
	/**
	 * Returns "Reflection Shader".
	 */
	public String toString() { return "Reflection Shader"; }
}
