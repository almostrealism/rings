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

package com.almostrealism.raytracer.shaders;

import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;
import org.almostrealism.util.graphics.ColorProducer;
import org.almostrealism.util.graphics.RGB;

/**
 * A SilhouetteShader object can be used to shade a surface with one color value
 * for all parts of the surface.
 * 
 * @author Mike Murray
 */
public class SilhouetteShader implements Editable, Shader {
  private ColorProducer color;
  
  private String names[] = {"Color"};
  private String desc[] = {"The color of the silhouette"};
  private Class types[] = {ColorProducer.class};
  
  
	/**
	 * Constructs a new SilhouetteShader object using black as a color.
	 */
	public SilhouetteShader() { this.color = new RGB(0.0, 0.0, 0.0); }
	
	/**
	 * Constructs a new SilhouetteShader using the specified ColorProducer as a color.
	 * 
	 * @param color  ColorProducer to use.
	 */
	public SilhouetteShader(ColorProducer color) { this.color = color; }
	
	/**
	 * @see com.almostrealism.raytracer.shaders.Shader#shade(com.almostrealism.raytracer.shaders.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) { return this.color.evaluate(new Object[] {p}); }

	/**
	 * @see org.almostrealism.util.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) { return this.color.evaluate(args); }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() { return this.names; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() { return this.desc; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() { return this.types; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() { return new Object[] {this.color}; }

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object value, int index) {
		if (index == 0)
			this.color = (ColorProducer)value;
		else
			throw new IllegalArgumentException("Illegal property index: " + index);
	}

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object values[]) {
		if (values.length > 0) this.color = (ColorProducer)values[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] { this.color }; }

	/**
	 * @see org.almostrealism.util.Editable#setInputPropertyValue(int, org.almostrealism.util.Producer)
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.color = (ColorProducer)p;
		else
			throw new IllegalArgumentException("Illegal property index: " + index);
	}
	
	/**
	 * @return  "Silhouette Shader".
	 */
	public String toString() { return "Silhouette Shader"; }
}
