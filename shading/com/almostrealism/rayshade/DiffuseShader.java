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
import org.almostrealism.algebra.ScalarSupplier;
import org.almostrealism.algebra.VectorSupplier;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.RGBBlack;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.math.bool.GreaterThanRGB;
import org.almostrealism.relation.Maker;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Evaluable;

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
	public Maker<RGB> shade(ShaderContext p, DiscreteField normals) {
		VectorSupplier point = origin(() -> normals.get(0));
		VectorSupplier n = direction(() -> normals.get(0)).normalize();
		ScalarSupplier scaleFront = n.dotProduct(p.getLightDirection());
		ScalarSupplier scaleBack = n.scalarMultiply(-1.0).dotProduct(p.getLightDirection());
		Maker<RGB> lightColor = p.getLight().getColorAt(point);
		Maker<RGB> surfaceColor = () -> p.getSurface().getValueAt(point.get());

		Evaluable<RGB> front = null, back = null;

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

		Evaluable<RGB> ffront = front;
		Evaluable<RGB> fback = back;

		if (front != null && back != null) {
			return () -> GeneratedColorProducer.fromProducer(this, cadd(ffront, fback));
		} else if (front != null) {
			return () -> GeneratedColorProducer.fromProducer(this, ffront);
		} else if (back != null) {
			return () -> GeneratedColorProducer.fromProducer(this, fback);
		} else {
			return () -> GeneratedColorProducer.fromProducer(this, RGB.blank());
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
	public Evaluable[] getInputPropertyValues() { return new Evaluable[0]; }
	
	/** Does nothing. */
	@Override
	public void setInputPropertyValue(int index, Evaluable p) {}
	
	/** Returns "Diffuse Shader". */
	@Override
	public String toString() { return "Diffuse Shader"; }
}
