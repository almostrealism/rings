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

package net.sf.j3d.ui.dialogs;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.almostrealism.raytracer.shaders.TextureShader;
import com.almostrealism.texture.Texture;
import com.almostrealism.texture.TextureFactory;

import net.sf.j3d.util.EditableFactory;
import net.sf.j3d.ui.panels.DynamicDisplay;


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
  
  private JComboBox typesList;
  private JButton addButton;

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
