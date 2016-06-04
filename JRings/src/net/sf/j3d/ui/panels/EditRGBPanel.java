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



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.almostrealism.raytracer.graphics.*;
import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.run.Settings;

/**
  An EditRGBPanel object can be used to specify an RGB color.
*/

public class EditRGBPanel extends JPanel {
  private JFormattedTextField redField, greenField, blueField;
  private JButton selectColorButton;

	/**
	  Constructs a new EditRGBPanel object with the initial values set to 0.0 (black).
	*/
	
	public EditRGBPanel() {
		this(new RGB(0.0, 0.0, 0.0));
	}
	
	/**
	  Constructs a new EditRGBPanel object with the initial values set to those of the specified RGB object.
	*/
	
	public EditRGBPanel(RGB color) {
		super(new GridLayout(0, 2));
		
		this.redField = new JFormattedTextField(Settings.decimalFormat);
		this.greenField = new JFormattedTextField(Settings.decimalFormat);
		this.blueField = new JFormattedTextField(Settings.decimalFormat);
		
		this.redField.setColumns(6);
		this.greenField.setColumns(6);
		this.blueField.setColumns(6);
		
		FocusListener listener = new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				JTextField field = (JTextField)event.getSource();
				field.setSelectionStart(0);
				field.setSelectionEnd(field.getText().length());
			}
		};
		
		this.redField.addFocusListener(listener);
		this.greenField.addFocusListener(listener);
		this.blueField.addFocusListener(listener);
		
		this.setSelectedColor(color);
		
		this.selectColorButton = new JButton("Select...");
		
		this.selectColorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				double r = ((Double)redField.getValue()).doubleValue();
				double g = ((Double)greenField.getValue()).doubleValue();
				double b = ((Double)blueField.getValue()).doubleValue();
				
				Color currentColor = GraphicsConverter.convertToAWTColor(new RGB(r, g, b));
				
				java.awt.Color newColor = JColorChooser.showDialog(null, "Select Color", currentColor);
				
				if (newColor != null) {
					RGB newRGB = GraphicsConverter.convertToRGB(newColor);
					setSelectedColor(newRGB);
				} else {
					return;
				}
			}
		});
		
		this.add(new JLabel("Red: "));
		this.add(this.redField);
		this.add(new JLabel("Green: "));
		this.add(this.greenField);
		this.add(new JLabel("Blue: "));
		this.add(this.blueField);
		this.add(this.selectColorButton);
	}
	
	/**
	  Updates the fields of this EditRGBPanel object to display the values for the color represented by the specified RGB object.
	*/
	
	public void setSelectedColor(RGB color) {
		this.redField.setValue(new Double(color.getRed()));
		this.greenField.setValue(new Double(color.getGreen()));
		this.blueField.setValue(new Double(color.getBlue()));
	}
	
	/**
	  Returns the color selected by this EditRGBPanel object as an RGB object.
	*/
	
	public RGB getSelectedColor() {
		double r = ((Number)this.redField.getValue()).doubleValue();
		double g = ((Number)this.greenField.getValue()).doubleValue();
		double b = ((Number)this.blueField.getValue()).doubleValue();
		
		RGB color = new RGB(r, g, b);
		
		return color;
	}
}
