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

package net.sf.j3d.ui.menus;


import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import net.sf.j3d.ui.event.*;
import net.sf.j3d.ui.panels.*;

/**
  The EditMenu class extends JMenu and provides editing options for the user.
*/

public class EditMenu extends JMenu implements EventListener, EventGenerator {
  private EventHandler handler;
  
  private EditSceneMenu editSceneMenu;

	/**
	  Constructs a new EditMenu object using the specified Scene object and the specified SurfaceInfoPanel and LightInfoPanel objects.
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
