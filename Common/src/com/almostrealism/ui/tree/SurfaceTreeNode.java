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
* Copyright (C) 2004-06  Mike Murray
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.swing.tree.MutableTreeNode;

import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.raytracer.surfaceUI.AbstractSurfaceUI;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;
import com.almostrealism.ui.event.EventGenerator;
import com.almostrealism.ui.event.EventHandler;
import com.almostrealism.ui.event.SurfaceEditEvent;


/**
 * A SurfaceTreeNode object allows SurfaceUI objects to be represented
 * by TreeNode components that can be added to a JTree component.
 * 
 * @author Mike Murray
 */
public class SurfaceTreeNode implements TreeNode, EventGenerator {
  private SurfaceUI surface;
  
  private TreeNode parent;
  
  private boolean hasChildren;
  private SurfaceTreeNode children[];
  
  private EventHandler handler;

	/**
	 * Constructs a new SurfaceTreeNode object using the specified SurfaceUI object with the specified parent.
	 */
	public SurfaceTreeNode(SurfaceUI surface, TreeNode parent) {
		this.surface = surface;
		this.parent = parent;
		
		this.updateChildren();
	}
	
	/**
	 * Returns the SurfaceUI object stored by this SurfaceTreeNode object.
	 */
	public SurfaceUI getSurface() { return this.surface; }
	
	/**
	 * Updates the children of this SurfaceTreeNode object so that they match the stored SurfaceUI object.
	 */
	public void updateChildren() {
		if (this.surface instanceof SurfaceGroup || this.surface.getSurface() instanceof SurfaceGroup) {
			
			SurfaceGroup group = null;
			
			if (this.surface instanceof SurfaceGroup)
				group = (SurfaceGroup)this.surface;
			else
				group = (SurfaceGroup)((AbstractSurfaceUI)this.surface).getSurface();
			
			if (this.surface instanceof Mesh || this.surface.getSurface() instanceof Mesh)
			    this.hasChildren = false;
			else
			    this.hasChildren = true;
			
			this.children = new SurfaceTreeNode[group.getSurfaces().length];
			
			for(int i = 0; i < this.children.length; i++) {
				if (group.getSurface(i) instanceof SurfaceUI)
					this.children[i] = new SurfaceTreeNode((SurfaceUI)group.getSurface(i), this);
			}
		} else {
			this.hasChildren = false;
		}
	}
	
	/**
	 * @return  The children of this SurfaceTreeNode object as an Enumeration.
	 */
	public Enumeration children() {
		Enumeration nodeEnum = new Enumeration() {
			private int currentIndex = 0;
			private SurfaceTreeNode nodes[] = children;
			
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
					SurfaceTreeNode next = this.nodes[currentIndex];
					this.currentIndex++;
					return next;
				}
			}
		};
		
		return nodeEnum;
	}
	
	/**
	 * Returns the child of this SurfaceTreeNode object at the specified index.
	 */
	public javax.swing.tree.TreeNode getChildAt(int index) { return this.children[index]; }
	
	/**
	 * Returns the number of children contained by this SurfaceTreeNode object.
	 */
	public int getChildCount() { return this.children.length; }
	
	/**
	 * Returns the parent of this SurfaceTreeNode object.
	 */
	public javax.swing.tree.TreeNode getParent() { return this.parent; }
	
	/**
	 * Returns the index of the specified TreeNode object in this SurfaceTreeNode object's children.
	 * If the specified TreeNode object is unmatched -1 will be returned.
	 */
	public int getIndex(javax.swing.tree.TreeNode node) {
		for (int i = 0; i < this.children.length; i++) {
			if (this.children[i] == node)
				return i;
		}
		
		return -1;
	}
	
	/**
	 * @return  True if this SurfaceTreeNode object allows children.
	 */
	public boolean getAllowsChildren() { return this.hasChildren; }
	
	/**
	 * @return  True if this SurfaceTreeNode object has no children.
	 */
	public boolean isLeaf() { return !this.hasChildren; }
	
	/**
	 * @return  The name of the SurfaceUI object represented by this SurfaceTreeNode object.
	 */
	public String toString() { return this.surface.getName(); }

	public void insert(MutableTreeNode node, int index) {
		System.out.println("SurfaceTreeNode.insert(" + node + ", " + index + ")");
		
		if (!this.hasChildren || node instanceof SurfaceTreeNode == false) return;
		
		SurfaceGroup group = null;
		
		if (this.surface instanceof SurfaceGroup)
			group = (SurfaceGroup)this.surface;
		else
			group = (SurfaceGroup)((AbstractSurfaceUI)this.surface).getSurface();
		
		group.addSurface(((SurfaceTreeNode)node).getSurface());
	}
	
	/**
	 * Does nothing.
	 */
	public void remove(int index) {
		System.out.println("SurfaceTreeNode.remove(" + index + ")");
	}
	
	/**
	 * Does nothing.
	 */
	public void remove(MutableTreeNode node) {
		System.out.println("SurfaceTreeNode.remove(" + node + ")");
	}
	
	/**
	 * Sets the name of the SurfaceUI object stored to the specified value, if it is a String.
	 */
	public void setUserObject(Object o) {
		if (o instanceof String) {
			this.surface.setName((String)o);
			
			if (this.handler != null)
				this.handler.fireEvent(
						new SurfaceEditEvent(SurfaceEditEvent.nameChangeEvent, this.surface));
		}
	}
	
	public void removeFromParent() { System.out.println("SurfaceTreeNode.removeFromParent"); }
	
	public void setParent(MutableTreeNode node) { this.parent = (TreeNode) node; }

	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		for (int i = 0; i < this.children.length; i++) this.children[i].setEventHandler(handler);
	}

	public EventHandler getEventHandler() { return this.handler; }
}
