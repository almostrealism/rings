package com.almostrealism.ui;

import javax.swing.tree.TreeNode;

public interface WebTreeNode<T> extends TreeNode, Iterable<T> {
	/**
	 * Return the name of a field which will contain children
	 * when this object is mapped to JSON or XML. This prevents
	 * cluttering the exported data with duplicate data for
	 * children when children are already being mapped to a
	 * JSON/XML field.
	 */
	public String getChildrenFieldName();
	
	public String getHREF();
}
