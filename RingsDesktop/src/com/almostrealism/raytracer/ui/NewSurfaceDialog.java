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

package com.almostrealism.raytracer.ui;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.almostrealism.swing.Dialog;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.dialogs.DialogCloseEvent;

import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;
import com.almostrealism.raytracer.surfaceUI.SurfaceUIFactory;

/**
  A NewSurfaceDialog object can be used to allow the user to add a new Surface object to a Scene object
  or a SurfaceGroup object.
*/

public class NewSurfaceDialog extends JPanel implements Dialog, EventGenerator {
  private String surfaceTypes[];
  
  private Scene scene;
  private SurfaceGroup group;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JComboBox surfaceTypesList;
  private JButton createButton;

	/**
	  Constructs a new NewSurfaceDialog object using the specified Scene object.
	*/
	
	public NewSurfaceDialog(Scene scene) {
		this.scene = scene;
		this.init();
	}
	
	/**
	  Constructs a new NewSurfaceDialog object using the specified SurfaceGroup object.
	*/
	
	public NewSurfaceDialog(SurfaceGroup group) {
		this.group = group;
		this.init();
	}
	
	/**
	  Used to initialize a NewSurfaceDialog object.
	*/
	
	public void init() {
		this.setLayout(new java.awt.FlowLayout());
		
		this.surfaceTypes = SurfaceUIFactory.getSurfaceTypeNames();
		this.surfaceTypesList = new JComboBox(this.surfaceTypes);
		this.createButton = new JButton("Create");
		
		this.add(this.surfaceTypesList);
		this.add(this.createButton);
		
		this.createButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				create();
			}
		});
		
		this.frame = new JFrame("Create New Surface");
		this.frame.setSize(300, 80);
		
		this.frame.getContentPane().add(this);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closeDialog();
			}
		});
	}
	
	/**
	  Shows this dialog in a JFrame.
	*/
	
	public void showDialog() {
		if (this.open == true)
			return;
		
		this.frame.setVisible(true);
		this.open = true;
	}
	
	/**
	  Closes (and disposes) this dialog if it is open.
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
	  Creates a new SurfaceUI object of the selected type.
	*/
	
	public void create() {
		SurfaceUI surface = SurfaceUIFactory.createSurfaceUI(this.surfaceTypesList.getSelectedIndex());
		
		this.closeDialog();
		
		if (this.scene != null)
			this.scene.addSurface(surface);
		else if (this.group != null)
			this.group.addSurface(surface);
		else
			return;
		
		if (this.handler != null) {
			this.handler.fireEvent(new SurfaceAddEvent(surface));
		}
		
//		EditSurfaceDialog editDialog = new EditSurfaceDialog(surface);
//		editDialog.setEventHandler(this.handler);
//		
//		if (this.handler != null) {
//			this.handler.addListener(editDialog);
//		}
//		
//		editDialog.showDialog();
	}
	
	/**
	  Sets the EventHandler object used by this NewSurfaceDialog object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this NewSurfaceDialog object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
