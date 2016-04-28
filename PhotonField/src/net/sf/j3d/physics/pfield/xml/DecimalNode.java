/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.xml;

import java.awt.Container;
import java.util.List;

import javax.swing.JFormattedTextField;

import net.sf.j3d.run.Settings;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DecimalNode extends Node {
	private Number value;
	private JFormattedTextField field;
	
	public DecimalNode() {
		this.field = new JFormattedTextField(Settings.decimalFormat);
		this.field.setColumns(6);
	}
	
	public void setObject(Object o) {
		this.value = (Number) o;
		if (this.value == null) this.value = new Double(0.0);
		this.field.setValue(this.value);
	}
	
	public Object getObject() { return this.field.getValue(); }
	
	public void listElements(Document doc, Element node, List l) {
		Number o = (Number) this.getObject();
		Element el = doc.createElement("decimal");
		if (super.name != null) el.setAttribute("name", super.name);
		el.setNodeValue(o.toString());
		
		if (node != null) {
			node.appendChild(el);
			el = node;
		}
		
		l.add(el);
	}
	
	public NodeDisplay getDisplay() {
		NodeDisplay display = new NodeDisplay() {
			public Container getContainer() { return DecimalNode.this.field; }
			public Container getFrame() { return null; }
			public int getGridHeight() { return 1; }
			public int getGridWidth() { return 1; }
			public Node getNode() { return DecimalNode.this; }
		};
		
		return display;
	}
}
