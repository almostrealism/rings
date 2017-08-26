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

import java.util.concurrent.Future;

import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

/**
 * A {@link DiffuseShader} provides a shading method for diffuse surfaces.
 * The {@link DiffuseShader} class uses a lambertian shading algorithm.
 * 
 * @author Michael Murray
 */
public class DiffuseShader implements Shader, Editable {
  public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
  public static boolean produceOutput = false;

	/** Constructs a new {@link DiffuseShader}. */
	public DiffuseShader() { }
	
	/** Method specified by the {@link Shader} interface. */
	public ColorProducer shade(ShaderParameters p) {
		ColorProducer lightColor = p.getLight().getColorAt(p.getIntersection().getPoint());
		
		Vector n = p.getIntersection().getNormal();
		ColorProducer surfaceColor = p.getSurface().getColorAt(p.getIntersection().getPoint());
		
		ColorSum color = new ColorSum();
		
		if (p.getSurface().getShadeFront()) {
			double scale = n.dotProduct(p.getLightDirection());
			color.add((Future) new ColorProduct(lightColor, surfaceColor, new RGB(scale, scale, scale)));
		}
		
		if (p.getSurface().getShadeBack()) {
			double scale = n.minus().dotProduct(p.getLightDirection());
			color.add((Future) new ColorProduct(lightColor, surfaceColor, new RGB(scale, scale, scale)));
		}
		
		return color;
	}
	
	/** Returns a zero length array. */	
	public String[] getPropertyNames() { return new String[0]; }
	
	/** Returns a zero length array. */
	public String[] getPropertyDescriptions() { return new String[0]; }
	
	/** Returns a zero length array. */
	public Class[] getPropertyTypes() { return new Class[0]; }
	
	/** Returns a zero length array. */
	public Object[] getPropertyValues() { return new Object[0]; }
	
	/** @throws IndexOutOfBoundsException */
	public void setPropertyValue(Object value, int index) {
		throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}
	
	/**
	 * Does nothing.
	 */
	public void setPropertyValues(Object values[]) {}
	
	/**
	 * @return  An empty array.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[0]; }
	
	/** Does nothing. */
	public void setInputPropertyValue(int index, Producer p) {}
	
	/** Returns "Diffuse Shader". */
	public String toString() { return "Diffuse Shader"; }
}
