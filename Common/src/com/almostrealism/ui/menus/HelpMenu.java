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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.almostrealism.raytracer.Settings;


/**
 * @author Mike Murray
 */
public class HelpMenu extends JMenu {
  private JTextArea area;

	/**
	 * Constructs a new HelpMenu object.
	 */
	public HelpMenu() {
		super("Help");
		
		this.area = new JTextArea(Settings.aboutText);
		this.area.setEditable(false);
		this.area.setLineWrap(true);
		this.area.setWrapStyleWord(true);
		
		JMenuItem aboutItem = new JMenuItem("About");
		
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JOptionPane.showMessageDialog(null, new JScrollPane(area), "About Rings", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		super.add(aboutItem);
	}
}
