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

package com.almostrealism.ui.panels;


import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.ui.event.*;

/**
  The SurfaceListModel class extends AbstractListModel and provides a list model that dynamically displays
  the Surface objects of a Scene object.
*/

public class SurfaceListModel extends AbstractListModel implements EventListener {
 private Scene scene;

	/**
	  Constructs a new SurfaceListModel object using the specified Scene object.
	*/
	
	public SurfaceListModel(Scene scene) {
		this.scene = scene;
	}
	
	/**
	  Returns the number of Surface objects that are contained in the Scene object
	  used by this SurfaceListModel object.
	*/
	
	public int getSize() {
		return this.scene.getSurfaces().length;
	}
	
	/**
	  Returns the Surface object at the specified index.
	*/
	
	public Object getElementAt(int index) {
		return this.scene.getSurface(index);
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof LightAddEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		} else if (event instanceof LightRemoveEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		} else if (event instanceof LightEditEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}
}
