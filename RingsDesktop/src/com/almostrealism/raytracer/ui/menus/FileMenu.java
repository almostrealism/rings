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

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.RenderPanel;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.event.SceneCloseEvent;
import com.almostrealism.raytracer.event.SceneOpenEvent;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.ui.SurfaceInfoPanel;
import com.almostrealism.raytracer.ui.menus.ImportMenu.CustomExceptionListener;

/**
  The FileMenu class extends JMenu and provides menu items for opening, closing, and saving files.
*/

public class FileMenu extends JMenu implements EventListener, EventGenerator {
  private EventHandler handler;
  
  private NewMenu newMenu;
  private ImportMenu importMenu;
  private SaveMenu saveMenu;
  
  private JMenuItem openSceneItem;
  private JMenuItem closeSceneItem;
  private JMenuItem exitItem;

	/**
	  Constructs a new FileMenu object using the specified Scene object.
	*/
	
	public FileMenu(Scene scene, RenderPanel renderPanel, SurfaceInfoPanel surfacePanel) {
		super("File");
		
		this.newMenu = new NewMenu(scene);
		this.importMenu = new ImportMenu(scene);
		this.saveMenu = new SaveMenu(scene, renderPanel, surfacePanel);
		
		this.openSceneItem = new JMenuItem("Open Scene");
		this.closeSceneItem = new JMenuItem("Close Scene");
		this.exitItem = new JMenuItem("Exit");
		
		this.add(this.newMenu);
		this.add(this.openSceneItem);
		this.add(this.saveMenu);
		this.add(this.closeSceneItem);
		this.add(this.importMenu);
		this.addSeparator();
		this.add(this.exitItem);
		
		this.openSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openScene(FileDecoder.XMLEncoding);
			}
		});
		
		this.closeSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (handler != null) {
					handler.fireEvent(new SceneCloseEvent());
				}
			}
		});
		
		this.exitItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				System.exit(0);
			}
		});
		
		this.newMenu.setEventHandler(this.handler);
		this.importMenu.setEventHandler(this.handler);
		this.saveMenu.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.newMenu);
			this.handler.addListener(this.importMenu);
			this.handler.addListener(this.saveMenu);
		}
	}
	
	/**
	 * Allows the user to select a file that stores scene data and opens that file. It then fires the required events.
	 */
	public void openScene(final int encoding) {
		final JFileChooser fileChooser = new JFileChooser();
		int selected = fileChooser.showOpenDialog(null);
		
		if (selected == JFileChooser.APPROVE_OPTION) {
			final JFrame frame = new JFrame("Loading");
			frame.setSize(250, 70);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(new JLabel("Loading scene file..."));
			
			frame.setVisible(true);
			
			final Thread loader = new Thread(new Runnable() {
				public void run() {
					CustomExceptionListener listener = new CustomExceptionListener();
					
					try {
						Scene newScene = FileDecoder.decodeSceneFile(fileChooser.getSelectedFile(), encoding, true, listener);
						if (handler != null) handler.fireEvent(new SceneOpenEvent(newScene));
					} catch (FileNotFoundException fnf) {
						JOptionPane.showMessageDialog(null, "There was a FileNotFoundException thrown by threeD.io.FileDecoder",
										"File Not Found", JOptionPane.ERROR_MESSAGE);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "There was an IOException thrown by threeD.io.FileDecoder",
										"IO Error", JOptionPane.ERROR_MESSAGE);
					} catch (Exception e) {
						e.printStackTrace();
						listener.thrown = true;
					}
					
					if (listener.thrown)
						JOptionPane.showMessageDialog(null, "Some errors occured while loading the file.", "Error", JOptionPane.ERROR_MESSAGE);
					
					frame.setVisible(false);
				}
			});
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent event) {
					loader.stop();
					System.gc();
				}
			});
			
			frame.getContentPane().add(cancelButton);
			
			loader.start();
		}
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {}
	
	/**
	 * Sets the EventHandler object used by this FileMenu object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		this.newMenu.setEventHandler(this.handler);
		this.importMenu.setEventHandler(this.handler);
		this.saveMenu.setEventHandler(this.handler);
		
		if (this.handler != null) {
			this.handler.addListener(this.newMenu);
			this.handler.addListener(this.importMenu);
			this.handler.addListener(this.saveMenu);
		}
	}
	
	/**
	 * Returns the EventHandler object used by this FileMenu object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
