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

package com.almostrealism.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.almostrealism.ui.dialogs.NetworkDialog;


// TODO  Add new menu items: send task and connect to server

/**
 * A NetworkMenu object provides a menu that allows the user to access networking features.
 * 
 * @author Mike Murray
 */
public class NetworkMenu extends JMenu {
  private NetworkDialog confDialog;
  
  private JMenuItem configureItem;
  private JMenuItem restartItem;

	/**
	 * Constructs a new NetworkMenu object.
	 */
	public NetworkMenu() {
		super("Network");
		
		this.confDialog = new NetworkDialog();
		
		this.configureItem = new JMenuItem("Configure network");
		this.restartItem = new JMenuItem("Restart");
		
		this.configureItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { NetworkMenu.this.confDialog.showDialog(); }
		});
		
		this.restartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { NetworkMenu.this.confDialog.restart(); }
		});
		
		super.add(this.configureItem);
		super.add(this.restartItem);
	}
}