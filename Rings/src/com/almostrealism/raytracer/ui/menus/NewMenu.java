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
import javax.swing.JMenuItem;

import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.ui.NewSurfaceDialog;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.ui.Event;
import com.almostrealism.ui.EventGenerator;
import com.almostrealism.ui.EventHandler;
import com.almostrealism.ui.EventListener;

/**
  A NewMenu object extends JMenu and provides menu items for creating new scenes and surfaces.
*/

public class NewMenu extends JMenu implements EventListener, EventGenerator {
  private Scene scene;
  
  private EventHandler handler;
  
  private JMenuItem newSceneItem, newSurfaceItem;

	/**
	  Constructs a new NewMenu object.
	*/
	
	public NewMenu(Scene scn) {
		super("New");
		
		this.scene = scn;
		
		this.newSceneItem = new JMenuItem("Scene");
		this.newSurfaceItem = new JMenuItem("Surface");
		
		this.add(this.newSceneItem);
		this.add(this.newSurfaceItem);
		
		this.newSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				newScene();
			}
		});
		
		this.newSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				NewSurfaceDialog newDialog = new NewSurfaceDialog(scene);
				newDialog.setEventHandler(handler);
				
				newDialog.showDialog();
			}
		});
	}
	
	/**
	  Creates a new Scene object and fires the required events.
	*/
	
	public void newScene() {
		Scene newScene = new Scene();
		
		if (this.handler != null) {
			this.handler.fireEvent(new SceneOpenEvent(newScene));
		}
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
		}
	}
	
	/**
	  Sets the EventHandler object used by this NewMenu object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
