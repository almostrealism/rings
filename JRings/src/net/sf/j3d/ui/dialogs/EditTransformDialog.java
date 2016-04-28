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
import javax.swing.border.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.raytracer.surfaceUI.*;
import net.sf.j3d.run.Settings;
import net.sf.j3d.ui.event.*;
import net.sf.j3d.ui.panels.*;

/**
  An EditTransformDialog object allows the user to edit the transformation properties of an AbstractSurface object.
*/

public class EditTransformDialog extends JPanel implements Dialog, EventGenerator {
  private AbstractSurface surface;
  private int index;
  private DynamicDisplay display;
  
  private boolean open, canceled;
  
  private EventHandler handler;
  
  private JFrame frame;
  
  private JPanel dataPanel, buttonPanel;
  
  private JFormattedTextField xField, yField, zField;
  private JComboBox angleMeasureBox;
  private JButton okButton, cancelButton;

	/**
	  Constructs a new EditTransformDialog object that can be used to edit the transformation of the specified
	  AbstractSurface object at the specified index. The specified DynamicDisplay object will be updated
	  when changes are made to the surface.
	*/
	
	public EditTransformDialog(AbstractSurface surface, int index, DynamicDisplay display) {
		this.surface = surface;
		this.index = index;
		this.display = display;
		
		this.dataPanel = new JPanel(new java.awt.GridLayout(3, 2));
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.xField = new JFormattedTextField(Settings.decimalFormat);
		this.yField = new JFormattedTextField(Settings.decimalFormat);
		this.zField = new JFormattedTextField(Settings.decimalFormat);
		
		this.angleMeasureBox = new JComboBox(new String[] {"Radians", "Degrees"});
		this.angleMeasureBox.addItemListener(new java.awt.event.ItemListener() {
			int lastSelection = angleMeasureBox.getSelectedIndex();
			
			public void itemStateChanged(java.awt.event.ItemEvent event) {
				double rotateX = 0.0;
				double rotateY = 0.0;
				double rotateZ = 0.0;
				
				if (xField.getText().equals("") == false)
					rotateX = ((Number)xField.getValue()).doubleValue();
				
				if (yField.getText().equals("") == false)
					rotateY = ((Number)yField.getValue()).doubleValue();
				
				if (zField.getText().equals("") == false)
					rotateZ = ((Number)zField.getValue()).doubleValue();
				
				if (lastSelection == angleMeasureBox.getSelectedIndex())
					return;
				
				if (lastSelection == 1) {
					rotateX = Math.toRadians(rotateX);
					rotateY = Math.toRadians(rotateY);
					rotateZ = Math.toRadians(rotateZ);
					
					lastSelection = 0;
				} else if (lastSelection == 0) {
					rotateX = Math.toDegrees(rotateX);
					rotateY = Math.toDegrees(rotateY);
					rotateZ = Math.toDegrees(rotateZ);
					
					lastSelection = 1;
				}
				
				if (xField.getText().equals("") == false)
					xField.setValue(new Double(rotateX));
				
				if (yField.getText().equals("") == false)
					yField.setValue(new Double(rotateY));
				
				if (zField.getText().equals("") == false)
					zField.setValue(new Double(rotateZ));
			}
		});
		
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.frame = new JFrame("Edit Transform");
		
		TransformMatrixUI transform = (TransformMatrixUI)this.surface.getTransforms()[this.index];
		
		if (transform.getType() == TransformMatrixUI.translationTransformation ||
			transform.getType() == TransformMatrixUI.scaleTransformation) {
			this.dataPanel = new JPanel(new java.awt.GridLayout(3, 2));
			
			if (transform.getType() == TransformMatrixUI.translationTransformation)
				this.dataPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Translate By:"));
			else if (transform.getType() == TransformMatrixUI.scaleTransformation)
				this.dataPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Scale By:"));
			
			this.dataPanel.add(new JLabel("X = "));
			this.dataPanel.add(this.xField);
			this.dataPanel.add(new JLabel("Y = "));
			this.dataPanel.add(this.yField);
			this.dataPanel.add(new JLabel("Z = "));
			this.dataPanel.add(this.zField);
			
			this.frame.setSize(220, 160);
		} else if (transform.getType() == TransformMatrixUI.rotateXTransformation) {
			this.dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
			this.dataPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Rotate-X By:"));
			
			this.dataPanel.add(this.xField);
			this.dataPanel.add(this.angleMeasureBox);
			
			this.frame.setSize(175, 125);
		} else if (transform.getType() == TransformMatrixUI.rotateYTransformation) {
			this.dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
			this.dataPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Rotate-Y By:"));
			
			this.dataPanel.add(this.yField);
			this.dataPanel.add(this.angleMeasureBox);
			
			this.frame.setSize(175, 125);
		} else if (transform.getType() == TransformMatrixUI.rotateZTransformation) {
			this.dataPanel = new JPanel(new java.awt.GridLayout(1, 2));
			this.dataPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Rotate-Z By:"));
			
			this.dataPanel.add(this.zField);
			this.dataPanel.add(this.angleMeasureBox);
			
			this.frame.setSize(175, 125);
		}
		
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(dataPanel, java.awt.BorderLayout.CENTER);
		this.add(buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.frame.getContentPane().add(this);
		
		this.okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
				closeDialog();
			}
		});
		
		this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
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
		
		this.updateFields();
		
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
	  Applies the changes made in this dialog.
	*/
	
	public void apply() {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		
		if (this.xField.getText() != "")
			x = ((Number)this.xField.getValue()).doubleValue();
		
		if (this.yField.getText() != "")
			y = ((Number)this.yField.getValue()).doubleValue();
		
		if (this.zField.getText() != "")
			z = ((Number)this.zField.getValue()).doubleValue();
		
		if (this.angleMeasureBox.getSelectedIndex() == 1) {
			x = Math.toRadians(x);
			y = Math.toRadians(y);
			z = Math.toRadians(z);
		}
		
		this.surface.setTransform(this.index, new TransformMatrixUI(((TransformMatrixUI)this.surface.getTransforms()[this.index]).getType(), x, y, z));
		
		if (this.handler != null)
			this.handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.transformationChangeEvent, this.surface));
		
		if (this.display != null)
			this.display.updateDisplay();
	}
	
	/**
	  Updates the fields of this dialog so that they match the TransformMatrixUI object being edited.
	*/
	
	public void updateFields() {
		TransformMatrixUI transform = (TransformMatrixUI)this.surface.getTransforms()[this.index];
		
		this.xField.setValue(new Double(transform.getX()));
		this.yField.setValue(new Double(transform.getY()));
		this.zField.setValue(new Double(transform.getZ()));
	}
		
	/**
	  Sets the EventHandler object used by this EditTransformDialog object to the specified EventHandler object.
	  Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this EditTransformDialog object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
