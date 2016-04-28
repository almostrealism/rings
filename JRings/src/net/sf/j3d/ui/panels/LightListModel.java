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

package net.sf.j3d.ui.panels;


import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import net.sf.j3d.ui.event.*;

/**
  The LightListModel class extends AbstractListModel and provides a list model that dynamically displays
  the Light objects of a Scene object.
*/

public class LightListModel extends AbstractListModel implements EventListener {
 private Scene scene;

	/**
	  Constructs a new LightListModel object using the specified Scene object.
	*/
	
	public LightListModel(Scene scene) {
		this.scene = scene;
	}
	
	/**
	  Returns the Scene object used by this LightListModel object.
	*/
	
	public Scene getScene() {
		return this.scene;
	}
	
	/**
	  Returns the number of Light objects that are contained in the Scene object
	  used by this LightListModel object.
	*/
	
	public int getSize() {
		return this.scene.getLights().length;
	}
	
	/**
	  Returns the Light object at the specified index.
	*/
	
	public Object getElementAt(int index) {
		return this.scene.getLight(index);
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			SceneOpenEvent openEvent = (SceneOpenEvent)event;
			this.scene = openEvent.getScene();
			
			this.fireContentsChanged(this, 0, this.getSize());
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
			
			this.fireContentsChanged(this, 0, this.getSize());
		}
		
		if (event instanceof LightAddEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		} else if (event instanceof LightRemoveEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		} else if (event instanceof LightEditEvent) {
			this.fireContentsChanged(this, 0, this.getSize());
		}
	}
}
