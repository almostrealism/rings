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

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.almostrealism.ui.Event;
import org.almostrealism.ui.EventGenerator;
import org.almostrealism.ui.EventHandler;
import org.almostrealism.ui.EventListener;
import org.almostrealism.util.graphics.RGB;

import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.io.FileEncoder;
import com.almostrealism.raytracer.surfaceUI.RenderPanel;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.raytracer.ui.SurfaceInfoPanel;

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