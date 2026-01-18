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

package com.almostrealism.photon.xml;

import com.almostrealism.raytracer.Settings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DecimalNode extends Node {
	private Number value;
	private final JFormattedTextField field;
	
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
