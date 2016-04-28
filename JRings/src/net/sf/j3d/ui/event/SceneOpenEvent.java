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
  A SceneOpenEvent object represents the event of opening of a new scene.
  It stores the new Scene object and provides access to it.
*/

public class SceneOpenEvent extends SceneEvent {
  private Scene scene;
	
	/**
	  Constructs a new SceneOpenEvent using the specified Scene object.
	*/
	
	public SceneOpenEvent(Scene scene) {
		this.scene = scene;
	}
	
	/**
	  Returns the new Scene object.
	*/
	
	public Scene getScene() {
		return this.scene;
	}
	
	/**
	  Returns "SceneOpenEvent".
	*/
	
	public String toString() {
		return "SceneOpenEvent";
	}
}
