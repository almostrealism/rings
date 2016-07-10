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

package com.almostrealism.ui.menus;


import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.ui.event.*;
import com.almostrealism.ui.panels.*;

/**
  The DefaultMenuBar class extends JMenuBar and provides access to all of the standard menus
  in the threeD.ui.menus package.
*/

public class DefaultMenuBar extends JMenuBar implements EventListener, EventGenerator {
  private EventHandler handler;
  
  private FileMenu fileMenu;
  private EditMenu editMenu;
  private RenderMenu renderMenu;
  private HelpMenu helpMenu;

	/**
	  Constructs a new DefaultMenuBar object using the specified Scene object.
	*/
	
	public DefaultMenuBar(Scene scene, RenderPanel renderPanel) {
		SurfaceInfoPanel surfacePanel = new SurfaceInfoPanel(scene);
		
		this.fileMenu = new FileMenu(scene, renderPanel, surfacePanel);
		this.editMenu = new EditMenu(scene, surfacePanel, new LightInfoPanel(scene));
		this.renderMenu = new RenderMenu(renderPanel);
		this.helpMenu = new HelpMenu();
		
		this.add(this.fileMenu);
		this.add(this.editMenu);
		this.add(this.renderMenu);
		this.add(this.helpMenu);
		
		this.fileMenu.setEventHandler(this.handler);
		this.editMenu.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.fileMenu);
			this.handler.addListener(this.editMenu);
		}
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
	}
	
	/**
	  Sets the EventHandler object used by this DefaultMenuBar object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.fileMenu.setEventHandler(this.handler);
		this.editMenu.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.fileMenu);
			this.handler.addListener(this.editMenu);
		}
	}
	
	/**
	  Returns the EventHandler object used by this DefualtMenuBar object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
