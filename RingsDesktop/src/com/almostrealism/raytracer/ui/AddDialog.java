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

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import org.almostrealism.swing.DynamicDisplay;
import org.almostrealism.texture.Texture;
import org.almostrealism.texture.TextureFactory;
import org.almostrealism.util.EditableFactory;

import com.almostrealism.rayshade.TextureShader;

/**
 * An AddDialog object can be used to allow a user to add an Editable object to a specified Set object.
 * 
 * @author Mike Murray
 */
public class AddDialog extends JFrame {
  private static DynamicDisplay lastDisplay;
  
  private Collection set;
  private DynamicDisplay display;
  private EditableFactory factory;
  
  private JButton addButton;
  private JComboBox typesList;

	/**
	 * Constructs a new AddDialog object that can be used to add to the specified Set object.
	 * This AddDialog object will update the specified DynamicDisplay object when necessary
	 * and will also use the specified EditableFactor object to construct Editable objects.
	 */
	public AddDialog(Collection c, DynamicDisplay display, EditableFactory factory) {
		super("Add...");
		super.setSize(300, 80);
		super.getContentPane().setLayout(new FlowLayout());
		
		this.set = c;
		this.display = display;
		this.factory = factory;
		
		if (this.display != null) AddDialog.lastDisplay = this.display;
		
		this.typesList = new JComboBox(this.factory.getTypeNames());
		this.addButton = new JButton("Add");
		
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				add();
				setVisible(false);
			}
		});
		
		super.getContentPane().add(this.typesList);
		super.getContentPane().add(this.addButton);
	}
	
	/**
	 * Constructs a new Editable object of the type specified by the current selection in the combo box
	 * on this dialog, adds it to the Set object stored by this AddDialog object, and updates the
	 * DynamicDisplay object stored by this AddDialog.
	 */
	public void add() {
		if (this.set instanceof TextureShader && this.factory instanceof TextureFactory)
			((TextureShader)this.set).setTexture((Texture)this.factory.constructObject(this.typesList.getSelectedIndex()));
		else
			this.set.add(this.factory.constructObject(this.typesList.getSelectedIndex()));
		
		if (this.display != null)
			this.display.updateDisplay();
		else if (AddDialog.lastDisplay != null)
			AddDialog.lastDisplay.updateDisplay();
	}
}
