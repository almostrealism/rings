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

import com.almostrealism.raytracer.engine.*;

/**
  A SurfaceAddEvent object represents the event of adding a new surface to the current scene.
*/

public class SurfaceAddEvent extends SceneEditEvent implements SurfaceEvent {
  private Surface target;

	/**
	  Constructs a new SurfaceAddEvent object with the specified target.
	*/
	
	public SurfaceAddEvent(Surface target) {
		this.target = target;
	}
	
	/**
	  Returns the target of this SurfaceAddEvent object.
	*/
	
	public Surface getTarget() {
		return this.target;
	}
	
	/**
	  Returns "SurfaceAddEvent";
	*/
	
	public String toString() {
		return "SurfaceAddEvent";
	}
}
