/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package net.sf.j3d.ui.displays;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

/**
 * @author Mike Murray
 */
public class GraphDisplay extends JPanel {
	private int scale = 4;
	
	private int maxEntries;
	private List entries = new ArrayList();

	public GraphDisplay() { this(1000); }
	public GraphDisplay(int maxEntries) { this.maxEntries = maxEntries; }
	
	public void addEntry(int i) {
		this.entries.add(new Integer(i));
		if (this.entries.size() > this.maxEntries) this.entries.remove(0);
	}
	
	public void paint(Graphics g) {
		int i = 0;
		Iterator itr = this.entries.iterator();
		
		while (itr.hasNext()) {
			g.drawLine(this.scale * i, 0, this.scale * i, 
					this.scale * ((Integer)itr.next()).intValue());
			i++;
		}
	}
}
