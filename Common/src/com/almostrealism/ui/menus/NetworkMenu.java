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