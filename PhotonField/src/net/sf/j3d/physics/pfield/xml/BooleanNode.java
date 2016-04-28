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

import javax.swing.JCheckBox;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BooleanNode extends Node {
	private JCheckBox box;
	
	public BooleanNode() {
		this.box = new JCheckBox();
	}
	
	public void setObject(Object o) {
		if (o instanceof Boolean == false) return;
		this.box.setSelected(((Boolean)o).booleanValue());
	}
	
	public Object getObject() { return new Boolean(this.box.isSelected()); }
	
	public void listElements(Document doc, Element node, List l) {
		Element el = doc.createElement("boolean");
		if (super.name != null) el.setAttribute("name", super.name);
		el.setNodeValue(String.valueOf(this.box.isSelected()));
		
		if (node != null) {
			node.appendChild(el);
			el = node;
		}
		
		l.add(el);
	}
	
	public NodeDisplay getDisplay() {
		NodeDisplay display = new NodeDisplay() {
			public Container getContainer() { return BooleanNode.this.box; }
			public Container getFrame() { return null; }
			public int getGridHeight() { return 1; }
			public int getGridWidth() { return 1; }
			public Node getNode() { return BooleanNode.this; }
		};
		
		return display;
	}
}
