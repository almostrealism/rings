/*
 * Copyright 2019 Michael Murray
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
import java.util.Arrays;
import java.util.List;

import io.almostrealism.relation.Editable;
import org.almostrealism.geometry.DiscreteField;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.geometry.computations.RayDirection;
import org.almostrealism.geometry.computations.RayMatrixTransform;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.computations.ScalarPow;
import org.almostrealism.color.*;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.computations.RGBAdd;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.computations.RayOrigin;
import org.almostrealism.hardware.HardwareFeatures;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.texture.Texture;
import org.almostrealism.util.CodeFeatures;
import io.almostrealism.relation.Evaluable;
import com.almostrealism.raytrace.LightingEngineAggregator;

/**
 * A ReflectionShader object provides a shading method for reflective surfaces.
 * The ReflectionShader class uses a shading algorithm based on Shlick's
 * approximation to the Fresnel equations.
 * 
 * @author  Michael Murray
 */
public class ReflectionShader extends ShaderSet<ShaderContext> implements Shader<ShaderContext>, Editable, HardwareFeatures, CodeFeatures {
  public static int maxReflections = 4;
  
  private static final String propNames[] = {"Reflectivity", "Reflective Color",
  										"Blur factor", "Environment map"};
  private static final String propDesc[] = {"The reflectivity of the surface at a direct (normal) viewing angle, usually in the range [0,1].",
										"The base color of the reflection.", "Blur factor.",
										"Texture to use as an environment map."};
  private static final Class propTypes[] = {Double.class, ColorEvaluable.class, Double.class, Texture.class};
  
  private double reflectivity, blur;
  private Producer<RGB> reflectiveColor;
  private Texture eMap;

	/**
	 * Constructs a new ReflectionShader object with a reflectivity of 0.0
	 * and white as a reflective color.
	 */
	public ReflectionShader() {
		this.setReflectivity(0.0);
		this.setBlur(0.0);
		this.setReflectiveColor(RGBWhite.getInstance());
	}
	
	/**
	 * Constructs a new ReflectionShader object with the specified reflectivity
	 * and reflective color.
	 */
	public ReflectionShader(double reflectivity, Producer<RGB> reflectiveColor) {
		this.setReflectivity(reflectivity);
		this.setReflectiveColor(reflectiveColor);
		this.setBlur(0.0);
	}
	
	/** Method specified by the Shader interface. */
	@Override
	public Producer<RGB> shade(ShaderContext p, DiscreteField normals) {
		if (p.getReflectionCount() > ReflectionShader.maxReflections) {
			return new DynamicRGBProducer(args -> {
					Vector point = p.getIntersection().get(0).get().evaluate(args).getOrigin();
					return reflectiveColor.get().evaluate(new Object[] { p })
							.multiply(p.getSurface().getValueAt(v(point)).get().evaluate());
				});
		}
		
		p.addReflection();
		
		List<Curve<RGB>> allSurfaces = new ArrayList<>();
		allSurfaces.add(p.getSurface());
		for (int i = 0; i < p.getOtherSurfaces().length; i++) { allSurfaces.add(p.getOtherSurfaces()[i]); }
		
		List<Light> allLights = new ArrayList<>();
		allLights.add(p.getLight());
		for (Light l : p.getOtherLights()) { allLights.add(l); }

		Producer<RGB> r = getReflectiveColor();
		if (size() > 0) {
			r = cmultiply(r, ReflectionShader.super.shade(p, normals));
		}

		final Producer<RGB> fr = r;

		VectorProducer point = new RayOrigin(p.getIntersection().get(0));
		VectorProducer n = new RayDirection(normals.iterator().next());
		Producer<Vector> nor = p.getIntersection().getNormalAt(point);

		RayMatrixTransform transform = new RayMatrixTransform(((AbstractSurface) p.getSurface()).getTransform(true), p.getIntersection().get(0));
		VectorProducer loc = origin(transform);

		ScalarProducer cp = length(nor).multiply(length(n));

		Evaluable<RGB> tc = null;

		// TODO Should surface color be factored in to reflection?
//		RGB surfaceColor = p.getSurface().getColorAt(p.getPoint());

		f: if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			Producer<Ray> reflectedRay = new ReflectedRay(loc, nor, n, blur);

			// TODO  Environment map should be a feature of the aggregator
			Evaluable<RGB> color = new LightingEngineAggregator(reflectedRay, Arrays.asList(p.getOtherSurfaces()), allLights, p).getAccelerated();
			/*
			if (color == null || color.evaluate(args) == null) { // TODO  Avoid evaluation here
				if (eMap == null) {
					break f;
				} else {
					throw new RuntimeException("Not implemented");
					// TODO  Use AdaptProducer
					// color = eMap.getColorAt(null).evaluate(new Object[]{reflectedRay.evaluate(args).getDirection()});
				}
			}
			 */

			ScalarProducer c = v(1).subtract(minus(n).dotProduct(nor).divide(cp));
			ScalarProducer reflective = v(reflectivity).add(v(1 - reflectivity)
							.multiply(compileProducer(new ScalarPow(c, v(5.0)))));
			Evaluable<RGB> fcolor = color;
			color = cfromScalar(reflective).multiply(fr).multiply(() -> fcolor).get();

			if (tc == null) {
				tc = color;
			} else {
				Evaluable<RGB> ftc = tc;
				Evaluable<RGB> ffcolor = color;
				tc = new RGBAdd(() -> ftc, () -> ffcolor);
			}
		}

		b: if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			n = minus(n);

			Producer<Ray> reflectedRay = new ReflectedRay(loc, nor, n, blur);

			// TODO  Environment map should be a feature of the aggregator
			Evaluable<RGB> color = new LightingEngineAggregator(reflectedRay, allSurfaces, allLights, p);
			/*
			if (color == null) {
				if (eMap == null) {
					break b;
				} else {
					throw new RuntimeException("Not implemented");
					// TODO  Use AdaptProducer
					// color = eMap.getColorAt(null).evaluate(new Object[] { reflectedRay.evaluate(args).getDirection() });
				}
			}
			 */

			ScalarProducer c = v(1).subtract(minus(n).dotProduct(nor).divide(cp));
			ScalarProducer reflective = v(reflectivity).add(
					v(1 - reflectivity).multiply(pow(c, v(5.0))));
			Evaluable<RGB> fcolor = color;
			color = cmultiply(() -> fcolor, cmultiply(fr, cfromScalar(reflective))).get();

			if (tc == null) {
				tc = color;
			} else {
				tc = cadd(tc, color);
			}
		}

		Producer<RGB> lightColor = p.getLight().getColorAt(point);
		Evaluable<RGB> ftc = tc;
		return GeneratedColorProducer.fromProducer(this, cmultiply(() -> ftc, lightColor));
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
	public void setReflectiveColor(Producer<RGB> color) { this.reflectiveColor = color; }
	
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
	 * Returns the reflective color used by this {@link ReflectionShader}
	 * as an {@link RGBEvaluable}.
	 */
	public Producer<RGB> getReflectiveColor() { return this.reflectiveColor; }
	
	/**
	 * @return  The Texture object used as an environment map for this ReflectionShader object.
	 */
	public Texture getEnvironmentMap() { return this.eMap; }
	
	/**
	 * Returns an array of String objects with names for each editable property of this ReflectionShader object.
	 */
	@Override
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
			if (value instanceof Producer)
				this.setReflectiveColor((Producer) value);
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