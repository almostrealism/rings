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

package com.almostrealism.raytracer.primitives;

import org.almostrealism.texture.ImageTexture;
import org.almostrealism.util.*;

/**
 * The TextureFactory class provides static methods for constructing Texture objects.
 */
public class TextureFactory extends EditableFactory {
  public static final int stripeTexture = 0;
  public static final int imageTexture = 1;
  
  private static final String typeNames[] = {"Stripe Texture", "Image Texture"};

	/**
	  Returns an array of String objects containing names for each type of Texture object
	  this TextureFactory can construct.
	*/
	
	public String[] getTypeNames() {
		return TextureFactory.typeNames;
	}
	
	/**
	  Constructs a Shader object of the type specified by the integer index.
	  
	  @throws IndexOutOfBoundsException  If the specified index is out of bounds.
	*/
	
	public Editable constructObject(int index) {
		if (index == 0) {
			return new StripeTexture();
		} else if (index == 1) {
		    return new ImageTexture();
		} else {
			throw new IndexOutOfBoundsException("Index out of bounds: " + index);
		}
	}
}
