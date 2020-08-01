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
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.space.ShadableSurface;
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
		ScalarProducer scaleFront = n.dotProduct(StaticProducer.of(p.getLightDirection()));
		ScalarProducer scaleBack = n.scalarMultiply(-1.0).dotProduct(StaticProducer.of(p.getLightDirection()));

		Producer<RGB> pr = new Producer<RGB>() {
			@Override
			public RGB evaluate(Object[] args) {
				Vector po = point.evaluate(args);
				ColorProducer lightColor = p.getLight().getColorAt().operate(po);

				ColorSum color = new ColorSum();

				ColorProducer realized = null;

				if (p.getSurface() != null) {
					realized = p.getSurface().evaluate(new Object[] { po });
				}

				if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
					double scale = scaleFront.evaluate(args).getValue();
					if (scale > 0) {
						color.add((Future) new ColorProduct(lightColor, realized, new RGB(scale, scale, scale)));
					}
				}

				if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
					double scale = scaleBack.evaluate(args).getValue();
					if (scale > 0) {
						color.add((Future) new ColorProduct(lightColor, realized, new RGB(scale, scale, scale)));
					}
				}

				return color.evaluate(args);
			}

			@Override
			public void compact() {
				point.compact();
				n.compact();
				scaleFront.compact();
				scaleBack.compact();
			}
		};
		
		return GeneratedColorProducer.fromProducer(this, pr);
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
