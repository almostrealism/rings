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

import org.almostrealism.space.Scene;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.ui.EditCameraDialog;
import com.almostrealism.raytracer.ui.LightInfoPanel;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.raytracer.ui.SurfaceInfoPanel;

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
