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

package com.almostrealism.ui.panels;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;

import com.almostrealism.io.JTextAreaPrintWriter;
import com.almostrealism.raytracer.Settings;


/**
  A DebugOutputPanel object provides a display for the debug output of the ray tracing application.
*/

public class DebugOutputPanel extends JPanel {
  private JFrame frame;
  
  private JMenu fileMenu;
  private JMenuItem saveItem;
  
  private JTabbedPane tabbedPane;
  private JPanel rayEnginePanel, shaderPanel, surfacePanel, cameraPanel, eventPanel;
  private JToggleButton rayEngineToggle, surfaceToggle, cameraToggle, eventToggle;
  private JButton clearButton;

	/**
	  Constructs a new DebugOutputPanel object.
	*/
	
	public DebugOutputPanel() {
		this.tabbedPane = new JTabbedPane();
		
		this.clearButton = new JButton("Clear");
		
		this.clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				JPanel p = (JPanel) DebugOutputPanel.this.tabbedPane.getSelectedComponent();
				JTextArea a = (JTextArea) ((JViewport)((JScrollPane)p.getComponent(0)).
											getComponent(0)).getComponent(0);
				a.setText("");
			}
		});
		
		this.saveItem = new JMenuItem("Save output...");
		
		this.saveItem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent event) {
				final JFileChooser fileChooser = new JFileChooser();
				int selected = fileChooser.showSaveDialog(null);
				
				if (selected == JFileChooser.APPROVE_OPTION) {
					final JFrame frame = new JFrame("Saving");
					frame.setSize(250, 70);
					frame.getContentPane().setLayout(new java.awt.FlowLayout());
					frame.getContentPane().add(new JLabel("Saving debug text..."));
					
					frame.setVisible(true);
					
			        JScrollPane c = (JScrollPane)((Container)DebugOutputPanel.this.tabbedPane.getSelectedComponent()).getComponent(0);
			        JTextComponent tc = (JTextComponent)((JViewport)c.getComponent(0)).getComponent(0);
			        final String text = tc.getText();
					
					final Thread saver = new Thread(new Runnable() {
						public void run() {
							try {
							    PrintWriter out = new PrintWriter(new FileOutputStream(fileChooser.getSelectedFile()));
							    out.write(text);
							    out.flush();
							    out.close();
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
		});
		
		this.fileMenu = new JMenu("File");
		this.fileMenu.add(this.saveItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(this.fileMenu);
		
		if (Settings.produceRayTracingEngineOutput == true) {
			this.rayEnginePanel = new JPanel(new BorderLayout());
			this.rayEngineToggle = new JToggleButton("Stop Output");
			
			this.rayEngineToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (Settings.produceRayTracingEngineOutput == true)
						Settings.produceRayTracingEngineOutput = false;
					else
						Settings.produceRayTracingEngineOutput = true;
				}
			});
			
			this.rayEnginePanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.rayEngineOut).getTextArea()),
					BorderLayout.CENTER);
			this.rayEnginePanel.add(this.rayEngineToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Ray Tracing Engine", this.rayEnginePanel);
		}
		
		if (Settings.produceShaderOutput == true) {
			this.shaderPanel = new JPanel(new BorderLayout());
			this.shaderPanel.add(new JScrollPane(((JTextAreaPrintWriter)Settings.shaderOut).getTextArea()), BorderLayout.CENTER);
			this.tabbedPane.addTab("Shaders", this.shaderPanel);
		}
		
		if (Settings.produceSurfaceOutput == true) {
			this.surfacePanel = new JPanel(new BorderLayout());
			this.surfaceToggle = new JToggleButton("Stop Output");
			
			this.surfaceToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (Settings.produceSurfaceOutput == true)
						Settings.produceSurfaceOutput = false;
					else
						Settings.produceSurfaceOutput = true;
				}
			});
			
			this.surfacePanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.surfaceOut).getTextArea()),
					BorderLayout.CENTER);
			this.surfacePanel.add(this.surfaceToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Surface", this.surfacePanel);
		}
		
		if (Settings.produceCameraOutput == true) {
			this.cameraPanel = new JPanel(new BorderLayout());
			this.cameraToggle = new JToggleButton("Stop Output");
			
			this.cameraToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					if (Settings.produceCameraOutput == true)
						Settings.produceCameraOutput = false;
					else
						Settings.produceCameraOutput = true;
				}
			});
			
			this.cameraPanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.cameraOut).getTextArea()),
					BorderLayout.CENTER);
			this.cameraPanel.add(this.cameraToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Camera", this.cameraPanel);
		}
		
		if (Settings.produceEventHandlerOutput == true) {
			this.tabbedPane.addTab("Event", new JScrollPane(
					((JTextAreaPrintWriter)Settings.eventOut).getTextArea()));
		}
		
		this.add(this.tabbedPane);
		
		this.frame = new JFrame("Output");
		this.frame.setSize(600, 500);
		this.frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		this.frame.getContentPane().add(this, BorderLayout.CENTER);
		this.frame.getContentPane().add(this.clearButton, BorderLayout.SOUTH);
	}
	
	/**
	  Shows this panel in a JFrame.
	*/
	
	public void showPanel() {
		this.frame.setVisible(true);
	}
	
	/**
	  Closes this panel.
	*/
	
	public void closePanel() {
		this.frame.setVisible(false);
	}
}
