/*
 * Copyright (C) 2004  Mike Murray
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

package com.almostrealism.texture;

import com.almostrealism.util.*;

/**
  The TextureFactory class provides static methods for constructing Texture objects.
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
