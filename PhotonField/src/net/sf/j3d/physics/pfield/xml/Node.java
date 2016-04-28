/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.xml;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.sf.j3d.physics.pfield.Absorber;
import net.sf.j3d.physics.pfield.util.ProbabilityDistribution;
import net.sf.j3d.util.Nameable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Node {
	protected String name;
	protected Object obj;
	
	private PropertyDescriptor desc[];
	private Node nodes[];
	private boolean display[];
	
	private AbsorberSetNode parent;
	
	public Node() { }
	
	public Node(Object obj) throws IntrospectionException, IllegalArgumentException,
								IllegalAccessException, InvocationTargetException {
		this.setObject(obj);
	}
	
	protected void init() throws IntrospectionException, IllegalArgumentException,
								IllegalAccessException, InvocationTargetException {
		this.desc = Introspector.getBeanInfo(this.obj.getClass()).getPropertyDescriptors();
		this.nodes = new Node[this.desc.length];
		this.display = new boolean[this.desc.length];
		
		i: for (int i = 0; i < this.nodes.length; i++) {
			Method m = this.desc[i].getReadMethod();
			Method s = this.desc[i].getWriteMethod();
			
			if (m == null || s == null || this.desc[i].getName().equals("clock")) {
				this.display[i] = false;
				continue i;
			}
			
			this.display[i] = true;
			
			Class c = m.getReturnType();
			Object o = m.invoke(this.obj, new Object[0]);
			
			if (new double[0].getClass().isAssignableFrom(c)) {
				this.nodes[i] = new VectorNode();
			} else if (int.class.isAssignableFrom(c) ||
					double.class.isAssignableFrom(c) ||
						Number.class.isAssignableFrom(c)) {
				this.nodes[i] = new DecimalNode();
			} else if (boolean.class.isAssignableFrom(c) ||
						Boolean.class.isAssignableFrom(c)) {
				this.nodes[i] = new BooleanNode();
			} else if (o instanceof Class || o instanceof String) {
				this.display[i] = false;
				continue i;
			} else if (o != null) {
				this.nodes[i] = new Node();
			} else if (o == null) {
				continue i;
			} else {
				this.display[i] = false;
				continue i;
			}
			
			this.nodes[i].setName(this.name + this.desc[i].getName());
			this.nodes[i].setObject(o);
			if (this.parent != null) this.nodes[i].setParent(this.parent);
		}
	}
	
	public void setName(String name) { this.name = name; }
	public String getName() { return this.name; }
	
	public void setObject(Object o) throws IntrospectionException, IllegalArgumentException,
										IllegalAccessException, InvocationTargetException {
		this.obj = o;
		
		if (this.name == null && this.obj instanceof Nameable)
			this.setName(((Nameable)this.obj).getName());
		
		this.init();
	}
	
	public Object getObject() { return this.obj; }
	
	public Node getParent() { return this.parent; }
	public void setParent(AbsorberSetNode n) { this.parent = n; }
	
	public void listElements(Document doc, Element node, List l) {
		List propList = new ArrayList();
		
		i: for (int i = 0; i < this.nodes.length; i++) {
			if (!this.display[i]) continue i;
			
			Element el = doc.createElement("property");
			el.setAttribute("name", this.desc[i].getName());
			
			if (this.nodes[i] instanceof DecimalNode) {
				this.nodes[i].listElements(doc, el, propList);
			} else {
				Element n = doc.createElement("object");
				n.setAttribute("name", this.nodes[i].getName());
				this.nodes[i].listElements(doc, n, l);
				Element refNode = doc.createElement("reference");
				refNode.setNodeValue(this.nodes[i].getName());
				el.appendChild(refNode);
				propList.add(el);
			}
			
			node.appendChild(el);
		}
	}
	
	public void setProperty(String name, Node n) {
		for (int i = 0; i < this.desc.length; i++) {
			if (this.desc[i].getName().equals(name)) {
				this.setProperty(i, n);
				return;
			}
		}
	}
	
	public void setProperty(int index, Node n) { this.nodes[index] = n; }
	
	public NodeDisplay getDisplay() {
		if (this.obj instanceof ProbabilityDistribution)
			return new ProbabilityDistributionDisplay(this);
		
		PropertyListDisplay d = new PropertyListDisplay(this);
		
		if (this.parent != null && this.obj instanceof Absorber)
			d.setContainer(this.parent.getAbsorberContainer((Absorber)this.obj, this), null);
		
		i: for (int i = 0; i < this.desc.length; i++) {
			if (!this.display[i]) continue i;
			d.addProperty(this.desc[i].getName(), this.nodes[i]);
		}
		
		return d;
	}
}
