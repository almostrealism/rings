/*
 * Copyright 2025 Michael Murray
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

import io.almostrealism.relation.Editable;
import org.almostrealism.algebra.Vector;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGBFeatures;
import org.almostrealism.color.computations.GreaterThanRGB;
import org.almostrealism.geometry.DiscreteField;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.CodeFeatures;

/**
 * A {@link DiffuseShader} provides a shading method for diffuse (matte) surfaces using
 * the Lambertian reflectance model.
 *
 * <p>The Lambertian model is the simplest physically-based shading model, representing
 * surfaces that scatter light equally in all directions. The intensity of reflected light
 * is proportional to the cosine of the angle between the surface normal and the light
 * direction (Lambert's cosine law).</p>
 *
 * <p>Shading calculation:</p>
 * <ol>
 *   <li>Extract surface normal from the intersection (via DiscreteField)</li>
 *   <li>Compute dot product between normal and light direction</li>
 *   <li>Multiply surface color by light color by the dot product</li>
 *   <li>Support both front-facing and back-facing shading if enabled on the surface</li>
 *   <li>Return black if the surface faces away from the light</li>
 * </ol>
 *
 * <p><b>Note:</b> This shader works with {@link org.almostrealism.geometry.ShadableIntersection}
 * from ar-common, which provides the surface normal via the {@code getNormalAt} method
 * (exposed through the Gradient interface).</p>
 *
 * @author Michael Murray
 * @see org.almostrealism.color.Shader
 * @see org.almostrealism.geometry.ShadableIntersection
 */
public class DiffuseShader implements Shader<ShaderContext>, Editable, RGBFeatures, CodeFeatures {
	public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
	public static boolean produceOutput = false;

	/** Constructs a new {@link DiffuseShader}. */
	public DiffuseShader() { }
	
	/** Method specified by the {@link Shader} interface. */
	@Override
	public Producer<RGB> shade(ShaderContext p, DiscreteField normals) {
		if (produceOutput) {
			System.out.println("DiffuseShader.shade() called");
			System.out.println("  Surface: " + p.getSurface());
			System.out.println("  Light: " + p.getLight());
			System.out.println("  normals field: " + normals);
			System.out.println("  normals.get(0): " + normals.get(0));
		}

		CollectionProducer<Vector> point = origin(normals.get(0));
		CollectionProducer<Vector> n = normalize(direction(normals.get(0)));
		CollectionProducer<PackedCollection<?>> scaleFront = dotProduct(n, p.getLightDirection());
		CollectionProducer<PackedCollection<?>> scaleBack = dotProduct(minus(n), p.getLightDirection());
		Producer<RGB> lightColor = p.getLight().getColorAt(point);
		Producer<RGB> surfaceColor = p.getSurface().getValueAt(point);

		if (produceOutput) {
			System.out.println("  point producer: " + point);
			System.out.println("  normal producer: " + n);
			System.out.println("  scaleFront producer: " + scaleFront);
			System.out.println("  lightColor producer: " + lightColor);
			System.out.println("  surfaceColor producer: " + surfaceColor);
		}

		Producer<RGB> front = null, back = null;

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			Producer<RGB> color = multiply(surfaceColor, lightColor).multiply(cfromScalar(scaleFront));
			front = new GreaterThanRGB(scaleFront, scalar(0),
							color, black());
			if (produceOutput) {
				System.out.println("  front color producer created: " + front);
			}
		}

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			Producer<RGB> color = multiply(surfaceColor, lightColor).multiply(cfromScalar(scaleBack));
			back = new GreaterThanRGB(scaleBack, scalar(0),
							color, black());
			if (produceOutput) {
				System.out.println("  back color producer created: " + back);
			}
		}

		Producer<RGB> result;
		if (front != null && back != null) {
			result = GeneratedColorProducer.fromProducer(this, add(front, back));
		} else if (front != null) {
			result = GeneratedColorProducer.fromProducer(this, front);
		} else if (back != null) {
			result = GeneratedColorProducer.fromProducer(this, back);
		} else {
			result = GeneratedColorProducer.fromProducer(this, RGB.blank());
		}

		if (produceOutput) {
			System.out.println("  final result producer: " + result);
		}

		return result;
	}
	
	/** Returns a zero length array. */
	@Override
	public String[] getPropertyNames() { return new String[0]; }
	
	/** Returns a zero length array. */
	@Override
	public String[] getPropertyDescriptions() { return new String[0]; }
	
	/** Returns a zero length array. */
	@Override
	public Class[] getPropertyTypes() { return new Class[0]; }
	
	/** Returns a zero length array. */
	@Override
	public Object[] getPropertyValues() { return new Object[0]; }
	
	/** @throws IndexOutOfBoundsException */
	@Override
	public void setPropertyValue(Object value, int index) {
		throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void setPropertyValues(Object values[]) {}
	
	/**
	 * @return  An empty array.
	 */
	@Override
	public Producer[] getInputPropertyValues() { return new Producer[0]; }
	
	/** Does nothing. */
	@Override
	public void setInputPropertyValue(int index, Producer p) {}
	
	/** Returns "Diffuse Shader". */
	@Override
	public String toString() { return "Diffuse Shader"; }
}
