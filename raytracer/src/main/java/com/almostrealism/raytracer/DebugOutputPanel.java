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

package com.almostrealism.raytracer;

import org.almostrealism.swing.JTextAreaPrintWriter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A DebugOutputPanel object provides a display for the debug output of the ray tracing application.
 */
public class DebugOutputPanel extends JPanel {
  private final JFrame frame;
  
  private final JMenu fileMenu;
  private final JMenuItem saveItem;
  
  private final JTabbedPane tabbedPane;
  private JPanel rayEnginePanel, shaderPanel, surfacePanel, cameraPanel, eventPanel;
  private JToggleButton rayEngineToggle, surfaceToggle, cameraToggle, eventToggle;
  private final JButton clearButton;

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
							try (PrintWriter out = new PrintWriter(new FileOutputStream(fileChooser.getSelectedFile()))) {
							    out.write(text);
							    out.flush();
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
		
		if (Settings.produceRayTracingEngineOutput) {
			this.rayEnginePanel = new JPanel(new BorderLayout());
			this.rayEngineToggle = new JToggleButton("Stop Output");
			
			this.rayEngineToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					Settings.produceRayTracingEngineOutput = !Settings.produceRayTracingEngineOutput;
				}
			});
			
			this.rayEnginePanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.rayEngineOut).getTextArea()),
					BorderLayout.CENTER);
			this.rayEnginePanel.add(this.rayEngineToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Ray Tracing Engine", this.rayEnginePanel);
		}
		
		if (Settings.produceShaderOutput) {
			this.shaderPanel = new JPanel(new BorderLayout());
			this.shaderPanel.add(new JScrollPane(((JTextAreaPrintWriter)Settings.shaderOut).getTextArea()), BorderLayout.CENTER);
			this.tabbedPane.addTab("Shaders", this.shaderPanel);
		}
		
		if (Settings.produceSurfaceOutput) {
			this.surfacePanel = new JPanel(new BorderLayout());
			this.surfaceToggle = new JToggleButton("Stop Output");
			
			this.surfaceToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					Settings.produceSurfaceOutput = !Settings.produceSurfaceOutput;
				}
			});
			
			this.surfacePanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.surfaceOut).getTextArea()),
					BorderLayout.CENTER);
			this.surfacePanel.add(this.surfaceToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Surface", this.surfacePanel);
		}
		
		if (Settings.produceCameraOutput) {
			this.cameraPanel = new JPanel(new BorderLayout());
			this.cameraToggle = new JToggleButton("Stop Output");
			
			this.cameraToggle.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					Settings.produceCameraOutput = !Settings.produceCameraOutput;
				}
			});
			
			this.cameraPanel.add(new JScrollPane(
					((JTextAreaPrintWriter)Settings.cameraOut).getTextArea()),
					BorderLayout.CENTER);
			this.cameraPanel.add(this.cameraToggle, BorderLayout.SOUTH);
			
			this.tabbedPane.addTab("Camera", this.cameraPanel);
		}
		
		if (Settings.produceEventHandlerOutput) {
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
