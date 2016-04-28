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

package net.sf.j3d.ui.event;

import com.almostrealism.raytracer.lighting.*;

/**
  A LightAddEvent object represents the event of adding a new Light object to the current Scene object.
*/

public class LightAddEvent extends SceneEditEvent implements LightEvent {
  private Light target;

	/**
	  Constructs a new LightAddEvent object using the specified target.
	*/
	
	public LightAddEvent(Light target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this LightAddEvent object.
	*/
	
	public Light getTarget() {
		return this.target;
	}
	
	/**
	  Returns "LightAddEvent".
	*/
	
	public String toString() {
		return "LightAddEvent";
	}
}
