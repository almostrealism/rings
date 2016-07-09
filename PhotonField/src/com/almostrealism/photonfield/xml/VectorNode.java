package com.almostrealism.photonfield.xml;

import java.awt.Container;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.almostrealism.ui.panels.EditVectorPanel;
import com.almostrealism.util.Vector;

public class VectorNode extends Node {
	private double[] value;
	private EditVectorPanel panel;
	
	public VectorNode() {
		this.panel = new EditVectorPanel();
	}
	
	public void setObject(Object o) {
		this.value = (double[]) o;
		if (this.value == null) this.value = new double[3];
		
		this.panel.setSelectedVector(
					new Vector(this.value[0], this.value[1], this.value[2]));
	}
	
	public Object getObject() {
		if (this.value == null) this.value = new double[3];
		
		Vector v = this.panel.getSelectedVector();
		this.value[0] = v.getX();
		this.value[1] = v.getY();
		this.value[2] = v.getZ();
		
		return this.value;
	}
	
	public void listElements(Document doc, Element node, List l) {
		double v[] = (double[]) this.getObject();
		Element el = doc.createElement("vector");
		el.setAttribute("name", super.name);
		el.setAttribute("x", String.valueOf(v[0]));
		el.setAttribute("y", String.valueOf(v[1]));
		el.setAttribute("y", String.valueOf(v[2]));
		
		if (node != null) {
			node.appendChild(el);
			el = node;
		}
		
		l.add(el);
	}
	
	public NodeDisplay getDisplay() {
		NodeDisplay display = new NodeDisplay() {
			public Container getContainer() { return VectorNode.this.panel; }
			public Container getFrame() { return null; }
			public int getGridHeight() { return 3; }
			public int getGridWidth() { return 2; }
			public Node getNode() { return VectorNode.this; }
		};
		
		return display;
	}
}
