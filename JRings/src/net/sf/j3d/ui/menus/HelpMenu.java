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

package net.sf.j3d.ui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.sf.j3d.run.Settings;


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
