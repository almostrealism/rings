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

import com.almostrealism.io.FileDecoder;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.lighting.Light;

import net.sf.j3d.ui.event.Event;
import net.sf.j3d.ui.event.EventGenerator;
import net.sf.j3d.ui.event.EventHandler;
import net.sf.j3d.ui.event.EventListener;
import net.sf.j3d.ui.event.SceneCloseEvent;
import net.sf.j3d.ui.event.SceneOpenEvent;
import net.sf.j3d.ui.event.SurfaceAddEvent;


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
