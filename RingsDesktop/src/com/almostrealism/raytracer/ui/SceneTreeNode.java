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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.MutableTreeNode;

import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;

/**
 * A SceneTreeNode object allows a Scene object to be represented by a TreeNode component
 * that can be added to a JTree component.
 * 
 * @author  Mike Murray
 */
public class SceneTreeNode implements MutableTreeNode, EventGenerator, EventListener {
  private Scene scene;
  private List children;
  
  private EventHandler handler;

	/**
	 * Constructs a new SceneTreeNode object using the specified Scene object.
	 */
	public SceneTreeNode(Scene scene) {
		this.scene = scene;
		this.children = new ArrayList();
		this.updateChildren();
	}
	
	/**
	 * Returns the Scene object stored by this SceneTreeNode object.
	 */
	public Scene getScene() { return this.scene; }
	
	/**
	 * Updates the children of this SceneTreeNode object so that they match the stored Scene object.
	 */
	public void updateChildren() {
		this.children.clear();
		int l = this.scene.getSurfaces().length;
		
		for(int i = 0; i < l; i++) {
			if (this.scene.getSurface(i) instanceof SurfaceUI)
				this.children.add(new SurfaceTreeNode((SurfaceUI)this.scene.getSurface(i), this));
		}
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
			this.updateChildren();
		}
		
		if (event instanceof SceneCloseEvent) {
			this.scene = null;
			this.children.clear();
		}
		
		if (event instanceof SurfaceAddEvent || event instanceof SurfaceRemoveEvent) {
			this.updateChildren();
		} else if (event instanceof SurfaceEditEvent && ((SurfaceEditEvent)event).isNameChangeEvent() == true) {
			this.updateChildren();
		}
	}
	
	/**
	 * @return  The children of this SceneTreeNode object as an Enumeration.
	 */
	public java.util.Enumeration children() {
		java.util.Enumeration en = new java.util.Enumeration() {
			private Iterator itr = SceneTreeNode.this.children.iterator();
			
			public boolean hasMoreElements() { return itr.hasNext(); }
			public Object nextElement() throws java.util.NoSuchElementException { return itr.next(); }
		};
		
		return en;
	}
	
	/**
	 * @return  The child of this SceneTreeNode object at the specified index.
	 */
	public javax.swing.tree.MutableTreeNode getChildAt(int index) { return (MutableTreeNode) this.children.get(index); }
	
	/**
	 * @return  The number of children contained by this SceneTreeNode object.
	 */
	public int getChildCount() { return this.children.size(); }
	
	/**
	 * @return  null (A SceneTreeNode is a root node and has no parent).
	 */
	public javax.swing.tree.TreeNode getParent() { return null; }
	
	/**
	 * @return  The index of the specified TreeNode object in this SceneTreeNode object's children.
	 *          If the specified TreeNode object is unmatched -1 will be returned.
	 */
	public int getIndex(javax.swing.tree.TreeNode node) { return this.children.indexOf(node); }
	
	/**
	 * @return  True (A SceneTreeNode always allows children).
	 */
	public boolean getAllowsChildren() { return true; }
	
	/**
	 * @return  False (A SceneTreeNode is never a leaf).
	 */
	public boolean isLeaf() { return false; }
	
	/**
	 * @return  "Scene".
	 */
	public String toString() { return "Scene"; }
	
	/**
	 * Does nothing.
	 */
	public void insert(MutableTreeNode node, int index) { }
	
	/**
	 * Removes the Surface object stored by the Scene backing this SceneTreeNode
	 * at the specified index and updates the children.
	 */
	public void remove(int index) {
		this.scene.removeSurface(index);
		this.updateChildren();
	}
	
	/**
	 * Does nothing.
	 */
	public void remove(MutableTreeNode node) { }
	
	/**
	 * Does nothing.
	 */
	public void setUserObject(Object o) { }
	
	/**
	 * Does nothing.
	 */
	public void removeFromParent() { }
	
	/**
	 * Does nothing.
	 */
	public void setParent(MutableTreeNode node) { }

	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		Iterator itr = this.children.iterator();
		while (itr.hasNext()) ((EventGenerator)itr.next()).setEventHandler(handler);
	}

	public EventHandler getEventHandler() { return this.handler; }
}
