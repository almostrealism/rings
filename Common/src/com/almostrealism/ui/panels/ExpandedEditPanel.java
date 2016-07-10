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

package com.almostrealism.ui.panels;


import java.util.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.almostrealism.ui.dialogs.*;
import com.almostrealism.util.*;

/**
 * An ExpandedEditPanel object can be used to allow a user to set the properties of a set of Editable objects.
 * 
 * @author Mike Murray
 */
public class ExpandedEditPanel extends JPanel implements DynamicDisplay {
  protected Set editing;
  private EditableFactory factory;
  
  protected EditPanel editPanel;
  private JPanel buttonPanel, listPanel;
  
  private JList editingList;
  private AbstractListModel editingListModel;
  
  private JButton addButton, removeButton;

  	protected ExpandedEditPanel(Set editing, EditableFactory factory, JPanel listPanel) {
  		super(new BorderLayout());
  		
  		this.listPanel = listPanel;
		this.buttonPanel = new JPanel(new GridLayout(1, 2));
		
		this.editingListModel = new AbstractListModel() {
			public Object getElementAt(int index) {
				if (getEditing() != null) {
					Object editingArray[] = getEditing().toArray();
					
					if (index < editingArray.length)
						return editingArray[index];
					else
						return null;
				} else {
					return null;
				}
			}
			
			public int getSize() {
				if (getEditing() != null)
					return getEditing().size();
				else
					return 0;
			}
		};
		
		super.add(this.listPanel, BorderLayout.WEST);
		
		this.editing = editing;
		this.setFactory(factory);
  	}
  	
	/**
	 * Constructs a new ExpandedEditPanel object using the Editable objects stored by the specified Set object.
	 * Objects to add to the set will be created using the specified EditableFactory object. Setting the factory
	 * to null will prevent support for adding to the set.
	 */
	public ExpandedEditPanel(Set editing, EditableFactory factory) {
		this(editing, factory, new JPanel(new BorderLayout()));
		
		this.editingList = new JList(this.editingListModel);
		this.editingList.setPrototypeCellValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		this.editingList.setFixedCellWidth(30);
		
		this.addButton = new JButton(" + ");
		this.removeButton = new JButton(" - ");
		
		this.buttonPanel.add(this.addButton);
		this.buttonPanel.add(this.removeButton);
		
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (getFactory() == null)
					return;
				
				AddDialog dialog = new AddDialog(getEditing(), ExpandedEditPanel.this, getFactory());
				dialog.setVisible(true);
			}
		});
		
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Object o = editingList.getSelectedValue();
				
				if (o != null && getEditing().remove(o)) {
					if (editPanel != null) editPanel.setEditing(null);
					
					updateDisplay();
				}
			}
		});
		
		this.listPanel.add(this.editingList, BorderLayout.CENTER);
		this.listPanel.add(this.buttonPanel, BorderLayout.SOUTH);
		
		this.editingList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				Editable selected = (Editable)editingList.getSelectedValue();
				
				if (editPanel == null) {
					editPanel = new EditPanel(selected);
					ExpandedEditPanel.this.add(new JScrollPane(editPanel), BorderLayout.CENTER);
					ExpandedEditPanel.this.validate();
				} else {
					editPanel.setEditing(selected);
				}
			}
		});
	}
	
	/**
	 * Sets the set of Editable objects that this panel object modifies to those stored by the specified Set object
	 * and updates the panel to reflect the change.
	 */
	public void setEditing(Set editing) {
		this.editing = editing;
		this.updateDisplay();
	}
	
	/**
	 * Sets the EditableFactory object that will be used to create bjects to add to the Set object stored
	 * by this panel. Setting the factory to null will prevent support for adding to the set.
	 */
	public void setFactory(EditableFactory factory) { this.factory = factory; }
	
	/**
	 * @return  The set of Editable objects that this panel modifies.
	 */
	public Set getEditing() { return this.editing; }
	
	/**
	 * @return  The EditableFactory object used by this panel.
	 */
	public EditableFactory getFactory() { return this.factory; }
	
	/**
	 * @return  The JPanel object used as the list panel for this panel.
	 */
	protected JPanel getListPanel() { return this.listPanel; }
	
	/**
	 * Updates the list of Editable objects displayed by this panel.
	 */
	public void updateDisplay() {
		if (this.getEditing() != null) {
			ListDataListener listeners[] = this.editingListModel.getListDataListeners();
			
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].contentsChanged(new ListDataEvent(this.editingListModel, ListDataEvent.CONTENTS_CHANGED, 0, this.getEditing().size()));
			}
		}
		
		if (this.editPanel != null) {
			this.editPanel.setEditing((Editable)this.editingList.getSelectedValue());
			this.editPanel.updateTable();
		}
	}
}
