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


import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.almostrealism.ui.panels.*;
import com.almostrealism.util.*;

/**
  An EditVectorDialog object can be used to allow a user to specify
  the data for a Vector object.
*/
public class EditVectorDialog extends JFrame {
  private Vector vector;
  private DynamicDisplay display;
  
  private EditVectorPanel editPanel;
  private JPanel buttonPanel;
  private JButton okButton, cancelButton;

	/**
	  Constructs a new EditVectorDialog that can be used to edit
	  the specified Vector object. The dialog will update the
	  specified DynamicDisplay object when the Vector value
	  has been changed.
	*/
	
	public EditVectorDialog(Vector vector, DynamicDisplay display) {
		super("Edit Vector");
		
		this.vector = vector;
		this.display = display;
		
		this.editPanel = new EditVectorPanel(this.vector);
		
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
		Vector newVector = this.editPanel.getSelectedVector();
		
		this.vector.setX(newVector.getX());
		this.vector.setY(newVector.getY());
		this.vector.setZ(newVector.getZ());
		
		if (this.display != null)
			this.display.updateDisplay();
	}
}
