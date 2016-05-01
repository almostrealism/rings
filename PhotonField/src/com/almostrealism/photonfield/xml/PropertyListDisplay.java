/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.xml;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PropertyListDisplay implements NodeDisplay, ActionListener {
	private Node node;
	private int width = 2, height = 0;
	private Container container, frame;
	private JPanel panel;
	private GridBagConstraints cons;
	
	public PropertyListDisplay(Node n) {
		this.node = n;
		this.panel = new JPanel(new GridBagLayout());
		this.cons = new GridBagConstraints();
		this.cons.anchor = GridBagConstraints.NORTHWEST;
		this.cons.fill = GridBagConstraints.HORIZONTAL;
		this.cons.weightx = 0.0;
		this.cons.weighty = 0.0;
		this.cons.gridy = 0;
	}
	
	public void addProperty(String name, Node n) {
		this.cons.gridx = 0;
		
		if (n == null) {
			this.cons.gridwidth = 1;
			this.cons.gridheight = 1;
			this.cons.weightx = 0.0;
			this.panel.add(new JLabel("  " + name), this.cons);
			
			this.cons.weightx = 1.0;
			this.cons.gridx = 1;
			
			JButton b = new JButton("Set");
			b.addActionListener(this);
			b.setActionCommand(name);
			this.height++;
			this.panel.add(b, this.cons);
		} else {
			NodeDisplay d = n.getDisplay();
			Container c = d.getContainer();
			if (c == null) return;
			
			this.cons.gridwidth = 1;
			this.cons.gridheight = d.getGridHeight();
			this.panel.add(new JLabel("  " + name), this.cons);
			
			this.cons.gridx = 1;
			this.cons.gridwidth = d.getGridWidth();
			if (this.cons.gridwidth > this.width - 1) this.width = 1 + this.cons.gridwidth;
			this.height += this.cons.gridheight;
			this.panel.add(c, this.cons);
		}
		
		this.cons.gridy = this.height;
	}
	
	public void setFrame(Container c) { this.frame = c;}
	public Container getFrame() { return this.frame; }
	
	public void setContainer(Container c, Object args) {
		this.container = c;
		if (c != null) {
			this.container.add(this.panel, args);
			int i = 0;
			while (c.getParent() instanceof Window == false && i++ < 5)
				c = c.getParent();
			this.setFrame(c);
		}
	}
	
	public Container getContainer() {
		if (this.height == 0) return null;
		
		if (this.container == null)
			return this.panel;
		else
			return this.container;
	}
	
	public void setGridHeight(int h) { this.height = h; }
	public void setGridWidth(int w) { this.width = w; }
	public int getGridHeight() { return this.height; }
	public int getGridWidth() { return this.width; }
	public Node getNode() { return this.node; }

	public void actionPerformed(ActionEvent event) {
		Node selected = null;
		
		// TODO  Get selected node...
		
		this.node.setProperty(event.getActionCommand(), selected);
	}
}
