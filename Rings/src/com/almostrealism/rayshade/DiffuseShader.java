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

import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

import com.almostrealism.raytracer.Settings;

/**
 * A DiffuseShader object provides a shading method for diffuse surfaces.
 * The DiffuseShader class uses a lambertian shading algorithm.
 * 
 * @author Mike Murray
 */
public class DiffuseShader implements Shader, Editable {
  public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
  public static boolean produceOutput = false;

	/**
	 * Constructs a new DiffuseShader object.
	 */
	public DiffuseShader() {}
	
	/**
	 * Method specified by the Shader interface.
	 */
	public RGB shade(ShaderParameters p) {
		RGB lightColor = p.getLight().getColorAt(p.getPoint());
		
		Vector n = p.getSurface().getNormalAt(p.getPoint());
		RGB surfaceColor = p.getSurface().getColorAt(p.getPoint());
		
		RGB color = new RGB(0.0, 0.0, 0.0);
		
		if (Settings.produceShaderOutput && DiffuseShader.produceOutput) {
			Settings.shaderOut.print("Diffuse Shader: " + lightColor + " " + n + " " + surfaceColor + "  " +
									p.getSurface().getShadeFront() + " " + p.getSurface().getShadeBack());
		}
		
		if (p.getSurface().getShadeFront())
			color.addTo((lightColor.multiply(surfaceColor)).multiply(n.dotProduct(p.getLightDirection())));
		
		if (p.getSurface().getShadeBack())
			color.addTo((lightColor.multiply(surfaceColor)).multiply(n.minus().dotProduct(p.getLightDirection())));
		
		if (Settings.produceShaderOutput && DiffuseShader.produceOutput) {
			Settings.shaderOut.println(" -- " + color);
		}
		
		return color;
	}
	
	/**
	 * @throws IllegalArgumentException  If args[0] is not a ShaderParameters object.
	 * @return  this.shade(args[0]).
	 */
	public RGB evaluate(Object args[]) {
	    if (args[0] instanceof ShaderParameters) {
	        return this.shade((ShaderParameters)args[0]);
	    } else {
	        throw new IllegalArgumentException("Illegal argument: " + args[0]);
	    }
	}
	
	/**
	 * Returns a zero length array.
	 */	
	public String[] getPropertyNames() { return new String[0]; }
	
	/**
	 * Returns a zero length array.
	 */
	public String[] getPropertyDescriptions() { return new String[0]; }
	
	/**
	 * Returns a zero length array.
	 */
	public Class[] getPropertyTypes() { return new Class[0]; }
	
	/**
	 * Returns a zero length array.
	 */
	public Object[] getPropertyValues() { return new Object[0]; }
	
	/**
	 * @throws IndexOutOfBoundsException
	 */
	public void setPropertyValue(Object value, int index) { throw new IndexOutOfBoundsException("Index out of bounds: " + index); }
	
	/**
	 * Does nothing.
	 */
	public void setPropertyValues(Object values[]) {}
	
	/**
	 * @return  An empty array.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[0]; }
	
	/**
	 * Does nothing.
	 */
	public void setInputPropertyValue(int index, Producer p) {}
	
	/**
	 * Returns "Diffuse Shader".
	 */
	public String toString() { return "Diffuse Shader"; }
}
