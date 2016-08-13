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

package com.almostrealism.raytracer.ui;

import com.almostrealism.raytracer.lighting.Light;

/**
  A LightEditEvent object represents the event of editing a light in the current scene.
  The integer code of the LightEditEvent is the sum of all of the codes that apply to the edit.
*/

public class LightEditEvent extends SceneEditEvent implements LightEvent {
  /** The code for a location change event. */
  public static final int locationChangeEvent = 1;
  
  /** The code for an attenuation coefficient change event. */
  public static final int attenuationCoefficientChangeEvent = 1 << 1;
  
  /** The code for a direction change event. */
  public static final int directionChangeEvent = 1 << 2;
  
  /** The code for a color change event. */
  public static final int colorChangeEvent = 1 << 3;
  
  /** The code for an intensity change event. */
  public static final int intensityChangeEvent = 1 << 4;
  
  private int code = 0;
  private Light target;

	/**
	  Constructs a new LightEditEvent object with the specified integer code and target.
	*/
	
	public LightEditEvent(int code, Light target) {
		this.code = code;
		this.target = target;
	}
	
	/**
	  Returns the integer code of this LightEditEvent object.
	*/
	
	public int getCode() {
		return this.code;
	}
	
	/**
	  Returns the target of this LightEditEvent object.
	*/
	
	public Light getTarget() {
		return this.target;
	}
	
	/**
	  Returns true if this LightEditEvent object is a location change event.
	*/
	
	public boolean isLocationChangeEvent() {
		if ((this.code & LightEditEvent.locationChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this LightEditEvent object is an attenuation coefficient change event.
	*/
	
	public boolean isAttenuationCoefficientChangeEvent() {
		if ((this.code & LightEditEvent.attenuationCoefficientChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this LightEditEvent object is a direction change event.
	*/
	
	public boolean isDirectionChangeEvent() {
		if ((this.code & LightEditEvent.directionChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this LightEditEvent object is a color change event.
	*/
	
	public boolean isColorChangeEvent() {
		if ((this.code & LightEditEvent.colorChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns true if this LightEditEvent object is an intensity change event.
	*/
	
	public boolean isIntensityChangeEvent() {
		if ((this.code & LightEditEvent.intensityChangeEvent) != 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	  Returns "LightEditEvent".
	*/
	
	public String toString() {
		return "LightEditEvent";
	}
}
