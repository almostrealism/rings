/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */

/**
 *  @author Samuel Tepper
 */

package net.sf.j3d.physics.pfield.ui;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class SceneMenuBar extends JMenuBar implements ActionListener{

	private JMenu fileMenu;
	private JMenuItem saveAs, save, exit;
	
	public SceneMenuBar() {
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		saveAs = new JMenuItem("Save As...");
		saveAs.getAccessibleContext().setAccessibleDescription("Save this simulation to a file");
		save = new JMenuItem("Save");
		save.getAccessibleContext().setAccessibleDescription("Save this version to the current file");
		fileMenu.addSeparator();
		exit = new JMenuItem("Exit");
		exit.getAccessibleContext().setAccessibleDescription("Exit the Photon Field Simulator");
		
		
		fileMenu.add(saveAs);
		fileMenu.add(save);
		fileMenu.add(exit);
		
		super.add(fileMenu);
	}
	
	public void actionPerformed(ActionEvent e){
		if (e.getSource() == saveAs){
			//TODO: save the current xml to a file using a fileChooser
		}
		if(e.getSource() == save){
			//TODO: Save the current xml file
		}
		if(e.getSource() == exit){
			//TODO: Exit the program
		}
		
	}

}
