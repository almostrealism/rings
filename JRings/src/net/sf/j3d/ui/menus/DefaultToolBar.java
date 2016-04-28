/*
 * Copyright (C) 2005  Mike Murray
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.Scene;
import net.sf.j3d.ui.dialogs.EditCameraDialog;
import net.sf.j3d.ui.dialogs.RenderOptionsDialog;
import net.sf.j3d.ui.event.Event;
import net.sf.j3d.ui.event.EventGenerator;
import net.sf.j3d.ui.event.EventHandler;
import net.sf.j3d.ui.event.EventListener;
import net.sf.j3d.ui.event.SceneCloseEvent;
import net.sf.j3d.ui.event.SceneOpenEvent;
import net.sf.j3d.ui.panels.LightInfoPanel;
import net.sf.j3d.ui.panels.RenderPanel;
import net.sf.j3d.ui.panels.SurfaceInfoPanel;


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
