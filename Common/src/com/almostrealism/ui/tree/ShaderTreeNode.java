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

package com.almostrealism.ui.tree;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.tree.TreeNode;

import com.almostrealism.raytracer.shaders.Shader;
import com.almostrealism.raytracer.shaders.ShaderSet;
import com.almostrealism.util.Editable;
import com.almostrealism.util.Producer;


/**
 * A ShaderTreeNode object wraps a Shader object and allows it to be displayed
 * in a tree.
 * 
 * @author Mike Murray
 */
public class ShaderTreeNode implements Editable, TreeNode, List {
  private Shader shader;
  private TreeNode parent;
  private ShaderTreeNode children[];

  	/**
  	 * Constructs a new ShaderTreeNode object.
  	 * 
  	 * @param s  Shader object to use.
  	 */
	public ShaderTreeNode(Shader s, TreeNode parent) {
		this.shader = s;
		this.parent = parent;
		
		this.updateChildren();
	}
	
	/**
	 * @return  The Shader object stored by this ShaderTreeNode object.
	 */
	public Shader getShader() { return this.shader; }
	
	/**
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	public int getChildCount() {
		if (this.shader instanceof ShaderSet)
			return this.children.length;
		else
			return 0;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() { return (this.shader instanceof ShaderSet); }

	/**
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() { return !(this.shader instanceof ShaderSet); }

	/**
	 * @see javax.swing.tree.TreeNode#children()
	 */
	public Enumeration children() {
		Enumeration nodeEnum = new Enumeration() {
			private int currentIndex = 0;
			private ShaderTreeNode nodes[] = ShaderTreeNode.this.children;
			
			public boolean hasMoreElements() {
				if (this.currentIndex < nodes.length)
					return true;
				else
					return false;
			}
			
			public Object nextElement() throws NoSuchElementException {
				if (this.hasMoreElements() == false) {
					throw new NoSuchElementException();
				} else {
					Shader next = this.nodes[currentIndex].getShader();
					this.currentIndex++;
					return new ShaderTreeNode(next, ShaderTreeNode.this);
				}
			}
		};
		
		return nodeEnum;
	}

	/**
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	public TreeNode getParent() { return this.parent; }
	
	/**
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int index) {
		if (this.shader instanceof ShaderSet)
			return this.children[index];
		else
			return null;
	}
	
	/**
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	public int getIndex(TreeNode node) {
		if (this.shader instanceof ShaderSet) {
			Enumeration e = this.children();
			
			for (int i = 0; e.hasMoreElements(); i++) if (e.nextElement().equals(node)) return i;
			
			return -1;
		} else {
			return -1;
		}
	}
	
	/**
	 * Updates the list of child nodes stored by this ShaderTreeNode object.
	 */
	public void updateChildren() {
		if (this.shader instanceof ShaderSet) {
			ShaderSet set = (ShaderSet)this.shader;
			
			this.children = new ShaderTreeNode[set.size()];
			Iterator itr = set.iterator();
			
			for (int i = 0; itr.hasNext(); i++)
				this.children[i] = new ShaderTreeNode((Shader)itr.next(), this);
		} else {
			this.children = null;
		}
	}
	
	/**
	 * @see com.almostrealism.util.Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() {
		if (this.shader instanceof Editable)
			return ((Editable)this.shader).getPropertyNames();
		else
			return new String[0];
	}
	
	/**
	 * @see com.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() {
		if (this.shader instanceof Editable)
			return ((Editable)this.shader).getPropertyDescriptions();
		else
			return new String[0];
	}
	
	/**
	 * @see com.almostrealism.util.Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() {
		if (this.shader instanceof Editable)
			return ((Editable)this.shader).getPropertyTypes();
		else
			return new Class[0];
	}

	/**
	 * @see com.almostrealism.util.Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() {
		if (this.shader instanceof Editable)
			return ((Editable)this.shader).getPropertyValues();
		else
			return new Object[0];
	}

	/**
	 * @see com.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object value, int index) {
		if (this.shader instanceof Editable)
			((Editable)this.shader).setPropertyValue(value, index);
	}
	
	/**
	 * @see com.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object[] values) {
		if (this.shader instanceof Editable)
			((Editable)this.shader).setPropertyValues(values);
	}
	
	public boolean equals(Object o) {
		if (o instanceof ShaderTreeNode) {
			return (((ShaderTreeNode)o).getShader() == this.shader);
		} else {
			return false;
		}
	}
	
	public int hashCode() { return this.shader.hashCode(); }
	
	public String toString() {
		if (this.shader == null)
			return "NULL";
		else
			return this.shader.toString();
	}

	/**
	 * @return  this.getChildCount().
	 */
	public int size() { return this.getChildCount(); }

	/**
	 * @see java.util.List#isEmpty()
	 */
	public boolean isEmpty() { return !(this.getChildCount() > 0); }

	/**
	 * @see java.util.List#contains(java.lang.Object)
	 */
	public boolean contains(Object o) {
		Enumeration c = this.children();
		
		while (c.hasMoreElements()) if (c.nextElement() == o) return true;
		
		return false;
	}

	/**
	 * @see java.util.List#iterator()
	 */
	public Iterator iterator() {
		return null;
	}

	/**
	 * @see java.util.List#toArray()
	 */
	public Object[] toArray() { return this.children; }

	/**
	 * @return  this.toArray().
	 */
	public Object[] toArray(Object[] o) { return this.children; }

	/**
	 * @see java.util.List#add(java.lang.Object)
	 */
	public boolean add(Object o) {
		if (this.shader instanceof Set) {
			if (!((Set)this.shader).add(o)) return false;
			
			ShaderTreeNode nc[] = new ShaderTreeNode[this.children.length + 1];
			for (int i = 0; i < this.children.length; i++) nc[i] = this.children[i];
			nc[nc.length - 1] = new ShaderTreeNode((Shader)o, this);
			
			this.children = nc;
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see java.util.List#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		Enumeration c = this.children();
		
		for (int i = 0; c.hasMoreElements(); i++) {
			if (((ShaderTreeNode)c.nextElement()).getShader() == o) {
				if (!((Set)this.shader).remove(o)) return false;
				
				ShaderTreeNode nc[] = new ShaderTreeNode[this.children.length - 1];
				for (int j = 0; j < i; j++) { nc[j] = this.children[j]; }
				for (int j = i + 1; j < nc.length; j++) { nc[j - 1] = this.children[j]; }
				
				this.children = nc;
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @see java.util.List#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection c) {
		return ((Set)this.shader).containsAll(c);
	}

	/**
	 * @see java.util.List#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection c) {
		Iterator itr = c.iterator();
		
		boolean changed = false;
		while(itr.hasNext()) if (this.add(itr.next())) changed = true;
		
		return changed;
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 * @return  false.
	 */
	public boolean addAll(int index, Collection c) { return false; }

	/**
	 * @see java.util.List#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection c) {
		Iterator itr = c.iterator();
		
		boolean changed = false;
		while(itr.hasNext()) if (this.remove(itr.next())) changed = true;
		
		return changed;
	}

	/**
	 * @see java.util.List#retainAll(java.util.Collection)
	 * @return  false.
	 */
	public boolean retainAll(Collection c) { return false; }

	/**
	 * @see java.util.List#clear()
	 */
	public void clear() {
		((Set)this.shader).clear();
		this.updateChildren();
	}

	/**
	 * @see java.util.List#get(int)
	 */
	public Object get(int index) { return this.children[index].getShader(); }

	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(int index, Object o) {
		Shader removed = this.children[index].getShader();
		
		if (!((Set)this.shader).remove(removed)) return null;
		if (!((Set)this.shader).add(o)) return removed;
		
		this.children[index] = new ShaderTreeNode((Shader)o, this);
		
		return removed;
	}

	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, Object o) {
		if (this.shader instanceof Set) {
			if (!((Set)this.shader).add(o)) return;
			
			ShaderTreeNode nc[] = new ShaderTreeNode[this.children.length + 1];
			for (int i = 0; i < index; i++) nc[i] = this.children[i];
			for (int i = index; i < this.children.length; i++) nc[i + 1] = this.children[i];
			nc[index] = new ShaderTreeNode((Shader)o, this);
			
			this.children = nc;
		}
	}

	/**
	 * @see java.util.List#remove(int)
	 */
	public Object remove(int index) {
		Shader s = (Shader)this.get(index);
		
		if (!((Set)this.shader).remove(s)) return null;
		
		ShaderTreeNode nc[] = new ShaderTreeNode[this.children.length - 1];
		for (int i = 0; i < index; i++) { nc[i] = this.children[i]; }
		for (int i = index + 1; i < nc.length; i++) { nc[i - 1] = this.children[i]; }
		
		this.children = nc;
		
		return s;
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		for (int i = 0; i < this.children.length; i++)
			if (this.children[i].getShader().equals(o)) return i;
		
		return -1;
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		for (int i = this.children.length - 1; i > 0; i++)
			if (this.children[i].getShader().equals(o)) return i;
		
		return -1;
	}

	/**
	 * @see java.util.List#listIterator()
	 * @return  null.
	 */
	public ListIterator listIterator() {
		return null;
	}

	/**
	 * @see java.util.List#listIterator(int)
	 * @return  null.
	 */
	public ListIterator listIterator(int i) { return null; }

	/**
	 * @see java.util.List#subList(int, int)
	 * @return  null.
	 */
	public List subList(int start, int end) { return null; }

	/**
	 * @see com.almostrealism.util.Editable#getInputPropertyValues()
	 */
	public Producer[] getInputPropertyValues() {
		if (this.shader instanceof Editable)
			return ((Editable)this.shader).getInputPropertyValues();
		else
			return new Producer[0];
	}

	/**
	 * @see com.almostrealism.util.Editable#setInputPropertyValue(int, com.almostrealism.util.Producer)
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (this.shader instanceof Editable) ((Editable)this.shader).setInputPropertyValue(index, p);
	}
}
