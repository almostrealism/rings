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

import io.almostrealism.relation.Editable;
import org.almostrealism.color.computations.GreaterThanRGB;
import org.almostrealism.geometry.DiscreteField;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.RGBBlack;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import io.almostrealism.relation.Producer;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.CodeFeatures;

/**
 * A {@link DiffuseShader} provides a shading method for diffuse surfaces.
 * The {@link DiffuseShader} class uses a lambertian shading algorithm.
 * 
 * @author Michael Murray
 */
public class DiffuseShader implements Shader<ShaderContext>, Editable, CodeFeatures {
	public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
	public static boolean produceOutput = false;

	/** Constructs a new {@link DiffuseShader}. */
	public DiffuseShader() { }
	
	/** Method specified by the {@link Shader} interface. */
	@Override
	public Producer<RGB> shade(ShaderContext p, DiscreteField normals) {
		VectorProducer point = origin(normals.get(0));
		VectorProducer n = direction(normals.get(0)).normalize();
		ScalarProducer scaleFront = n.dotProduct(p.getLightDirection());
		ScalarProducer scaleBack = n.scalarMultiply(-1.0).dotProduct(p.getLightDirection());
		Producer<RGB> lightColor = p.getLight().getColorAt(point);
		Producer<RGB> surfaceColor = p.getSurface().getValueAt(point);

		Producer<RGB> front = null, back = null;

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			front = new GreaterThanRGB(scaleFront, scalar(0),
							cfromScalar(scaleFront).multiply(lightColor).multiply(surfaceColor),
							RGBBlack.getInstance());
		}

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			back = new GreaterThanRGB(scaleBack, scalar(0),
							cfromScalar(scaleBack).multiply(lightColor).multiply(surfaceColor),
							RGBBlack.getInstance());
		}

		if (front != null && back != null) {
			return GeneratedColorProducer.fromProducer(this, cadd(front, back));
		} else if (front != null) {
			return GeneratedColorProducer.fromProducer(this, front);
		} else if (back != null) {
			return GeneratedColorProducer.fromProducer(this, back);
		} else {
			return GeneratedColorProducer.fromProducer(this, RGB.blank());
		}
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
