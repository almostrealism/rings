/*
* Copyright (C) 2004-05  Mike Murray
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

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.*;
import net.sf.j3d.ui.dialogs.*;
import net.sf.j3d.ui.event.*;
import net.sf.j3d.ui.panels.*;

/**
 * The EditSceneMenu class extends JMenu and provides menu items for editing a scene.
 */
public class EditSceneMenu extends JMenu implements EventListener, EventGenerator {
  private Scene scene;
  
  private SurfaceInfoPanel surfaceInfo;
  private LightInfoPanel lightInfo;
  
  private EventHandler handler;
  
  private JMenuItem editCameraItem, surfaceInfoItem, lightInfoItem;

	/**
	 * Constructs a new EditSceneMenu object using the specified Scene object and the specified SurfaceInfoPanel and LightInfoPanel objects.
	 */
	public EditSceneMenu(Scene scn, SurfaceInfoPanel surfaceInfoPanel, LightInfoPanel lightInfoPanel) {
		super("Edit Scene");
		
		this.scene = scn;
		
		this.surfaceInfo = surfaceInfoPanel;
		this.lightInfo = lightInfoPanel;
		
		this.editCameraItem = new JMenuItem("Edit Camera");
		this.surfaceInfoItem = new JMenuItem("Open Surface Info Panel");
		this.lightInfoItem = new JMenuItem("Open Light Info Panel");
		
		this.add(this.editCameraItem);
		this.addSeparator();
		this.add(this.surfaceInfoItem);
		this.add(this.lightInfoItem);
		
		this.editCameraItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (scene.getCamera() instanceof  OrthographicCamera) {
					EditCameraDialog editDialog = new EditCameraDialog(scene);
					
					editDialog.setEventHandler(handler);
					
					if (handler != null) {
						handler.addListener(editDialog);
					}
					
					editDialog.showDialog();
				}
			}
		});
		
		this.surfaceInfoItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				surfaceInfo.showPanel();
			}
		});
		
		this.lightInfoItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				lightInfo.showPanel();
			}
		});
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditSceneMenu object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.surfaceInfo.setEventHandler(handler);
		this.lightInfo.setEventHandler(handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.surfaceInfo);
			this.handler.addListener(this.lightInfo);
		}
	}
	
	/**
	 * Returns the EventHandler object used by this EditSceneMenu object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
