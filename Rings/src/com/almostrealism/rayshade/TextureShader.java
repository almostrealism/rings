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

import org.almostrealism.texture.ColorProducer;
import org.almostrealism.texture.RGB;
import org.almostrealism.texture.Texture;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

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
	 * @see org.almostrealism.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyNames();
		else
			return new String[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyDescriptions();
		else
			return new String[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyTypes();
		else
			return new Class[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() {
		if (this.tex instanceof Editable)
			return ((Editable)this.tex).getPropertyValues();
		else
			return new Object[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object value, int index) {
		if (this.tex instanceof Editable)
			((Editable)this.tex).setPropertyValue(value, index);
	}

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object[] values) {
		if (this.tex instanceof Editable)
			((Editable)this.tex).setPropertyValues(values);
	}

	/**
	 * @see org.almostrealism.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() { return this.props; }

	/**
	 * @see org.almostrealism.util.Editable#setInputPropertyValue(int, org.almostrealism.util.Producer)
	 */
	public void setInputPropertyValue(int index, Producer p) { this.props[index] = (ColorProducer)p; }

	/**
	 * @see com.almostrealism.rayshade.Shader#shade(com.almostrealism.raytracer.engine.ShaderParameters)
	 */
	public ColorProducer shade(ShaderParameters p) {
		if (this.tex instanceof Editable) {
			Editable t = (Editable)this.tex;
			
			for (int i = 0; i < this.getInputPropertyValues().length; i++)
				t.setInputPropertyValue(i, this.props[i].evaluate(new Object[] {p}));
		}
		
		return this.tex.getColorAt(p.getPoint()).add(super.shade(p));
	}

	/**
	 * @see org.almostrealism.texture.ColorProducer#evaluate(java.lang.Object[])
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
