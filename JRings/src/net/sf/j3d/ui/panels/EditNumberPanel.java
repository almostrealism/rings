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
import javax.swing.*;

/**
  An EditNumberPanel object can be used to specify a number.
*/

public class EditNumberPanel extends JPanel {
  private JTextField numberField;

	/**
	  Consturcts an EditNumberPanel object labeled "Value" and initial value set to 0.0.
	*/
	
	public EditNumberPanel() {
		this("Value", 0.0);
	}
	
	/**
	  Constructs an EditNumberPanel object with the specified label and initial value set to 0.0.
	*/
	
	public EditNumberPanel(String label) {
		this(label, 0.0);
	}
	
	/**
	  Constructs an EditNumberPanel object with the specified label and initial value.
	*/
	
	public EditNumberPanel(String label, double d) {
		super(new FlowLayout());
		
		this.numberField = new JTextField(6);
		
		this.numberField.setText(String.valueOf(d));
		
		this.add(new JLabel(label + " = "));
		this.add(this.numberField);
	}
	
	/**
	  Returns the number selected by this EditNumberPanel object as a double value.
	*/
	
	public double getSelectedNumber() {
		double d = Double.parseDouble(this.numberField.getText());
		
		return d;
	}
}
