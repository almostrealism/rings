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

import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.VectorProducer;
import org.almostrealism.algebra.computations.RayDirection;
import org.almostrealism.algebra.computations.RayOrigin;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.GeneratedColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBProducer;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.math.bool.GreaterThan;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.AdaptProducer;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

import java.util.concurrent.Future;

/**
 * A {@link DiffuseShader} provides a shading method for diffuse surfaces.
 * The {@link DiffuseShader} class uses a lambertian shading algorithm.
 * 
 * @author Michael Murray
 */
public class DiffuseShader implements Shader<ShaderContext>, Editable {
	public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
	public static boolean produceOutput = false;

	/** Constructs a new {@link DiffuseShader}. */
	public DiffuseShader() { }
	
	/** Method specified by the {@link Shader} interface. */
	@Override
	public Producer<RGB> shade(ShaderContext p, DiscreteField normals) {
		VectorProducer point = new RayOrigin(normals.get(0));
		VectorProducer n = new RayDirection(normals.get(0)).normalize();
		ScalarProducer scaleFront = n.dotProduct(p.getLightDirection());
		ScalarProducer scaleBack = n.scalarMultiply(-1.0).dotProduct(p.getLightDirection());
		Producer<RGB> lightColor = p.getLight().getColorAt(point);

		Producer<RGB> front = null, back = null;

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			front = new GreaterThan<>(3, RGB.blank(), scaleFront, StaticProducer.of(0),
											new ColorProduct(lightColor, p.getSurface().getValueAt(point), RGBProducer.fromScalar(scaleFront)),
											StaticProducer.of(new RGB(0.0, 0.0, 0.0)));
		}

		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			back = new GreaterThan<>(3, RGB.blank(), scaleBack, StaticProducer.of(0),
					new ColorProduct(lightColor, p.getSurface().getValueAt(point), RGBProducer.fromScalar(scaleBack)),
					StaticProducer.of(new RGB(0.0, 0.0, 0.0)));
		}

		if (front != null && back != null) {
			return GeneratedColorProducer.fromProducer(this, new ColorSum(front, back));
		} else if (front != null) {
			return GeneratedColorProducer.fromProducer(this, new ColorSum(front));
		} else if (back != null) {
			return GeneratedColorProducer.fromProducer(this, new ColorSum(back));
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
