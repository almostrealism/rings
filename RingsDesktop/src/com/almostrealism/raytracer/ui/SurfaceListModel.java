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

import javax.swing.AbstractListModel;

import org.almostrealism.ui.Event;
import org.almostrealism.ui.EventListener;

import com.almostrealism.raytracer.engine.Scene;

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
