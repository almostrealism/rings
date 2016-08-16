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

import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.ui.SceneCloseEvent;
import com.almostrealism.raytracer.ui.SceneOpenEvent;
import com.almostrealism.raytracer.ui.SurfaceAddEvent;

/**
  An OpenMenu object extends JMenu and provides menu items for open scene data stored in a file.
*/

public class OpenMenu extends JMenu implements EventListener, EventGenerator {
  private Scene scene;
  
  private EventHandler handler;
  
  private JMenu openSceneMenu, openSurfaceMenu;
  
  private JMenuItem openXMLSceneItem;
  private JMenuItem openXMLSurfaceItem, openRAWSurfaceItem, openGTSSurfaceItem;

	private class CustomExceptionListener implements ExceptionListener {
		public boolean thrown = false;
		
		public void exceptionThrown(Exception e) {
			this.thrown = true;
		}
	}
	
	/**
	  Constructs a new OpenMenu object.
	*/
	
	public OpenMenu(Scene scn) {
		super("Open");
		
		this.scene = scn;
		
		this.openSceneMenu = new JMenu("Scene");
		this.openSurfaceMenu = new JMenu("Surface");
		
		this.openXMLSceneItem = new JMenuItem("XML Encoded Scene");
		
		this.openXMLSurfaceItem = new JMenuItem("XML Encoded Surface");
		this.openRAWSurfaceItem = new JMenuItem("RAW Encoded Surface");
		this.openGTSSurfaceItem = new JMenuItem("GTS Encoded Surface");
		
		this.openSceneMenu.add(this.openXMLSceneItem);
		
		this.openSurfaceMenu.add(this.openXMLSurfaceItem);
		this.openSurfaceMenu.add(this.openRAWSurfaceItem);
		this.openSurfaceMenu.add(this.openGTSSurfaceItem);
		
		this.add(this.openSceneMenu);
		this.add(this.openSurfaceMenu);
		
		this.openXMLSceneItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openScene(FileDecoder.XMLEncoding);
			}
		});
		
		this.openXMLSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.XMLEncoding);
			}
		});
		
		this.openRAWSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.RAWEncoding);
			}
		});
		
		this.openGTSSurfaceItem.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				openSurface(FileDecoder.GTSEncoding);
			}
		});
	}
	
	/**
	  Allows the user to select a file that stores scene data and opens that file. It then fires the required events.
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
	  Allows the user to select a file that stores surface data and adds the constructed Surface object to the Scene object
	  stored by this OpenMenu object. It then fires the required events.
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
	  Sets the EventHandler object used by this OpenMenu object. Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	  Returns the EventHandler object used by this OpenMenu object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
