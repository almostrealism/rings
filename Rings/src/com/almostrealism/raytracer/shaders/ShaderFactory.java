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

import com.almostrealism.util.Editable;
import com.almostrealism.util.EditableFactory;

/**
 * The ShaderFactory class provides static methods for constructing Shader objects.
 * 
 * @author Mike Murray
 */
public class ShaderFactory extends EditableFactory {
  public static final int blendingShader = 0;
  public static final int diffuseShader = 1;
  public static final int highlightShader = 2;
  public static final int reflectionShader = 3;
  public static final int refractionShader = 4;
  public static final int silhouetteShader = 5;
  public static final int textureShader = 6;
  
  private static final String typeNames[] = {"Blending Shader", "Diffuse Shader",
  											"Highlight Shader", "Reflection Shader",
											"Refraction Shader", "Silhouette Shader",
											"Texture Shader"};

	/**
	 * Returns an array of String objects containing names for each type of Shader object
	 * this ShaderFactory can construct.
	 */
	public String[] getTypeNames() { return ShaderFactory.typeNames; }
	
	/**
	 * Constructs a Shader object of the type specified by the integer index.
	 * 
	 * @throws IndexOutOfBoundsException  If the specified index does not correspond to a type of shader.
	 */
	public synchronized Editable constructObject(int index) {
		if (index == ShaderFactory.blendingShader) {
			return new BlendingShader();
		} else if (index == ShaderFactory.diffuseShader) {
			return DiffuseShader.defaultDiffuseShader;
		} else if (index == ShaderFactory.highlightShader) {
			return new HighlightShader();
		} else if (index == ShaderFactory.reflectionShader) {
			ReflectionShader s = new ReflectionShader();
// TODO		s.setEnvironmentMap(new ImageTexture());
			
			return s;
		} else if (index == ShaderFactory.refractionShader) {
			return new RefractionShader();
		} else if (index == ShaderFactory.silhouetteShader) {
			return new SilhouetteShader();
		} else if (index == ShaderFactory.textureShader) {
			TextureShader ts = new TextureShader(null, this);
			
// TODO		AddDialog d = new AddDialog(ts, null, new TextureFactory());
//			d.setVisible(true);
			
			return ts;
		} else {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
	}
}
