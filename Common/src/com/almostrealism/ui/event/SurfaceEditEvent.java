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

package com.almostrealism.ui.event;

import com.almostrealism.raytracer.engine.*;

/**
  A SurfaceEditEvent object represents the event of editing a surface in the current scene.
  The integer code of the SurfaceEditEvent is the sum of all of the codes that apply to the edit.
*/

public class SurfaceEditEvent extends SceneEditEvent implements SurfaceEvent {
  /** The code for name change event. */
  public static final int nameChangeEvent = 1;
  
  /** the code for location change event. */
  public static final int locationChangeEvent = 1 << 1;
  
  /** The code for a size change event. */
  public static final int sizeChangeEvent = 1 << 2;
  
  /** The code for a scale coefficient change event. */
  public static final int scaleCoefficientChangeEvent = 1 << 3;
  
  /** The code for a rotation coefficient change event. */
  public static final int rotationCoefficientChangeEvent = 1 << 4;
  
  /** The code for a transformation change event. */
  public static final int transformationChangeEvent = 1 << 5;
  
  /** The code for color change event. */
  public static final int colorChangeEvent = 1 << 6;
  
  /** The code for a shading options change event. */
  public static final int shadingOptionChangeEvent = 1 << 7;
  
  /** The code for a data change event. */
  public static final int dataChangeEvent = 1 << 11;
  
  private int code = 0;
  private Surface target;
	
	/**
	  Constructs a new SurfaceEditEvent object with the specified integer code.
	*/
	
	public SurfaceEditEvent(int code, Surface target) {
		this.code = code;
		this.target = target;
	}
	
	/**
	  Returns the integer code of this SurfaceEditEvent object.
	*/
	
	public int getCode() {
		return this.code;
	}
	
	/**
	  Returns the target of this SurfaceEditEvent.
	*/
	
	public Surface getTarget() {
		return this.target;
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a name change event.
	*/
	
	public boolean isNameChangeEvent() {
		if ((this.code & SurfaceEditEvent.nameChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a location change event.
	*/
	
	public boolean isLocationChangeEvent() {
		if ((this.code & SurfaceEditEvent.locationChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a size change event.
	*/
	
	public boolean isSizeChangeEvent() {
		if ((this.code & SurfaceEditEvent.sizeChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a scale coefficient change event.
	*/
	
	public boolean isScaleCoefficientChangeEvent() {
		if ((this.code & SurfaceEditEvent.scaleCoefficientChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a rotation coefficient change event.
	*/
	
	public boolean isRotationCoefficientChangeEvent() {
		if ((this.code & SurfaceEditEvent.rotationCoefficientChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent obeject is a transformation change event.
	*/
	
	public boolean isTransformationChangeEvent() {
		if ((this.code & SurfaceEditEvent.transformationChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a color change event.
	*/
	
	public boolean isColorChangeEvent() {
		if ((this.code & SurfaceEditEvent.colorChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if thsi SurfaceEditEvent object is a shading option change event.
	*/
	
	public boolean isShadingOptionChangeEvent() {
		if ((this.code & SurfaceEditEvent.shadingOptionChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this SurfaceEditEvent object is a data change event.
	*/
	
	public boolean isDataChangeEvent() {
		if ((this.code & SurfaceEditEvent.dataChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns "SurfaceEditEvent".
	*/
	
	public String toString() {
		return "SurfaceEditEvent";
	}
}
