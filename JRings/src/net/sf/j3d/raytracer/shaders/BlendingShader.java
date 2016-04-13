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

package net.sf.j3d.raytracer.shaders;

import net.sf.j3d.util.Editable;
import net.sf.j3d.util.Producer;
import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.ColorProducer;
import net.sf.j3d.util.graphics.RGB;

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
	 * @see net.sf.j3d.raytracer.shaders.Shader#shade(net.sf.j3d.raytracer.shaders.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) {
		Vector n = p.getSurface().getNormalAt(p.getPoint());
		Vector l = p.getLightDirection();
		
		double k = (1.0 + n.dotProduct(l));
		
		RGB hc = this.hotColor.evaluate(new Object[] {p});
		RGB cc = this.coldColor.evaluate(new Object[] {p});
		
		RGB c = hc.multiply(k);
		c.addTo(cc.multiply(1 - k));
		
		return c;
	}

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() { return BlendingShader.names; }

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() { return BlendingShader.desc; }

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() { return BlendingShader.types; }

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() { return this.getInputPropertyValues(); }

	/**
	 * @see net.sf.j3d.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object o, int index) { this.setInputPropertyValue(index, (Producer)o); }

	/**
	 * @see net.sf.j3d.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++) this.setPropertyValue(values[i], i);
	}

	/**
	 * @see net.sf.j3d.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return new Producer[] {this.hotColor, this.coldColor}; }

	/**
	 * @see net.sf.j3d.util.Editable#setInputPropertyValue(int, net.sf.j3d.util.Producer)
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
	 * @see net.sf.j3d.util.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) { return this.shade((ShaderParameters)args[0]); }
	
	/**
	 * @return  "Blending Shader".
	 */
	public String toString() { return "Blending Shader"; }
}
