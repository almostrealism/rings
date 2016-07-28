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

package io.almostrealism.tree.ui;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.almostrealism.photonfield.util.Length;

public class ObjectTreeNode implements TreeNode, Length {
	private ObjectTreeNode parent;
	private Method method;
	private Object obj, target;
	private List children;
	private boolean leaf, arrayWrap = true;
	private Object arrayType[];
	
	public ObjectTreeNode(ObjectTreeNode parent, Method m) {
		this.parent = parent;
		this.leaf = false;
		this.method = m;
		this.children = new ArrayList();
	}
	
	public ObjectTreeNode(ObjectTreeNode parent, Object o, boolean leaf) {
		if (leaf)
			this.obj = o;
		else if (o instanceof Method)
			this.method = (Method) o;
		
		this.parent = parent;
		this.leaf = leaf;
		this.children = new ArrayList();
	}
	
	public Object getObject() throws IllegalArgumentException,
									IllegalAccessException,
									InvocationTargetException,
									InstantiationException {
		if (this.leaf) {
			return this.obj;
		} else if (this.method != null) {
			return this.method.invoke(this.getTarget(), this.getChildObjects());
		} else {
			return this.getChildObjects();
		}
	}
	
	public void setTarget(Object target) { this.target = target; }
	
	protected Object getTarget() {
		if (this.target != null)
			return this.target;
		else if (this.parent != null)
			return this.parent.getTarget();
		else
			return null;
	}
	
	public void addChildObject(Object o) {
		this.children.add(new ObjectTreeNode(this, o, true));
	}
	
	public void addChildMethod(Method m) {
		this.children.add(new ObjectTreeNode(this, m));
	}
	
	public Object[] getChildObjects() throws IllegalArgumentException,
									IllegalAccessException,
									InvocationTargetException,
									InstantiationException {
		List l = new ArrayList();
		Iterator itr = this.children.iterator();
		
		while (itr.hasNext())
			l.add(((ObjectTreeNode)itr.next()).getObject());
		
		if (this.getArrayType() == null) {
			this.arrayType = (Object[])
				Array.newInstance(this.method.getParameterTypes()[0].getComponentType(), 0);
		}
		
		if (this.arrayWrap)
			return new Object[] {l.toArray(this.arrayType)};
		else
			return l.toArray(this.arrayType);
	}
	
	public void setArrayType(Object o[]) { this.arrayType = o; }
	
	public Object[] getArrayType() {
		if (this.arrayType != null)
			return this.arrayType;
		else if (this.parent != null)
			return this.parent.getArrayType();
		else
			return null;
	}
	
	public Enumeration children() {
		Enumeration en = new Enumeration() {
			Iterator itr = children.iterator();
			public boolean hasMoreElements() { return this.itr.hasNext(); }
			public Object nextElement() { return itr.next(); }
		};
		
		return en;
	}

	public boolean getAllowsChildren() { return !this.leaf; }
	public TreeNode getChildAt(int index) { return (TreeNode) this.children.get(index); }
	public int getChildCount() { return this.children.size(); }
	public int getIndex(TreeNode node) { return this.children.indexOf(node); }
	public TreeNode getParent() { return this.parent; }
	public boolean isLeaf() { return this.leaf; }
	
	public double getMultiplier() {
		if (this.obj instanceof Length)
			return ((Length) this.obj).getMultiplier();
		else
			return 1.0;
	}
	
	public void setMultiplier(double m) {
		if (this.obj instanceof Length)
			((Length) this.obj).setMultiplier(m);
	}
	
	public String toString() {
		if (this.obj != null)
			return this.obj.toString();
		else if (this.method != null)
			return this.method.getName();
		else
			return "null";
	}
}
