/*
* Copyright (C) 2004-06  Mike Murray
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

import java.io.*;

import javax.swing.*;

import net.sf.j3d.io.*;

import com.almostrealism.io.FileEncoder;
import com.almostrealism.raytracer.engine.*;

import net.sf.j3d.ui.event.*;
import net.sf.j3d.ui.panels.*;
import net.sf.j3d.util.graphics.RGB;

/**
 * A SaveMenu object extends JMenu and provides menu items for saving scene and surface data to a file.
 */
public class SaveMenu extends JMenu implements EventListener, EventGenerator {
  private Scene scene;
  private RenderPanel renderPanel;
  private SurfaceInfoPanel surfacePanel;
  
  private EventHandler handler;
  
  private JMenu saveSceneMenu, saveSurfaceMenu, saveImageMenu;
  private JMenuItem saveXMLEncodedSceneItem;
  private JMenuItem saveXMLEncodedSurfaceItem, saveGTSEncodedSurfaceItem;
  private JMenuItem saveJPEGEncodedImageItem, savePPMEncodedImageItem, savePIXEncodedImageItem;

	/**
	 * Constructs a new SaveMenu object.
	 */
	public SaveMenu(Scene scene, RenderPanel renderPanel, SurfaceInfoPanel surfacePanel) {
		super("Save");
		
		this.scene = scene;
		this.renderPanel = renderPanel;
		this.surfacePanel = surfacePanel;
		
		this.saveSceneMenu = new JMenu("Scene");
		this.saveSurfaceMenu = new JMenu("Surface");
		this.saveImageMenu = new JMenu("Image");
		
		this.saveXMLEncodedSceneItem = new JMenuItem("As XML");
		
		this.saveXMLEncodedSurfaceItem = new JMenuItem("As XML");
		this.saveGTSEncodedSurfaceItem = new JMenuItem("As GTS");
		
		this.saveJPEGEncodedImageItem = new JMenuItem("As JPEG");
		this.savePPMEncodedImageItem = new JMenuItem("As PPM");
		this.savePIXEncodedImageItem = new JMenuItem("As PIX");
		
		this.saveSceneMenu.add(this.saveXMLEncodedSceneItem);
		
		this.saveSurfaceMenu.add(this.saveXMLEncodedSurfaceItem);
		this.saveSurfaceMenu.add(this.saveGTSEncodedSurfaceItem);
		
		this.saveImageMenu.add(this.saveJPEGEncodedImageItem);
		this.saveImageMenu.add(this.savePPMEncodedImageItem);
		this.saveImageMenu.add(this.savePIXEncodedImageItem);
		
		this.add(this.saveSceneMenu);
		this.add(this.saveSurfaceMenu);
		this.addSeparator();
		this.add(this.saveImageMenu);
		
		this.saveXMLEncodedSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveScene(FileEncoder.XMLEncoding);
			}
		});
		
		this.saveXMLEncodedSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveSurface(FileEncoder.XMLEncoding);
			}
		});
		
		this.saveGTSEncodedSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveSurface(FileEncoder.GTSEncoding);
			}
		});
		
		this.saveJPEGEncodedImageItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveImage(FileEncoder.JPEGEncoding);
			}
		});
		
		this.savePPMEncodedImageItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveImage(FileEncoder.PPMEncoding);
			}
		});
		
		this.savePIXEncodedImageItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				saveImage(FileEncoder.PIXEncoding);
			}
		});
	}
	
	/**
	  Allows the user to select a file and save scene data to that file.
	*/
	
	public void saveScene(final int encoding) {
		if (this.scene == null)
			return;
		
		final JFileChooser fileChooser = new JFileChooser();
		int selected = fileChooser.showSaveDialog(null);
		
		if (selected == JFileChooser.APPROVE_OPTION) {
			final JFrame frame = new JFrame("Saving");
			frame.setSize(250, 70);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(new JLabel("Saving scene file..."));
			
			frame.setVisible(true);
			
			final Thread saver = new Thread(new Runnable() {
				public void run() {
					try {
						FileEncoder.encodeSceneFile(SaveMenu.this.scene, fileChooser.getSelectedFile(), encoding);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "An IO error occured while saving.",
								"IO Error", JOptionPane.ERROR_MESSAGE);
					}
					
					frame.setVisible(false);
				}
			});
			
			saver.start();
		}
	}
	
	/**
	  Allows the user to select a surface and a file and save the surface data to that file.
	*/
	
	public void saveSurface(final int encoding) {
		if (this.scene == null)
			return;
		
		final Surface surface = this.surfacePanel.getSelectedSurface();
		
		if (surface == null)
			return;
		
		final JFileChooser fileChooser = new JFileChooser();
		int selected = fileChooser.showSaveDialog(null);
		
		if (selected == JFileChooser.APPROVE_OPTION) {
			final JFrame frame = new JFrame("Saving");
			frame.setSize(250, 70);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(new JLabel("Saving surface file..."));
			
			frame.setVisible(true);
			
			final Thread saver = new Thread(new Runnable() {
				public void run() {
					try {
						FileEncoder.encodeSurfaceFile(surface, fileChooser.getSelectedFile(), encoding);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "An IO error occured while saving.",
								"IO Error", JOptionPane.ERROR_MESSAGE);
					}
					
					frame.setVisible(false);
				}
			});
			
			saver.start();
		}
	}
	
	/**
	  Allows the user to select a file a save image data to that file.
	*/
	
	public void saveImage(final int encoding) {
		final RGB image[][] = this.renderPanel.getRenderedImageData();
		
		if (image == null)
			return;
		
		final JFileChooser fileChooser = new JFileChooser();
		int selected = fileChooser.showSaveDialog(null);
		
		if (selected == JFileChooser.APPROVE_OPTION) {
			final JFrame frame = new JFrame("Saving");
			frame.setSize(250, 70);
			frame.getContentPane().setLayout(new java.awt.FlowLayout());
			frame.getContentPane().add(new JLabel("Saving image file..."));
			
			frame.setVisible(true);
			
			final Thread saver = new Thread(new Runnable() {
				public void run() {
					try {
						FileEncoder.encodeImageFile(image, fileChooser.getSelectedFile(), encoding);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(null, "An IO error occured while saving.",
								"IO Error", JOptionPane.ERROR_MESSAGE);
					}
					
					frame.setVisible(false);
				}
			});
			
			saver.start();
		}
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SceneOpenEvent) {
			this.scene = ((SceneOpenEvent)event).getScene();
		} else if (event instanceof SceneCloseEvent) {
			this.scene = null;
		}
	}
	
	/**
	  Sets the EventHandler object used by this SaveMenu object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this SaveMenu object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
