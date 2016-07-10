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

package com.almostrealism.ui.panels;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.surfaceUI.AbstractSurfaceUI;
import com.almostrealism.ui.dialogs.Dialog;
import com.almostrealism.ui.event.Event;
import com.almostrealism.ui.event.EventGenerator;
import com.almostrealism.ui.event.EventHandler;
import com.almostrealism.ui.event.EventListener;
import com.almostrealism.ui.event.SceneCloseEvent;
import com.almostrealism.ui.event.SceneOpenEvent;
import com.almostrealism.ui.event.SurfaceEditEvent;
import com.almostrealism.ui.event.SurfaceEvent;
import com.almostrealism.ui.event.SurfaceRemoveEvent;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;


/**
 * An EditSurfacePanel object can be used to gather input from a user
 * to specify the settings for an AbstractSurfaceUI object and the
 * underlying AbstractSurface object.
 * 
 * @author Mike Murray
 */
public class EditSurfacePanel extends JPanel implements EventListener, EventGenerator {
  private AbstractSurfaceUI surface;
  
  private EventHandler handler;
  
  private JPanel idPanel, dataPanel, buttonPanel;
  private JPanel sizePanel, shadePanel;
  private EditVectorPanel locationPanel;
  private EditRGBPanel colorPanel;
  
  private JLabel typeLabel;
  private JTextField nameField;
  private JFormattedTextField sizeField;
  
  private JCheckBox shadeFrontOption, shadeBackOption;
  
  private JButton applyButton;
  private JButton editButton;

	/**
	 * Constructs a new EditSurfacePanel object that can be used to modify the specified AbstractSurfaceUI object.
	 */
	public EditSurfacePanel(AbstractSurfaceUI s) {
		super(new BorderLayout());
		
		GridBagLayout gb = new GridBagLayout();
		
		this.idPanel = new JPanel(new FlowLayout());
		this.dataPanel = new JPanel(gb);
		this.buttonPanel = new JPanel(new FlowLayout());
		
		this.locationPanel = new EditVectorPanel();
		this.locationPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Location"));
		
		this.colorPanel = new EditRGBPanel();
		this.colorPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Color"));
		
		this.sizePanel = new JPanel(new java.awt.FlowLayout());
		this.sizePanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Size"));
		
		this.shadePanel = new JPanel(new GridLayout(0, 1));
		
		this.typeLabel = new JLabel();
		this.nameField = new JTextField(10);
		this.sizeField = new JFormattedTextField(Settings.decimalFormat);
		this.sizeField.setColumns(6);
		this.shadeFrontOption = new JCheckBox("Shade front");
		this.shadeBackOption = new JCheckBox("Shade back");
		
		this.applyButton = new JButton("Apply");
		this.editButton = new JButton("Edit...");
		
		this.idPanel.add(new JLabel("Name: "));
		this.idPanel.add(this.nameField);
		this.idPanel.add(new JLabel("Type: "));
		this.idPanel.add(this.typeLabel);
		
		this.sizePanel.add(new JLabel("Size = "));
		this.sizePanel.add(this.sizeField);
		
		this.shadePanel.add(this.shadeFrontOption);
		this.shadePanel.add(this.shadeBackOption);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		c.gridheight = 4;
		gb.setConstraints(this.locationPanel, c);
		this.dataPanel.add(this.locationPanel);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(this.colorPanel, c);
		this.dataPanel.add(this.colorPanel);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridheight = 1;
		gb.setConstraints(this.sizePanel, c);
		this.dataPanel.add(this.sizePanel);
		
		gb.setConstraints(this.shadePanel, c);
		this.dataPanel.add(this.shadePanel);
		
		this.buttonPanel.add(this.applyButton);
		this.buttonPanel.add(this.editButton);
		
		this.add(this.idPanel, java.awt.BorderLayout.NORTH);
		this.add(this.dataPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.setSurface(s);
		
		this.applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
			}
		});
		
		this.editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (surface == null)
					return;
				
				if (surface.hasDialog() == true) {
					Dialog editDialog = surface.getDialog();
					
					if (editDialog instanceof EventGenerator) {
						((EventGenerator)editDialog).setEventHandler(handler);
					}
					
					if (handler != null && editDialog instanceof EventListener) {
						handler.addListener((EventListener)editDialog);
					}
					
					editDialog.showDialog();
				}
			}
		});
	}
	
	/**
	 * Sets the AbstractSurfaceUI object that this EditSurfacePanel object modifies and
	 * updates the fields to reflect the change.
	 */
	public void setSurface(AbstractSurfaceUI surface) {
		this.surface = surface;
		
		if (this.surface != null) {
			if (this.surface.hasDialog() == true)
				this.buttonPanel.add(this.editButton);
			
			this.updateAllFields();
		}
	}
	
	/**
	 * Returns the AbstractSurfaceUI object stored by this EditSurfacePanel object.
	 */
	public AbstractSurfaceUI getSurface() { return this.surface; }
	
	/**
	 * Applies the changes made in this panel to the AbstractSurfaceUI object being edited
	 * and fires the necessary events if the current EventHandler is not set to null.
	 */
	public void apply() {
		int eventCode = 0;
		
		if (this.nameField.getText().equals(this.surface.getName()) == false) {
			this.surface.setName(this.nameField.getText());
			eventCode += SurfaceEditEvent.nameChangeEvent;
		}
		
		Vector newLocation = this.locationPanel.getSelectedVector();
		
		if (((AbstractSurface) this.surface.getSurface()).getLocation().equals(newLocation) == false) {
			((AbstractSurface) this.surface.getSurface()).setLocation(newLocation);
			eventCode += SurfaceEditEvent.locationChangeEvent;
		}
		
		double size = Double.parseDouble(this.sizeField.getText());
		
		if (size != ((AbstractSurface) this.surface.getSurface()).getSize()) {
			((AbstractSurface) this.surface.getSurface()).setSize(size);
			eventCode += SurfaceEditEvent.sizeChangeEvent;
		}
		
		RGB color = this.colorPanel.getSelectedColor();
		
		if (color.equals(((AbstractSurface) this.surface.getSurface()).getColor()) == false) {
			((AbstractSurface) this.surface.getSurface()).setColor(color);
			eventCode += SurfaceEditEvent.colorChangeEvent;
		}
		
		if (this.shadeFrontOption.isSelected() != this.surface.getSurface().getShadeFront() ||
			this.shadeBackOption.isSelected() != this.surface.getSurface().getShadeBack()) {
			((AbstractSurface) this.surface.getSurface()).setShadeFront(this.shadeFrontOption.isSelected());
			((AbstractSurface) this.surface.getSurface()).setShadeBack(this.shadeBackOption.isSelected());
			eventCode += SurfaceEditEvent.shadingOptionChangeEvent;
		}
		
		if (this.handler != null) {
			this.handler.fireEvent(new SurfaceEditEvent(eventCode, this.surface));
		}
		
		this.updateAllFields();
	}
	
	/**
	 * Updates all fields of this panel so that they match the settings of the AbstractSurfaceUI object being edited.
	 * If the AbstractSurfaceUI object stored by this panel is null this method will do nothing.
	 */
	public void updateAllFields() {
		if (this.surface == null)
			return;
		
		this.updateTypeField();
		this.updateNameField();
		this.updateLocationFields();
		this.updateColorFields();
		this.updateSizeField();
		this.updateShadeFields();
	}
	
	/**
	 * Updates the type field of this panel so that it matches the type of the AbstractSurfaceUI object being edited.
	 */
	public void updateTypeField() { this.typeLabel.setText(this.surface.getType()); }
	
	/**
	 * Updates the name field of this panel so that it matches the name of the AbstractSurfaceUI object being edited.
	 */
	public void updateNameField() { this.nameField.setText(this.surface.getName()); }
	
	/**
	 * Updates the location fields of this panel so that they match the location values of the AbstractSurfaceUI
	 * object being edited.
	 */
	public void updateLocationFields() {
		this.locationPanel.setSelectedVector(((AbstractSurface) this.surface.getSurface()).getLocation());
	}
	
	/**
	 * Updates the color fields of this panel so that they match the color values of the AbstractSurfaceUI object being edited.
	 */
	public void updateColorFields() {
		this.colorPanel.setSelectedColor(((AbstractSurface) this.surface.getSurface()).getColor());
	}
	
	/**
	 * Updates the size field of this panel so that it matches the size of the AbstractSurfaceUI object being edited.
	 */
	public void updateSizeField() {
		this.sizeField.setText(String.valueOf(((AbstractSurface) this.surface.getSurface()).getSize()));
	}
	
	/**
	 * Updates the shading options fields of this panel so that they match the shading options of the AbstractSurfaceUI object being edited.
	 */
	public void updateShadeFields() {
		this.shadeFrontOption.setSelected(this.surface.getSurface().getShadeFront());
		this.shadeBackOption.setSelected(this.surface.getSurface().getShadeBack());
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SurfaceEvent && ((SurfaceEvent)event).getTarget() != this.surface)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent || event instanceof SurfaceRemoveEvent) {
			this.setVisible(false);
			return;
		}
		
		if (event instanceof SurfaceEditEvent) {
			SurfaceEditEvent editEvent = (SurfaceEditEvent)event;
			
			if (editEvent.isNameChangeEvent()) {
				this.updateNameField();
			}
			
			if (editEvent.isLocationChangeEvent()) {
				this.updateLocationFields();
			}
			
			if (editEvent.isSizeChangeEvent()) {
				this.updateSizeField();
			}
			
			if (editEvent.isColorChangeEvent()) {
				this.updateColorFields();
			}
			
			if (editEvent.isShadingOptionChangeEvent()) {
				this.updateShadeFields();
			}
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditSurfacePanel object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) { this.handler = handler; }
	
	/**
	 * Returns the EventHandler object used by this EditSurfacePanel object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
