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

import com.almostrealism.raytracer.engine.Texture;

import net.sf.j3d.util.Editable;
import net.sf.j3d.util.Producer;
import net.sf.j3d.util.graphics.ColorProducer;
import net.sf.j3d.util.graphics.RGB;

/**
 * A TextureShader object uses a Texture object as a filter for shader output.
 * The object allows you to set up Shader objects as input properties for Texture
 * objects and takes care of evaluating the shader values with each call of the
 * shade method.
 * 
 * @author Mike Murray
 */
public class TextureShader extends ShaderSet implements Editable, Shader {
  private Texture tex;
  private ShaderFactory factory;
  
  private ColorProducer props[];

  	/**
  	 * Constructs a new Texture shader object.
  	 */
  	public TextureShader() { }
  	
  	/**
  	 * Constructs a new TextureShader object using the specified Texture and ShaderFactory objects.
  	 * 
  	 * @param t  The Texture object to use as a filter.
  	 * @param factory  The ShaderFactory that can be used to create Shader objects as parameters.
  	 */
	public TextureShader(Texture t, ShaderFactory factory) {
		this.setTexture(t);
		this.factory = factory;
	}
	
	/**
	 * Sets the Texture object used by this TextureShader object.
	 * 
	 * @param t  The Texture object to be used.
	 */
	public void setTexture(Texture t) {
		this.tex = t;
		
		if (this.tex instanceof Editable) {
			this.props = new ColorProducer[((Editable)this.tex).getInputPropertyValues().length];
			
			for (int i = 0; i < this.props.length; i++) {
				this.props[i] = (ColorProducer)((Editable)this.tex).getInputPropertyValues()[i];
			}
		}
	}
	
	/**
	 * Sets the ShaderFactory object used by this TextureShader object.
	 * 
	 * @param factory  The ShaderFactory object to use.
	 */
	public void setFactory(ShaderFactory factory) { this.factory = factory; }
	
	/**
	 * @return  The Texture object used by this TextureShader object.
	 */
	public Texture getTexture() { return this.tex; }
	
	/**
	 * @return  The ShaderFactory object used by this TextureShader object.
	 */
	public ShaderFactory getFactory() { return this.factory; }
	
	/**
	 * @see net.sf.j3d.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyNames();
		else
			return new String[0];
	}

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyDescriptions();
		else
			return new String[0];
	}

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyTypes();
		else
			return new Class[0];
	}

	/**
	 * @see net.sf.j3d.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyValues();
		else
			return new Object[0];
	}

	/**
	 * @see net.sf.j3d.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object value, int index) {
		if (this.tex instanceof Editable)
			((Editable)this.tex).setPropertyValue(value, index);
	}

	/**
	 * @see net.sf.j3d.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object[] values) {
		if (this.tex instanceof Editable)
			((Editable)this.tex).setPropertyValues(values);
	}

	/**
	 * @see net.sf.j3d.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return this.props; }

	/**
	 * @see net.sf.j3d.util.Editable#setInputPropertyValue(int, net.sf.j3d.util.Producer)
	 */
	public void setInputPropertyValue(int index, Producer p) { this.props[index] = (ColorProducer)p; }

	/**
	 * @see com.almostrealism.raytracer.shaders.Shader#shade(net.sf.j3d.threeD.raytracer.engine.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) {
		if (this.tex instanceof Editable) {
			Editable t = (Editable)this.tex;
			
			for (int i = 0; i < this.getInputPropertyValues().length; i++)
				t.setInputPropertyValue(i, this.props[i].evaluate(new Object[] {p}));
		}
		
		return this.tex.getColorAt(p.getPoint()).add(super.shade(p));
	}

	/**
	 * @see net.sf.j3d.util.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object[] args) { return this.shade((ShaderParameters)args[0]); }
	
	/**
	 * @return The string representation of the Texture object used by this TextureShader object
	 *         plus " Shader".
	 */
	public String toString() {
		if (this.tex != null)
			return this.tex.toString() + " Shader";
		else
			return "Texture Shader";
	}
}
