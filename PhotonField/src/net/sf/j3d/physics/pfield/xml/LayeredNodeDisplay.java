/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.xml;

import java.awt.Container;

import javax.swing.JDesktopPane;
import javax.swing.JScrollPane;

public class LayeredNodeDisplay extends JDesktopPane implements NodeDisplay {
	private Node node;
	private int w = 8, h = 10;
	public LayeredNodeDisplay(Node n) { this.node = n; }
	public Container getContainer() { return new JScrollPane(this); }
	public Container getFrame() { return null; }
	public void setGridHeight(int h) { this.h = h; }
	public void setGridWidth(int w) { this.w = w; }
	public int getGridHeight() { return this.h; }
	public int getGridWidth() { return this.w; }
	public Node getNode() { return this.node; }
}
