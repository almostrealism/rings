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
  A LightRemoveEvent object represents the event of removing a Light object from the current Scene object.
*/

public class LightRemoveEvent extends SceneEditEvent implements LightEvent {
  private Light target;

	/**
	  Constructs a new LightRemoveEvent object using the specified target.
	*/
	
	public LightRemoveEvent(Light target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this LightRemoveEvent object.
	*/
	
	public Light getTarget() {
		return this.target;
	}
	
	/**
	  Returns "LightRemoveEvent".
	*/
	
	public String toString() {
		return "LightRemoveEvent";
	}
}
