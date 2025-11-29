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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class BooleanNode extends Node {
	private final JCheckBox box;
	
	public BooleanNode() {
		this.box = new JCheckBox();
	}
	
	public void setObject(Object o) {
		if (!(o instanceof Boolean)) return;
		this.box.setSelected(((Boolean)o).booleanValue());
	}
	
	public Object getObject() { return Boolean.valueOf(this.box.isSelected()); }
	
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
