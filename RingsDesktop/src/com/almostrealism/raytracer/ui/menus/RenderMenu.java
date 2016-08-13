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

package com.almostrealism.raytracer.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.almostrealism.raytracer.surfaceUI.RenderPanel;
import com.almostrealism.raytracer.ui.RenderOptionsDialog;


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
