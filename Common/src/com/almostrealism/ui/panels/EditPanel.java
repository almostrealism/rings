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

import com.almostrealism.util.*;

/**
  An EditPanel object can be used to allow a user to set the properties
  of an Editable object.
*/

public class EditPanel extends JTable {
  private Editable editing;
  private EditablePropertiesTableModel tableModel;

	/**
	  Constructs a new EditPanel object that can be used to modify
	  the specified Editable object.
	*/
	
	public EditPanel(Editable editing) {
		this.tableModel = new EditablePropertiesTableModel(null);
		super.setModel(this.tableModel);
		super.setDefaultEditor(Object.class, new ExtendedCellEditor());
		super.setDefaultRenderer(Object.class, new ExtendedCellRenderer());
		
		this.setEditing(editing);
	}
	
	/**
	  Sets the Editable object that this panel modifies and updates
	  the table to reflect the change.
	*/
	
	public void setEditing(Editable editing) {
		this.editing = editing;
		
		this.tableModel.setEditing(this.editing);
		this.updateTable();
	}
	
	/**
	  Returns the Editable object that this panel modifies.
	*/
	
	public Editable getEditing() {
		return this.editing;
	}
	
	/**
	  Updates the table displayed by this EditPanel object.
	*/
	
	public void updateTable() {
		for (int i = 0; i < super.getModel().getRowCount(); i++)
			super.setRowHeight(i, 45);
		
		super.doLayout();
	}
}
