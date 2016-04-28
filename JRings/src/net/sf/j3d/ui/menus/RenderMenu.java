/*
* Copyright (C) 2004-05  Mike Murray
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

package net.sf.j3d.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.sf.j3d.ui.dialogs.RenderOptionsDialog;
import net.sf.j3d.ui.panels.RenderPanel;


/**
 * A RenderMenu object extends JMenu and provides menu items for accessing the RenderPanel object it uses.
 * 
 * @author Mike Murray
 */
public class RenderMenu extends JMenu {
  private RenderPanel panel;
  
  private JMenu networkMenu;
  private JMenuItem startItem, clearItem, optionsItem;
  private RenderOptionsDialog optionsDialog;

	/**
	 * Constructrs a new RenderMenu using the specified RenderPanel object.
	 */
	public RenderMenu(RenderPanel panel) {
		super("Render");
		
		this.panel = panel;
		
		this.networkMenu = new NetworkMenu();
		
		this.startItem = new JMenuItem("Start");
		this.clearItem = new JMenuItem("Clear");
		this.optionsItem = new JMenuItem("Options...");
		
		this.add(this.startItem);
		this.add(this.clearItem);
		this.addSeparator();
		this.add(this.optionsItem);
		this.addSeparator();
		this.add(this.networkMenu);
		
		this.optionsDialog = new RenderOptionsDialog(this.panel);
		
		this.startItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { RenderMenu.this.panel.render(); }
		});
		
		this.clearItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { RenderMenu.this.panel.clearRenderedImage(); }
		});
		
		this.optionsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { RenderMenu.this.optionsDialog.showDialog(); }
		});
	}
}
