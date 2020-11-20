/*
 * Copyright 2020 Michael Murray
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

import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.ScalarSupplier;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.VectorSupplier;
import org.almostrealism.algebra.computations.RayDirection;
import org.almostrealism.color.*;
import org.almostrealism.color.computations.ColorProducer;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.computations.RGBAdd;
import org.almostrealism.color.computations.RGBProducer;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.relation.Maker;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.util.DynamicProducer;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;
import org.almostrealism.util.Provider;

/**
 * A {@link HighlightShader} provides a shading method for highlights on surfaces.
 * The {@link HighlightShader} uses a phong shading algorithm.
 * 
 * @author  Michael Murray
 */
public class HighlightShader extends ShaderSet<ShaderContext> implements Shader<ShaderContext>, Editable, HardwareFeatures, CodeFeatures {
  private static final String propNames[] = {"Highlight Color", "Highlight Exponent"};
  private static final String propDesc[] = {"The base color for the highlight", "The exponent used to dampen the highlight (phong exponent)"};
  private static final Class propTypes[] = {ColorProducer.class, Double.class};
  
  private RGBProducer highlightColor;
  private double highlightExponent;

	/**
	 * Constructs a new HighlightShader object using white as a highlight color
	 * and 1.0 as a highlight exponent.
	 */
	public HighlightShader() {
		this.setHighlightColor(RGBWhite.getProducer());
		this.setHighlightExponent(1.0);
	}
	
	/**
	 * Constructs a new HighlightShader object using the specified highlight color
	 * and highlight exponent.
	 */
	public HighlightShader(ColorProducer color, double exponent) {
		this.setHighlightColor(color);
		this.setHighlightExponent(exponent);
	}
	
	/** Method specified by the Shader interface. */
	@Override
	public Maker<RGB> shade(ShaderContext p, DiscreteField normals) {
		Vector point;
		
		try {
			point = p.getIntersection().get(0).evaluate().getOrigin();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		RGB lightColor = p.getLight().getColorAt(v(p.getIntersection().getNormalAt(v(point).get()).evaluate())).get().evaluate();
		
		Producer<Vector> n;
		
		try {
			n = compileProducer(new RayDirection(() -> normals.iterator().next()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		n = scalarMultiply(n, length(n).pow(-1.0));
		VectorSupplier h = add(() -> p.getIntersection().getNormalAt(v(point).get()), p.getLightDirection());
		h = h.scalarMultiply(h.length().pow(-1.0));

		Maker<RGB> hc = v(this.getHighlightColor().evaluate(new Object[] {p}));
		if (super.size() > 0) hc = cmultiply(hc, super.shade(p, normals));

		ScalarSupplier cFront = h.dotProduct(n);
		ScalarSupplier cBack = h.dotProduct(scalarMultiply(n, -1.0));

		Maker<RGB> fhc = hc;

		return () -> GeneratedColorProducer.fromProducer(this, new DynamicProducer<>(args -> {
			Maker<RGB> color = null;

			f: if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
				double c = cFront.get().evaluate(args).getValue();
				if (c < 0) break f;
				c = Math.pow(c, this.getHighlightExponent());

				Maker<RGB> pr = v(lightColor).multiply(v(fhc.get().evaluate(args))).multiply(v(new RGB(c, c, c)));
				if (color == null) {
					color = pr;
				} else {
					RGBAdd sum = new RGBAdd(color, pr);
					color = () -> sum;
				}
			}

			f: if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
				double c = cBack.get().evaluate(args).getValue();
				if (c < 0) break f;
				c = Math.pow(c, this.getHighlightExponent());

				Maker<RGB> pr = v(lightColor).multiply(v(fhc.get().evaluate(args))).multiply(v(new RGB(c, c, c)));
				if (color == null) {
					color = pr;
				} else {
					RGBAdd sum = new RGBAdd(color, pr);
					color = () -> sum;
				}
			}

			return color.get().evaluate();
		}));
	}
	
	/**
	 * Sets the color used for the highlight shaded by this HighlightShader object
	 * to the color represented by the specifed RGB object.
	 */
	public void setHighlightColor(RGBProducer color) { this.highlightColor = color; }
	
	/**
	 * Sets the highlight exponent (phong exponent) used by this {@link HighlightShader}.
	 */
	public void setHighlightExponent(double exp) { this.highlightExponent = exp; }
	
	/**
	 * Returns the color used for the highlight shaded by this {@link HighlightShader}
	 * as an {@link RGBProducer}.
	 */
	public RGBProducer getHighlightColor() { return this.highlightColor; }
	
	/**
	 * Returns the highlight exponent (phong exponent) used by this HighlightShader object.
	 */
	public double getHighlightExponent() { return this.highlightExponent; }
	
	/**
	 * Returns an array of String objects with names for each editable property of this HighlightShader object.
	 */
	public String[] getPropertyNames() { return HighlightShader.propNames; }
	
	/**
	 * Returns an array of String objects with descriptions for each editable property of this HighlightShader object.
	 */
	public String[] getPropertyDescriptions() { return HighlightShader.propDesc; }
	
	/**
	 * Returns an array of Class objects representing the class types of each editable property of this HighlightShader object.
	 */
	public Class[] getPropertyTypes() { return HighlightShader.propTypes; }
	
	/**
	 * Returns the values of the properties of this HighlightShader object as an Object array.
	 */
	public Object[] getPropertyValues() {
		return new Object[] {this.highlightColor, new Double(this.highlightExponent)};
	}
	
	/**
	 * Sets the value of the property of this HighlightShader object at the specified index to the specified value.
	 * 
	 * @throws IllegalArgumentException  If the object specified is not of the correct type.
	 * @throws IndexOutOfBoundsException  If the index specified does not correspond to an editable property
	 *                                    of this HighlightShader object.
	 */
	public void setPropertyValue(Object value, int index) {
		if (index == 0) {
			if (value instanceof ColorProducer)
				this.setHighlightColor((ColorProducer)value);
			else
				throw new IllegalArgumentException("Illegal argument: " + value.toString());
		} else if (index == 1) {
			if (value instanceof Double)
				this.setHighlightExponent(((Double)value).doubleValue());
			else
				throw new IllegalArgumentException("Illegal argument: " + value.toString());
		} else {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
	}
	
	/**
	 * Sets the values of properties of this HighlightShader object to those specified.
	 * 
	 * @throws IllegalArgumentException  If one of the objects specified is not of the correct type.
	 *                                   (Note: none of the values after the erroneous value will be set)
	 * @throws IndexOutOfBoundsException  If the length of the specified array is longer than permitted.
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++) {
			this.setPropertyValue(values[i], i);
		}
	}
	
	/**
	 * @return  {highlight color}.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] {this.highlightColor}; }
	
	/**
	 * Sets the values of properties of this HighlightShader object to those specified.
	 * 
	 * @throws IllegalArgumentException  If the Producer object specified is not of the correct type.
	 * @throws IndexOutOfBoundsException  If the lindex != 0;
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.setPropertyValue(p, 0);
		else
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}
	
	/**
	 * Returns "Highlight Shader".
	 */
	public String toString() { return "Highlight Shader"; }
}
