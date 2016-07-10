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

package com.almostrealism.photonfield.ui;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 *  @author Samuel Tepper
 */
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
