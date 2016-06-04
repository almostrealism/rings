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



import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.almostrealism.raytracer.graphics.*;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.ui.panels.*;

/**
  An EditRGBDialog object can be used to allow a user to specify
  the data for a RGB object.
*/

public class EditRGBDialog extends JFrame {
  private RGB color;
  private DynamicDisplay display;
  
  private EditRGBPanel editPanel;
  private JPanel buttonPanel;
  private JButton okButton, cancelButton;

	/**
	  Constructs a new EditRGBDialog that can be used to edit
	  the specified RGB object. The dialog will update the
	  specified DynamicDisplay object when the RGB value
	  has been changed.
	*/
	
	public EditRGBDialog(RGB color, DynamicDisplay display) {
		super("Edit RGB");
		
		this.color = color;
		this.display = display;
		
		this.editPanel = new EditRGBPanel(this.color);
		
		this.buttonPanel = new JPanel(new FlowLayout());
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		super.getContentPane().add(this.editPanel, BorderLayout.CENTER);
		super.getContentPane().add(this.buttonPanel, BorderLayout.SOUTH);
		
		super.setSize(170, 160);
		
		this.okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				apply();
				setVisible(false);
			}
		});
		
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				setVisible(false);
			}
		});
	}
	
	/**
	  Applies the changes made in this dialog.
	*/
	
	public void apply() {
		RGB newColor = this.editPanel.getSelectedColor();
		
		this.color.setRed(newColor.getRed());
		this.color.setGreen(newColor.getGreen());
		this.color.setBlue(newColor.getBlue());
		
		if (this.display != null)
			this.display.updateDisplay();
	}
}
