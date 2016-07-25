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

package com.almostrealism.ui.menus;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.ui.SurfaceInfoPanel;
import com.almostrealism.ui.dialogs.EditCameraDialog;
import com.almostrealism.ui.dialogs.RenderOptionsDialog;
import com.almostrealism.ui.event.Event;
import com.almostrealism.ui.event.EventGenerator;
import com.almostrealism.ui.event.EventHandler;
import com.almostrealism.ui.event.EventListener;
import com.almostrealism.ui.event.SceneCloseEvent;
import com.almostrealism.ui.event.SceneOpenEvent;
import com.almostrealism.ui.panels.LightInfoPanel;
import com.almostrealism.ui.panels.RenderPanel;

/**
 * @author Mike Murray
 */
public class DefaultToolBar extends JToolBar implements MouseListener, EventListener, EventGenerator {
	private NewMenu newMenu;
	private OpenMenu openMenu;
	private SaveMenu saveMenu;
	private ImportMenu importMenu;
	private NetworkMenu networkMenu;
	private HelpMenu helpMenu;
	
	private JPopupMenu popup;
	
	private JButton newButton, openButton, saveButton, closeButton, importButton;
	private JButton cameraButton, surfaceButton, lightButton;
	private JButton startButton, clearButton, optionsButton, networkButton;
	private JButton aboutButton;
	
	private Scene scene;
	
	private SurfaceInfoPanel spanel;
	private LightInfoPanel lpanel;
	private RenderPanel rpanel;
	
	private RenderOptionsDialog optionsDialog;
	
	private EventHandler handler;
	
	public DefaultToolBar(Scene s, RenderPanel rpanel) {
		this.scene = s;
		
		this.spanel = new SurfaceInfoPanel(this.scene);
		this.lpanel = new LightInfoPanel(this.scene);
		this.rpanel = rpanel;
		
		this.optionsDialog = new RenderOptionsDialog(this.rpanel);
		
		this.newMenu = new NewMenu(this.scene);
		this.openMenu = new OpenMenu(this.scene);
		this.saveMenu = new SaveMenu(this.scene, rpanel, this.spanel);
		this.importMenu = new ImportMenu(this.scene);
		this.networkMenu = new NetworkMenu();
		this.helpMenu = new HelpMenu();
		
		this.newButton = new JButton("New");
		this.openButton = new JButton("Open");
		this.saveButton = new JButton("Save");
		this.importButton = new JButton("Import");
		this.closeButton = new JButton("X");
		
		this.cameraButton = new JButton("C");
		this.surfaceButton = new JButton("S");
		this.lightButton = new JButton("L");
		
		this.startButton = new JButton("R");
		this.clearButton = new JButton("X");
		this.optionsButton = new JButton("O");
		this.networkButton = new JButton("Net");
		
		this.aboutButton = new JButton("?");
		
		this.newButton.addMouseListener(this);
		this.openButton.addMouseListener(this);
		this.saveButton.addMouseListener(this);
		this.importButton.addMouseListener(this);
		this.closeButton.addMouseListener(this);
		
		this.cameraButton.addMouseListener(this);
		this.surfaceButton.addMouseListener(this);
		this.lightButton.addMouseListener(this);
		
		this.startButton.addMouseListener(this);
		this.clearButton.addMouseListener(this);
		this.optionsButton.addMouseListener(this);
		this.networkButton.addMouseListener(this);
		
		this.aboutButton.addMouseListener(this);
		
		super.add(this.newButton);
		super.add(this.openButton);
		super.add(this.saveButton);
		super.add(this.importButton);
		super.add(this.closeButton);
		super.addSeparator();
		super.add(this.cameraButton);
		super.add(this.surfaceButton);
		super.add(this.lightButton);
		super.addSeparator();
		super.add(this.startButton);
		super.add(this.clearButton);
		super.add(this.optionsButton);
		super.add(this.networkButton);
		super.add(Box.createHorizontalGlue());
		super.add(this.aboutButton);
	}

	public void mouseClicked(MouseEvent event) {
		if (event.getSource() == this.closeButton) {
			if (handler != null) handler.fireEvent(new SceneCloseEvent());
		} else if (event.getSource() == this.cameraButton) {
			if (this.scene.getCamera() instanceof OrthographicCamera) {
				EditCameraDialog editDialog = new EditCameraDialog(scene);
				editDialog.setEventHandler(handler);
				if (this.handler != null) this.handler.addListener(editDialog);
				
				editDialog.showDialog();
			}
		} else if (event.getSource() == this.surfaceButton) {
			this.spanel.showPanel();
		} else if (event.getSource() == this.lightButton) {
			this.lpanel.showPanel();
		} else if (event.getSource() == this.startButton) {
			this.rpanel.render();
		} else if (event.getSource() == this.clearButton) {
			this.rpanel.clearRenderedImage();
		} else if (event.getSource() == this.optionsButton) {
			this.optionsDialog.showDialog();
		} else if (event.getSource() == this.newButton) {
			this.popup = this.newMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		} else if (event.getSource() == this.openButton) {
			this.popup = this.openMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		} else if (event.getSource() == this.saveButton) {
			this.popup = this.saveMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		} else if (event.getSource() == this.importButton) {
			this.popup = this.importMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		} else if (event.getSource() == this.networkButton) {
			this.popup = this.networkMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		} else if (event.getSource() == this.aboutButton) {
			this.popup = this.helpMenu.getPopupMenu();
			this.popup.show(event.getComponent(), event.getX(), event.getY());
		}
	}

	public void mousePressed(MouseEvent event) { }

	public void mouseReleased(MouseEvent event) { }

	public void mouseEntered(MouseEvent event) { }

	public void mouseExited(MouseEvent event) { }

	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent)
			this.scene = ((SceneOpenEvent)event).getScene();
		else if (event instanceof SceneCloseEvent)
			this.scene = null;
	}

	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.newMenu.setEventHandler(handler);
		this.openMenu.setEventHandler(handler);
		this.saveMenu.setEventHandler(handler);
		this.importMenu.setEventHandler(handler);
		this.spanel.setEventHandler(handler);
		this.lpanel.setEventHandler(handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.newMenu);
			this.handler.addListener(this.openMenu);
			this.handler.addListener(this.saveMenu);
			this.handler.addListener(this.importMenu);
			this.handler.addListener(this.spanel);
			this.handler.addListener(this.lpanel);
		}
	}
	
	public EventHandler getEventHandler() { return this.handler; }
}
