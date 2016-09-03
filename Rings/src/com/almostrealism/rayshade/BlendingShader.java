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

import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Vector;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

/**
 * A BlendingShader object provides a method for blending values from two
 * different ColorProducer instances based on lighting. This is best for
 * cool to warm shading or cartoon shading.
 * 
 * @author Mike Murray
 */
public class BlendingShader implements Shader, Editable {
  private static final String names[] = {"Hot color", "Cold color"};
  private static final String desc[] = {"Color for hot (lit) area.", "Color for cold (dim) area."};
  private static final Class types[] = {ColorProducer.class, ColorProducer.class};
  
  private ColorProducer hotColor, coldColor;

	/**
	 * Constructs a new BlendingShader using white as a hot color
	 * and black as a cold color.
	 */
	public BlendingShader() {
		this.hotColor = new RGB(1.0, 1.0, 1.0);
		this.coldColor = new RGB(0.0, 0.0, 0.0);
	}
	
	/**
	 * Constructs a new BlendingShader using the specified hot and cold colors.
	 * 
	 * @param hot  ColorProducer to use for hot color.
	 * @param cold  ColorProducer to use for cold color.
	 */
	public BlendingShader(ColorProducer hot, ColorProducer cold) {
		this.hotColor = hot;
		this.coldColor = cold;
	}
	
	/**
	 * @see com.almostrealism.rayshade.Shader#shade(com.almostrealism.rayshade.ShaderParameters)
	 */
	public ColorProducer shade(ShaderParameters p) {
		Vector n = p.getIntersection().getNormal(0);
		Vector l = p.getLightDirection();
		
		double k = (1.0 + n.dotProduct(l));
		
		RGB hc = this.hotColor.evaluate(new Object[] {p});
		RGB cc = this.coldColor.evaluate(new Object[] {p});
		
		RGB c = hc.multiply(k);
		c.addTo(cc.multiply(1 - k));
		
		return c;
	}

	/**
	 * @see org.almostrealism.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() { return BlendingShader.names; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() { return BlendingShader.desc; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() { return BlendingShader.types; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() { return this.getInputPropertyValues(); }

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object o, int index) { this.setInputPropertyValue(index, (Producer)o); }

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++) this.setPropertyValue(values[i], i);
	}

	/**
	 * @see org.almostrealism.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] {this.hotColor, this.coldColor}; }

	/**
	 * @see org.almostrealism.util.Editable#setInputPropertyValue(int, org.almostrealism.util.Producer)
	 * @throws IndexOutOfBoundsException  If the property index is out of bounds.
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.hotColor = (ColorProducer)p;
		else if (index == 1)
			this.coldColor = (ColorProducer)p;
		else
			throw new IndexOutOfBoundsException("Property index out of bounds: " + index);
	}
	
	/**
	 * @return  "Blending Shader".
	 */
	public String toString() { return "Blending Shader"; }
}
