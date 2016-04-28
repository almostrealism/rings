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
import com.almostrealism.raytracer.lighting.*;
import net.sf.j3d.ui.dialogs.*;
import net.sf.j3d.ui.event.*;

/**
  A LightInfoPanel object allows access to a list of Light objects contained in the specified Scene object.
*/

public class LightInfoPanel extends JPanel implements EventListener, EventGenerator {
  private Scene scene;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  private JPanel buttonPanel;
  
  private LightListModel listModel;
  
  private JList lightList;
  private JScrollPane lightListScrollPane;
  
  private JButton newButton, editButton, removeButton;

	/**
	  Constructs a new LightInfoPanel object using the specified Scene object.
	*/

	public LightInfoPanel(Scene scn) {
		super(new java.awt.BorderLayout());
		
		this.scene = scn;
		
		this.listModel = new LightListModel(this.scene);
		
		this.lightList = new JList(this.listModel);
		this.lightListScrollPane = new JScrollPane(this.lightList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.newButton = new JButton("Create New Light");
		this.editButton = new JButton("Edit Light");
		this.removeButton = new JButton("Remove Light");
		
		this.buttonPanel = new JPanel(new java.awt.GridLayout(3, 1));
		
		this.buttonPanel.add(this.newButton);
		this.buttonPanel.add(this.editButton);
		this.buttonPanel.add(this.removeButton);
		
		this.add(this.lightListScrollPane, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.newButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (scene == null)
					return;
				
				NewLightDialog newDialog = new NewLightDialog(scene);
				
				newDialog.setEventHandler(handler);
				newDialog.showDialog();
			}
		});
		
		this.editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (scene == null)
					return;
				
				Light light = (Light)lightList.getSelectedValue();
				
				if (light == null)
					return;
				
				EditLightDialog editDialog = new EditLightDialog(light);
				
				editDialog.setEventHandler(handler);
				
				if (handler != null)
					handler.addListener(editDialog);
				
				editDialog.showDialog();
			}
		});
		
		this.removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (scene == null)
					return;
				
				int index = lightList.getSelectedIndex();
				
				if (index >= 0) {
					Light light = scene.getLight(index);
					
					scene.removeLight(index);
					
					handler.fireEvent(new LightRemoveEvent(light));
				}
			}
		});
		
		this.frame = new JFrame("Light Info");
		this.frame.setSize(200, 280);
		
		this.frame.getContentPane().add(this);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closePanel();
			}
		});
	}
	
	/**
	  Shows this panel in a JFrame.
	*/
	
	public void showPanel() {
		if (this.open == true) {
			this.frame.toFront();
		} else {
			this.frame.setVisible(true);
			this.open = true;
		}
	}
	
	/**
	  Closes this panel if it is open.
	*/
	
	public void closePanel() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
		}
	}
	
	/**
	  Repaints the Light list of this panel.
	*/
	
	public void updateLightList() {
		this.lightList.repaint();
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			SceneOpenEvent openEvent = (SceneOpenEvent)event;
			this.scene = openEvent.getScene();
			
			this.updateLightList();
			
			this.closePanel();
			return;
		}
		
		if (event instanceof SceneCloseEvent) {
			this.scene = null;
			
			this.closePanel();
			return;
		}
		
		if (event instanceof LightEditEvent) {
			this.updateLightList();
			return;
		}
		
		if (event instanceof LightAddEvent || event instanceof LightRemoveEvent) {
			this.updateLightList();
			return;
		}
	}
	
	/**
	  Sets the EventHandler object used by this LightInfoPanel object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		if (this.handler != null) {
			this.handler.addListener(this.listModel);
		}
	}
	
	/**
	  Returns the EventHandler object used by this LightInfoPanel object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
