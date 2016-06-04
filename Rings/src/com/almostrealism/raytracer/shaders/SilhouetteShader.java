/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.shaders;

import com.almostrealism.util.Editable;
import com.almostrealism.util.Producer;
import com.almostrealism.util.graphics.ColorProducer;
import com.almostrealism.util.graphics.RGB;

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
	 * @see com.almostrealism.util.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) { return this.color.evaluate(args); }

	/**
	 * @see com.almostrealism.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() { return this.names; }

	/**
	 * @see com.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() { return this.desc; }

	/**
	 * @see com.almostrealism.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() { return this.types; }

	/**
	 * @see com.almostrealism.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() { return new Object[] {this.color}; }

	/**
	 * @see com.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object value, int index) {
		if (index == 0)
			this.color = (ColorProducer)value;
		else
			throw new IllegalArgumentException("Illegal property index: " + index);
	}

	/**
	 * @see com.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object values[]) {
		if (values.length > 0) this.color = (ColorProducer)values[0];
	}

	/**
	 * @see com.almostrealism.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] { this.color }; }

	/**
	 * @see com.almostrealism.util.Editable#setInputPropertyValue(int, com.almostrealism.util.Producer)
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
