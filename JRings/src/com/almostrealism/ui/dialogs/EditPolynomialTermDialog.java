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

package com.almostrealism.ui.dialogs;



import javax.swing.*;

import com.almostrealism.raytracer.primitives.*;
import com.almostrealism.ui.event.*;

/**
  An EditPolynomialTermDialog object can be used to allow the user to edit a PolynomialTerm object.
*/

public class EditPolynomialTermDialog extends JPanel implements Dialog, EventGenerator {
  private Polynomial polynomial;
  private PolynomialTerm term;
  
  private boolean open;
  
  private EventHandler handler;
  
  private JFrame frame;
  
  private JPanel dataPanel, buttonPanel;
  
  private JTextField coefficientField, xField, yField, zField;
  private JButton okButton, cancelButton;

	/**
	  Constructs a new EditPolynomialTermDialog object that can be used to edit the PolynomialTerm object
	  at the specified index of the specified Polynomial object.
	*/
	
	public EditPolynomialTermDialog(Polynomial polynomial, int index) {
		this.polynomial = polynomial;
		this.term = this.polynomial.getTerms()[index];
		
		this.dataPanel = new JPanel(new java.awt.GridLayout(4, 2));
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.coefficientField = new JTextField(6);
		this.xField = new JTextField(6);
		this.yField = new JTextField(6);
		this.zField = new JTextField(6);
		
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.frame = new JFrame("Edit Polynomial Term");
		this.frame.setSize(300, 150);
		
		this.dataPanel.add(new JLabel("Coefficient = "));
		this.dataPanel.add(this.coefficientField);
		this.dataPanel.add(new JLabel("Exponent Of X = "));
		this.dataPanel.add(this.xField);
		this.dataPanel.add(new JLabel("Exponent Of Y = "));
		this.dataPanel.add(this.yField);
		this.dataPanel.add(new JLabel("Exponent Of Z = "));
		this.dataPanel.add(this.zField);
		
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.dataPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
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
		
		this.updateAllFields();
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
		this.term.setCoefficient(Double.parseDouble(this.coefficientField.getText()));
		
		this.term.setExpOfX(Integer.parseInt(this.xField.getText()));
		this.term.setExpOfY(Integer.parseInt(this.yField.getText()));
		this.term.setExpOfZ(Integer.parseInt(this.zField.getText()));
		
		if (this.handler != null) {
			this.handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.dataChangeEvent, this.polynomial));
		}
	}
	
	/**
	  Updates all fields of this dialog so that they match the PolynomialTerm object being edited.
	*/
	
	public void updateAllFields() {
		this.coefficientField.setText(String.valueOf(this.term.getCoefficient()));
		
		this.xField.setText(String.valueOf(this.term.getExpOfX()));
		this.yField.setText(String.valueOf(this.term.getExpOfY()));
		this.zField.setText(String.valueOf(this.term.getExpOfZ()));
	}
	
	/**
	  Sets the EventHandler object used by this EditPolynomialTermDialog object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler used by this EditPolynomialTermDialog object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
