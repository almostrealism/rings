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

import com.almostrealism.raytracer.engine.*;

/**
  A SurfaceRemoveEvent object represents the event of removing a Surface object from the current Scene object.
*/

public class SurfaceRemoveEvent extends SceneEditEvent implements SurfaceEvent {
  private Surface target;

	/**
	  Constructs a new SurfaceRemoveEvent object using the specified target.
	*/
	
	public SurfaceRemoveEvent(Surface target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this SurfaceRemoveEvent object.
	*/
	
	public Surface getTarget() {
		return this.target;
	}
	
	/**
	  Returns "SurfaceRemoveEvent".
	*/
	
	public String toString() {
		return "SurfaceRemoveEvent";
	}
}
