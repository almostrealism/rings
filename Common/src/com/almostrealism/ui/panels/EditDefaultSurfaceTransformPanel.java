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

package com.almostrealism.ui.panels;



import javax.swing.*;
import javax.swing.border.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.ui.event.*;

/**
  An EditDefaultSurfaceTransformPanel can be used to gather input from the user
  to specifiy scaling and rotation properties of an AbstractSurface object.
*/

public class EditDefaultSurfaceTransformPanel extends JPanel implements EventListener, EventGenerator {
  private AbstractSurface surface;
  
  private EventHandler handler;
  
  private JPanel buttonPanel;
  
  private JPanel transformationPropertiesPanel;
  private JPanel scalePropertiesPanel, rotationPropertiesPanel;
  private JTextField scaleXField, scaleYField, scaleZField;
  private JTextField rotateXField, rotateYField, rotateZField;
  private JComboBox rotationMeasurementBox;
  
  private JButton applyButton;

	/**
	  Constructs a new EditDefaultSurfaceTransformPanel object that can be used to modify the specified AbstractSurface object.
	*/
	
	public EditDefaultSurfaceTransformPanel(AbstractSurface surface) {
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.transformationPropertiesPanel = new JPanel(new java.awt.FlowLayout());
		this.transformationPropertiesPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Transformations"));
		
		this.scalePropertiesPanel = new JPanel(new java.awt.GridLayout(3, 2));
		this.scalePropertiesPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Scale"));
		
		this.rotationPropertiesPanel = new JPanel(new java.awt.GridLayout(4, 2));
		this.rotationPropertiesPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Rotate"));
		
		this.scaleXField = new JTextField(6);
		this.scaleYField = new JTextField(6);
		this.scaleZField = new JTextField(6);
		
		this.rotateXField = new JTextField(6);
		this.rotateYField = new JTextField(6);
		this.rotateZField = new JTextField(6);
		
		String measurements[] = {"Radians", "Degrees"};
		this.rotationMeasurementBox = new JComboBox(measurements);
		
		this.applyButton = new JButton("Apply");
		
		this.scalePropertiesPanel.add(new JLabel("Scale X = "));
		this.scalePropertiesPanel.add(this.scaleXField);
		this.scalePropertiesPanel.add(new JLabel("Scale Y = "));
		this.scalePropertiesPanel.add(this.scaleYField);
		this.scalePropertiesPanel.add(new JLabel("Scale Z = "));
		this.scalePropertiesPanel.add(this.scaleZField);
		
		this.rotationPropertiesPanel.add(new JLabel("Rotate X = "));
		this.rotationPropertiesPanel.add(this.rotateXField);
		this.rotationPropertiesPanel.add(new JLabel("Rotate Y = "));
		this.rotationPropertiesPanel.add(this.rotateYField);
		this.rotationPropertiesPanel.add(new JLabel("Rotate Z = "));
		this.rotationPropertiesPanel.add(this.rotateZField);
		this.rotationPropertiesPanel.add(this.rotationMeasurementBox);
		
		this.transformationPropertiesPanel.add(this.scalePropertiesPanel);
		this.transformationPropertiesPanel.add(this.rotationPropertiesPanel);
		
		this.buttonPanel.add(this.applyButton);
		
		this.add(this.transformationPropertiesPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.setSurface(surface);
		
		this.rotationMeasurementBox.addItemListener(new java.awt.event.ItemListener() {
			int lastSelection = rotationMeasurementBox.getSelectedIndex();
			
			public void itemStateChanged(java.awt.event.ItemEvent event) {
				double rotateX = Double.parseDouble(rotateXField.getText());
				double rotateY = Double.parseDouble(rotateYField.getText());
				double rotateZ = Double.parseDouble(rotateZField.getText());
				
				if (lastSelection == rotationMeasurementBox.getSelectedIndex())
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
				
				rotateXField.setText(String.valueOf(rotateX));
				rotateYField.setText(String.valueOf(rotateY));
				rotateZField.setText(String.valueOf(rotateZ));
			}
		});
		
		this.applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
			}
		});
	}
	
	/**
	  Sets the AbstractSurface object that this EditDefaltSurfaceTransformPanel object modifies
	  and updates the fields to reflect the change.
	*/
	
	public void setSurface(AbstractSurface surface) {
		this.surface = surface;
		this.updateAllFields();
	}
	
	/**
	  Returns the AbstractSurface object stored by this EditDefaultSurfaceTransformPanel object.
	*/
	
	public AbstractSurface getSurface() {
		return this.surface;
	}
	
	/**
	  Applies the changes made in this panel to the AbstractSurface object being edited and fires the necessary events
	  if the current EventHandler is not set to null.
	*/
	
	public void apply() {
		int eventCode = 0;
		
		double scaleX = Double.parseDouble(this.scaleXField.getText());
		double scaleY = Double.parseDouble(this.scaleYField.getText());
		double scaleZ = Double.parseDouble(this.scaleZField.getText());
		
		if (scaleX != this.surface.getScaleCoefficients()[0] ||
			scaleY != this.surface.getScaleCoefficients()[1] ||
			scaleZ != this.surface.getScaleCoefficients()[2]) {
			this.surface.setScaleCoefficients(scaleX, scaleY, scaleZ);
			
			eventCode += SurfaceEditEvent.scaleCoefficientChangeEvent;
		}
		
		double rotateX = Double.parseDouble(this.rotateXField.getText());
		double rotateY = Double.parseDouble(this.rotateYField.getText());
		double rotateZ = Double.parseDouble(this.rotateZField.getText());
		
		if (rotationMeasurementBox.getSelectedIndex() == 1) {
			rotateX = Math.toRadians(rotateX);
			rotateY = Math.toRadians(rotateY);
			rotateZ = Math.toRadians(rotateZ);
		}
		
		if (rotateX != this.surface.getRotationCoefficients()[0] ||
			rotateY != this.surface.getRotationCoefficients()[1] ||
			rotateZ != this.surface.getRotationCoefficients()[2]) {
			this.surface.setRotationCoefficients(rotateX, rotateY, rotateZ);
			
			eventCode += SurfaceEditEvent.rotationCoefficientChangeEvent;
		}
		
		if (this.handler != null && eventCode != 0) {
			this.handler.fireEvent(new SurfaceEditEvent(eventCode, this.surface));
		}
	}
	
	/**
	  Updates all fields of this panel so that they match the AbstractSurface object being edited.
	  If the AbstractSurface object stored by this panel is null this method will do nothing.
	*/
	
	public void updateAllFields() {
		if (this.surface == null)
			return;
		
		this.updateScalePropertiesFields();
		this.updateRotationPropertiesFields();
	}
	
	/**
	  Updates the scale properties fields of this panel so that they match the AbstractSurface object being edited.
	*/
	
	public void updateScalePropertiesFields() {
		this.scaleXField.setText(String.valueOf(this.surface.getScaleCoefficients()[0]));
		this.scaleYField.setText(String.valueOf(this.surface.getScaleCoefficients()[1]));
		this.scaleZField.setText(String.valueOf(this.surface.getScaleCoefficients()[2]));
	}
	
	/**
	  Updates the rotation properties fields of this panel so that they match the AbstractSurface object being edited.
	*/
	
	public void updateRotationPropertiesFields() {
		double rotationCoefficients[] = this.surface.getRotationCoefficients();
		
		if (this.rotationMeasurementBox.getSelectedIndex() == 1) {
			rotationCoefficients[0] = Math.toDegrees(rotationCoefficients[0]);
			rotationCoefficients[1] = Math.toDegrees(rotationCoefficients[1]);
			rotationCoefficients[2] = Math.toDegrees(rotationCoefficients[2]);
		}
		
		this.rotateXField.setText(String.valueOf(rotationCoefficients[0]));
		this.rotateYField.setText(String.valueOf(rotationCoefficients[1]));
		this.rotateZField.setText(String.valueOf(rotationCoefficients[2]));
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SurfaceEvent && ((SurfaceEvent)event).getTarget() != this.surface)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent) {
			this.setVisible(false);
			return;
		}
		
		if (event instanceof SurfaceEditEvent) {
			SurfaceEditEvent editEvent = (SurfaceEditEvent)event;
			
			if (editEvent.isScaleCoefficientChangeEvent()) {
				this.updateScalePropertiesFields();
			}
			
			if (editEvent.isRotationCoefficientChangeEvent()) {
				this.updateRotationPropertiesFields();
			}
		}
	}
	
	/**
	  Sets the EventHandler object used by this EditDefaultSurfaceTransformPanel object.
	  Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this EditDefaultSurfaceTransformPanel object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
