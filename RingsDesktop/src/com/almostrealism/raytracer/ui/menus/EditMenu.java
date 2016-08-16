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

package com.almostrealism.raytracer.ui.menus;


import javax.swing.JMenu;

import org.almostrealism.space.Scene;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.ui.LightInfoPanel;
import com.almostrealism.raytracer.ui.SurfaceInfoPanel;

/** The EditMenu class extends JMenu and provides editing options for the user. */
public class EditMenu extends JMenu implements EventListener, EventGenerator {
  private EventHandler handler;
  
  private EditSceneMenu editSceneMenu;

	/**
	 * Constructs a new EditMenu object using the specified Scene object and the
	 * specified {@link SurfaceInfoPanel} and {@link LightInfoPanel} objects.
	 */
	public EditMenu(Scene scene, SurfaceInfoPanel surfaceInfoPanel, LightInfoPanel lightInfoPanel) {
		super("Edit");
		
		this.editSceneMenu = new EditSceneMenu(scene, surfaceInfoPanel, lightInfoPanel);
		this.editSceneMenu.setEventHandler(this.handler);
		
		this.add(this.editSceneMenu);
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
	}
	
	/**
	  Sets the EventHandler object used by this EditMenu object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		this.editSceneMenu.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.editSceneMenu);
		}
	}
	
	/**
	  Returns the EventHandler object used by this EditMenu object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
