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

package com.almostrealism.ui.dialogs;



import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.raytracer.lighting.*;
import com.almostrealism.ui.event.*;

/**
 * A NewLightDialog object can be used to allow the user to add a Light object to a Scene object.
 */
public class NewLightDialog extends JPanel implements Dialog, EventGenerator {
  private String lightTypes[] = {"Ambient Light", "Directional Ambient Light", "Point Light",
  								"Spherical Light", "Rectangular Light"};
  
  private Scene scene;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JComboBox lightTypesList;
  private JButton createButton;

	/**
	 * Constructs a new NewLightDialog object using the specified Scene object.
	 */
	public NewLightDialog(Scene scene) {
		this.scene = scene;
		
		this.setLayout(new java.awt.FlowLayout());
		
		this.lightTypesList = new JComboBox(this.lightTypes);
		this.createButton = new JButton("Create");
		
		this.add(this.lightTypesList);
		this.add(this.createButton);
		
		this.createButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				create();
			}
		});
		
		this.frame = new JFrame("Create New Light");
		this.frame.setSize(300, 80);
		
		this.frame.getContentPane().add(this);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closeDialog();
			}
		});
	}
	
	/**
	 * Shows this dialog in a JFrame.
	 */
	public void showDialog() {
		if (this.open == true)
			return;
		
		this.frame.setVisible(true);
		this.open = true;
	}
	
	/**
	 * Closes (and disposes) this dialog if it is open.
	 */
	public void closeDialog() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
			
			this.frame.dispose();
			
			if (this.handler != null)
				this.handler.fireEvent(new DialogCloseEvent(this));
		}
	}
	
	/**
	 * Creates a new Light object of the selected type.
	 */
	public void create() {
		Light light = null;
		
		if (this.lightTypesList.getSelectedItem() == this.lightTypes[0]) {
			light = new AmbientLight();
		} else if (this.lightTypesList.getSelectedItem() == this.lightTypes[1]) {
			light = new DirectionalAmbientLight();
		} else if (this.lightTypesList.getSelectedItem() == this.lightTypes[2]) {
			light = new PointLight();
		} else if (this.lightTypesList.getSelectedItem() == this.lightTypes[3]) {
			light = new SphericalLight();
		} else if (this.lightTypesList.getSelectedItem() == this.lightTypes[4]) {
			light = new RectangularLight();
		} else {
			return;
		}
		
		this.closeDialog();
		
		this.scene.addLight(light);
		
		if (this.handler != null) {
			this.handler.fireEvent(new LightAddEvent(light));
		}
		
		EditLightDialog editDialog = new EditLightDialog(light);
		
		editDialog.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(editDialog);
		}
		
		editDialog.showDialog();
	}
	
	/**
	 * Sets the EventHandler object used by this NewSurfaceDialog object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) { this.handler = handler; }
	
	/**
	 * Returns the EventHandler object used by this NewSurfaceDialog object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
