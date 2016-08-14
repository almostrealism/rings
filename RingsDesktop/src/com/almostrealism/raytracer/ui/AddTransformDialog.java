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

import org.almostrealism.ui.Dialog;
import org.almostrealism.ui.DynamicDisplay;
import org.almostrealism.ui.EventGenerator;
import org.almostrealism.ui.EventHandler;
import org.almostrealism.ui.dialogs.DialogCloseEvent;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.surfaceUI.TransformMatrixUI;

/**
 * An AddTransformDialog allows the user to select a type of transformation
 * to apply to an AbstractSurface object.
 */
public class AddTransformDialog extends JPanel implements Dialog, EventGenerator {
  private static String transforms[] = {"Translate", "Scale", "Rotate-X", "Rotate-Y", "Rotate-Z"};
  
  private AbstractSurface surface;
  private DynamicDisplay display;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JButton addButton;
  private JComboBox transformationsBox;

	/**
	  Constructs a new AddTransformDialog object using the specified AbstractSurface object.
	  The dialog will update the specified DynamicDisplay object when changes are made
	  to the surface.
	*/
	
	public AddTransformDialog(AbstractSurface surface, DynamicDisplay display) {
		super(new java.awt.FlowLayout());
		
		this.surface = surface;
		this.display = display;
		
		this.transformationsBox = new JComboBox(this.transforms);
		this.addButton = new JButton("Add");
		
		this.add(this.transformationsBox);
		this.add(this.addButton);
		
		this.frame = new JFrame("Add Transformation");
		this.frame.setSize(300, 80);
		
		this.frame.getContentPane().add(this);
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closeDialog();
			}
		});
		
		this.addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
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
	  Adds the transform specified by the user to the Surface object stored by this AddTransformDialog object.
	*/
	
	public void apply() {
		switch(this.transformationsBox.getSelectedIndex()) {
			case 0:
				this.surface.addTransform(new TransformMatrixUI(TransformMatrixUI.translationTransformation, 0, 0, 0)); break;
			case 1:
				this.surface.addTransform(new TransformMatrixUI(TransformMatrixUI.scaleTransformation, 1, 1, 1)); break;
			case 2:
				this.surface.addTransform(new TransformMatrixUI(TransformMatrixUI.rotateXTransformation, 0)); break;
			case 3:
				this.surface.addTransform(new TransformMatrixUI(TransformMatrixUI.rotateYTransformation, 0)); break;
			case 4:
				this.surface.addTransform(new TransformMatrixUI(TransformMatrixUI.rotateZTransformation, 0)); break;
		}
		
		if (this.handler != null)
			this.handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.transformationChangeEvent, this.surface));
		
		if (this.display != null)
			this.display.updateDisplay();
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
	  Sets the EventHandler object used by this AddTransformDialog object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this AddTransformDialog object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
