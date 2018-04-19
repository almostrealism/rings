/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.surfaceui;

import org.almostrealism.algebra.TransformMatrix;

/**
 * A {@link TransformMatrixUI} object stores extra data about
 * a {@link TransformMatrix} object such as its type for use
 * in an application with a user interface.
 */
public class TransformMatrixUI extends TransformMatrix {
  /** Code for a translation transformation. */
  public static final int translationTransformation = 1;
  
  /** Code for a scale transformation. */
  public static final int scaleTransformation = 1 << 1;
  
  /** Code for a rotate-X transformation. */
  public static final int rotateXTransformation = 1 << 2;
  
  /** Code for a rotate-Y transformation. */
  public static final int rotateYTransformation = 1 << 3;
  
  /** Code for a rotate-Z transformation. */
  public static final int rotateZTransformation = 1 << 4;
  
  private int type;
  private String info;
  
  private double x, y, z;

	/**
	  Constructs a new TransformMatrixUI object with no type.
	*/
	
	public TransformMatrixUI() {
		this.type = -1;
	}
	
	/**
	  Constructs a new TransformMatrixUI object of the specified type. If this type is translationTransformation or scaleTransformation,
	  the specified value is used for all 3 coefficients. If the type is rotateXTransformation, rotateYTransformation, or rotateZTransformation
	  the specified value is used as the rotation coefficient (in radians).
	*/
	
	public TransformMatrixUI(int type, double value) {
		this.type = type;
		
		this.init(value, value, value);
	}
	
	/**
	  Constructs a new TransformMatrixUI object of the specified type. If this type is translationTransformation or scaleTransformation,
	  the specified values are used as the 3 coefficients. If the type is rotateXTransformation, rotateYTransformation, or rotateZTransformation
	  the value coresponding in name to the axis of rotation is used as the rotation coefficient (in radians) and the other values are ignored.
	*/
	
	public TransformMatrixUI(int type, double x, double y, double z) {
		this.type = type;
		
		this.init(x, y, z);
	}
	
	/**
	  Initializes this TransformMatrixUI object.
	*/
	
	private void init(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		if (this.type == TransformMatrixUI.translationTransformation) {
			this.setMatrix(TransformMatrix.createTranslationMatrix(x, y, z).getMatrix());
			this.info = "Translation: " + ((int)(x * 1000)) / 1000.0 + ", " + ((int)(y * 1000)) / 1000.0 + ", " + ((int)(z * 1000)) / 1000.0;
		} else if (this.type == TransformMatrixUI.scaleTransformation) {
			this.setMatrix(TransformMatrix.createScaleMatrix(x, y, z).getMatrix());
			this.info = "Scale: " + ((int)(x * 1000)) / 1000.0 + ", " + ((int)(y * 1000)) / 1000.0 + ", " + ((int)(z * 1000)) / 1000.0;
		} else if (this.type == TransformMatrixUI.rotateXTransformation) {
			this.setMatrix(TransformMatrix.createRotateXMatrix(x).getMatrix());
			this.info = "Rotate X: " + ((int)(x * 1000)) / 1000.0 + " Radians";
		} else if (this.type == TransformMatrixUI.rotateYTransformation) {
			this.setMatrix(TransformMatrix.createRotateYMatrix(y).getMatrix());
			this.info = "Rotate Y: " + ((int)(y * 1000)) / 1000.0 + " Radians";
		} else if (this.type == TransformMatrixUI.rotateZTransformation) {
			this.setMatrix(TransformMatrix.createRotateZMatrix(z).getMatrix());
			this.info = "Rotate Z: " + ((int)(z * 1000)) / 1000.0 + " Radians";
		}
	}
	
	/**
	  Sets the X coefficient of translation, scaling, or rotation depending on the type of this TransformMatrixUI object.
	  Calling this method will cause this TransformMatrixUI object to be initialized again.
	*/
	
	public void setX(double x) {
		this.x = x;
		
		this.init(this.x, this.y, this.z);
	}
	
	/**
	  Sets the Y coefficient of translation, scaling, or rotation depending on the type of this TransformMatrixUI object.
	  Calling this method will cause this TransformMatrixUI object to be initialized again.
	*/
	
	public void setY(double y) {
		this.y = y;
		
		this.init(this.x, this.y, this.z);
	}
	
	/**
	  Sets the Z coefficient of translation, scaling, or rotation depending on the type of this TransformMatrixUI object.
	  Calling this method will cause this TransformMatrixUI object to be initialized again.
	*/
	
	public void setZ(double z) {
		this.z = z;
		
		this.init(this.x, this.y, this.z);
	}
	
	/**
	  Returns the X coefficient of translation, scaling, or rotation depending the type of this TransformMatrixUI object.
	*/
	
	public double getX() {
		return this.x;
	}
	
	/**
	  Returns the Y coefficient of translation, scaling, or rotation depending the type of this TransformMatrixUI object.
	*/
	
	public double getY() {
		return this.y;
	}
	
	/**
	  Returns the Z coefficient of translation, scaling, or rotation depending the type of this TransformMatrixUI object.
	*/
	
	public double getZ() {
		return this.z;
	}
	
	/**
	  Sets the type of this TransformMatrixUI object to the type represented by the specified integer code.
	  Calling this method will cause this TransformMatrixUI object to be initialized again.
	*/
	
	public void setType(int type) {
		this.type = type;
		
		this.init(this.x, this.y, this.z);
	}
	
	/**
	  Returns the type of this TransformMatrixUI object.
	*/
	
	public int getType() {
		return this.type;
	}
	
	/**
	  Returns a String representation of this TransformMatrixUI object.
	*/
	
	public String toString() {
		return this.info;
	}
}
