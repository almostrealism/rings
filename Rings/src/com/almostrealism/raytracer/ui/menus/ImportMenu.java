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

import java.beans.ExceptionListener;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.raytracer.ui.SurfaceAddEvent;
import com.almostrealism.ui.Event;
import com.almostrealism.ui.EventGenerator;
import com.almostrealism.ui.EventHandler;
import com.almostrealism.ui.EventListener;


/**
 * @author Mike Murray
 */
public class ImportMenu extends JMenu implements EventListener, EventGenerator {
	private Scene scene;
	
	private EventHandler handler;
	
	private JMenu importSceneMenu, importSurfaceMenu;
	
	private JMenuItem importXMLSceneItem;
	private JMenuItem importXMLSurfaceItem, importRAWSurfaceItem, importGTSSurfaceItem;
	
	public static class CustomExceptionListener implements ExceptionListener {
		public boolean thrown = false;
		public void exceptionThrown(Exception e) { this.thrown = true; }
	}
	
	/**
	 * Constructs a new OpenMenu object.
	 */
	public ImportMenu(Scene scn) {
		super("Import");
		
		this.scene = scn;
		
		this.importSceneMenu = new JMenu("Scene");
		this.importSurfaceMenu = new JMenu("Surface");
		
		this.importXMLSceneItem = new JMenuItem("XML Encoded Scene");
		
		this.importXMLSurfaceItem = new JMenuItem("XML Encoded Surface");
		this.importRAWSurfaceItem = new JMenuItem("RAW Encoded Surface");
		this.importGTSSurfaceItem = new JMenuItem("GTS Encoded Surface");
		
		this.importSceneMenu.add(this.importXMLSceneItem);
		
		this.importSurfaceMenu.add(this.importXMLSurfaceItem);
		this.importSurfaceMenu.add(this.importRAWSurfaceItem);
		this.importSurfaceMenu.add(this.importGTSSurfaceItem);
		
		this.add(this.importSceneMenu);
		this.add(this.importSurfaceMenu);
		
		this.importXMLSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openScene(FileDecoder.XMLEncoding);
			}
		});
		
		this.importXMLSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.XMLEncoding);
			}
		});
		
		this.importRAWSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.RAWEncoding);
			}
		});
		
		this.importGTSSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.GTSEncoding);
			}
		});
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
						Light l[] = newScene.getLights();
						Surface s[] = newScene.getSurfaces();
						
						if (ImportMenu.this.scene != null) {
							for (int i = 0; i < l.length; i++) ImportMenu.this.scene.addLight(l[i]);
							for (int i = 0; i < s.length; i++) ImportMenu.this.scene.addSurface(s[i]);
						}
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
	 * Allows the user to select a file that stores surface data and adds the constructed Surface object to the Scene object
	 * stored by this OpenMenu object. It then fires the required events.
	 */
	public void openSurface(final int encoding) {
		final JFileChooser fileChooser = new JFileChooser();
		int selected = fileChooser.showOpenDialog(null);
		
		if (selected == JFileChooser.APPROVE_OPTION) {
			final JFrame frame = new JFrame("Loading");
			frame.setSize(250, 70);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(new JLabel("Loading surface file..."));
			
			frame.setVisible(true);
				
			final Thread loader = new Thread(new Runnable() {
				public void run() {
					CustomExceptionListener listener = new CustomExceptionListener();
					
					try {
						Surface newSurface = FileDecoder.decodeSurfaceFile(fileChooser.getSelectedFile(), encoding, true, listener);
						scene.addSurface(newSurface);
						
						if (handler != null) handler.fireEvent(new SurfaceAddEvent(newSurface));
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
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
		}
	}
	
	/**
	 * Sets the EventHandler object used by this OpenMenu object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) { this.handler = handler; }
	
	/**
	 * Returns the EventHandler object used by this OpenMenu object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}