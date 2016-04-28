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

package net.sf.j3d.ui.dialogs;



import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.raytracer.surfaceUI.*;
import net.sf.j3d.ui.event.*;
import net.sf.j3d.ui.panels.*;

/**
  An AddTransformDialog allows the user to select a type of transformation
  to apply to an AbstractSurface object.
*/

public class AddTransformDialog extends JPanel implements Dialog, EventGenerator {
  private String transforms[] = {"Translate", "Scale", "Rotate-X", "Rotate-Y", "Rotate-Z"};
  
  private AbstractSurface surface;
  private DynamicDisplay display;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JComboBox transformationsBox;
  private JButton addButton;

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
