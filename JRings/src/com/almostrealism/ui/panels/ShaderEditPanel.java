/*
* Copyright (C) 2005  Mike Murray
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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.shaders.Shader;
import com.almostrealism.raytracer.shaders.ShaderSet;
import com.almostrealism.ui.dialogs.AddDialog;
import com.almostrealism.ui.displays.ShadedPreviewDisplay;
import com.almostrealism.ui.tree.ShaderTreeNode;
import com.almostrealism.util.Editable;
import com.almostrealism.util.EditableFactory;


/**
 * A ShaderEditPanel object can be used to allow a user to set the editable properties
 * of members of a shader tree (assuming that they implement the Editable interface).
 * 
 * @author Mike Murray
 */
public class ShaderEditPanel extends ExpandedEditPanel {
  private JPanel editingTreePanel;
  private JTree editingTree;
  
  private ShadedPreviewDisplay previewPanel;
  
  private JPanel buttonPanel;
  private JButton addButton, removeButton;
  
	
  	/**
  	 * Constructs a new ShaderEditPanel object.
  	 * 
  	 * @param editing  The ShaderSet object containing the Shader objects to be edited.
  	 * @param factory  The EditableFactory object that will be used to construct
  	 *                  new elements to add to the ShaderSet.
  	 */
	public ShaderEditPanel(ShaderSet editing, EditableFactory factory) {
		super(editing, factory, new JPanel(new BorderLayout()));
		
		this.editingTreePanel = new JPanel(new BorderLayout());
		this.editingTree = new JTree(new DefaultTreeModel(new ShaderTreeNode(editing, null)));
		
		this.buttonPanel = new JPanel(new GridLayout(1, 2));
		
		this.addButton = new JButton(" + ");
		this.removeButton = new JButton(" - ");
		
		this.buttonPanel.add(this.addButton);
		this.buttonPanel.add(this.removeButton);
		
		this.editingTreePanel.add(this.editingTree, BorderLayout.CENTER);
		this.editingTreePanel.add(this.buttonPanel, BorderLayout.SOUTH);
		
		this.previewPanel = new ShadedPreviewDisplay();
		
		this.addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (getFactory() == null)
					return;
				
				List l = (List)ShaderEditPanel.this.editingTree.getLastSelectedPathComponent();
				if (l == null) l = (List)ShaderEditPanel.this.editingTree.getModel().getRoot();
				
				AddDialog dialog = new AddDialog(l, ShaderEditPanel.this, ShaderEditPanel.super.getFactory());
				dialog.setVisible(true);
			}
		});
		
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ShaderTreeNode s = (ShaderTreeNode)ShaderEditPanel.this.editingTree.getLastSelectedPathComponent();
				
				if (s != null && s.getShader() != null && s.getParent() != null &&
						((Set)((ShaderTreeNode)s.getParent()).getShader()).remove(s.getShader())) {
					if (ShaderEditPanel.super.editPanel != null) ShaderEditPanel.super.editPanel.setEditing(null);
					
					updateDisplay();
				}
			}
		});
		
		this.editingTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent event) {
				Editable selected = (Editable)ShaderEditPanel.this.editingTree.getLastSelectedPathComponent();
				
				if (editPanel == null) {
					editPanel = new EditPanel(selected);
					ShaderEditPanel.this.add(new JScrollPane(editPanel), BorderLayout.CENTER);
					ShaderEditPanel.this.validate();
				} else {
					editPanel.setEditing(selected);
				}
			}
		});
		
		super.getListPanel().add(this.editingTreePanel, BorderLayout.CENTER);
		super.getListPanel().add(this.previewPanel, BorderLayout.NORTH);
	}
	/**
	 * Sets the set of Editable objects that this panel object modifies to those stored by the specified Set
	 * object and updates the panel to reflect the change.
	 */
	public void setEditing(Set editing) {
		super.editing = editing;
		this.updateDisplay();
	}
	
	public void setSurface(Surface s) { this.previewPanel.setSurface(s); }
	
	/**
	 * Updates the tree of Editable objects displayed by this panel.
	 */
	public void updateDisplay() {
		if (this.editingTree == null || this.editingTree.getModel() == null) return;
		
		DefaultTreeModel m = (DefaultTreeModel)this.editingTree.getModel();
		m.setRoot(new ShaderTreeNode((Shader)super.editing, null));
		m.nodeStructureChanged((javax.swing.tree.TreeNode)m.getRoot());
		
		this.editingTree.revalidate();
		this.editingTree.repaint();
		
		if (super.editPanel != null) {
			super.editPanel.setEditing((Editable)this.editingTree.getLastSelectedPathComponent());
			super.editPanel.updateTable();
		}
	}
}
