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

package com.almostrealism.ui.event;

import com.almostrealism.raytracer.lighting.*;

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
