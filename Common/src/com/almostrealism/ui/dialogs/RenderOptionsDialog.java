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

package com.almostrealism.ui.dialogs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.ui.panels.RenderPanel;

/**
 * A RenderOptionsDialog can be used to allow the user to modify a RenderPanel object.
 * 
 * @author Mike Murray
 */
public class RenderOptionsDialog extends JPanel implements Dialog {
  private RenderPanel renderPanel;
  
  private boolean open;
  
  private JFrame frame;
  
  private JPanel dataPanel, buttonPanel;
  private JPanel imageSizePanel, antialiasingPanel;
  
  private JTextField imageWidthField, imageHeightField;
  private JTextField supersampleWidthField, supersampleHeightField;
  private JCheckBox squarePixelsBox;
  private JCheckBox castShadowsBox;
  
  private JButton applyButton, okButton, cancelButton, checkBrightnessButton;

	/**
	 * Constructs a new RenderOptionsDialog that can be used to modify the specified RenderPanel object.
	 */
	public RenderOptionsDialog(RenderPanel renderPanel) {
		this.renderPanel = renderPanel;
		
		GridBagLayout gb = new GridBagLayout();
		
		this.dataPanel = new JPanel(gb);
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.imageSizePanel = new JPanel(new java.awt.GridLayout(0, 2));
		this.imageSizePanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Image Size"));
		
		this.antialiasingPanel = new JPanel(new java.awt.GridLayout(2, 2));
		this.antialiasingPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Anti-Aliasing Options"));
		
		this.imageWidthField = new JTextField(6);
		this.imageHeightField = new JTextField(6);
		this.squarePixelsBox = new JCheckBox("Force Square Pixels", true);
		this.castShadowsBox = new JCheckBox("Cast Shadows", true);
		
		FocusListener f = new FocusListener() {
			public void focusGained(FocusEvent event) {}
			
			public void focusLost(FocusEvent event) {
				if (RenderOptionsDialog.this.squarePixelsBox.isSelected()) {
					if (event.getSource() == RenderOptionsDialog.this.imageWidthField) {
						int w = 100;
						double pw = RenderOptionsDialog.this.renderPanel.getProjectionWidth();
						double ph = RenderOptionsDialog.this.renderPanel.getProjectionHeight();
						
						try {
							w = Integer.parseInt(RenderOptionsDialog.this.imageWidthField.getText());
						} catch (NumberFormatException nf) {
							RenderOptionsDialog.this.imageWidthField.setText("100");
						}
						
						RenderOptionsDialog.this.imageHeightField.setText(String.valueOf((int)(ph * (w / pw))));
					} else if (event.getSource() == RenderOptionsDialog.this.imageHeightField) {
						int h = 100;
						double pw = RenderOptionsDialog.this.renderPanel.getProjectionWidth();
						double ph = RenderOptionsDialog.this.renderPanel.getProjectionHeight();
						
						try {
							h = Integer.parseInt(RenderOptionsDialog.this.imageHeightField.getText());
						} catch (NumberFormatException nf) {
							RenderOptionsDialog.this.imageHeightField.setText("100");
						}
						
						RenderOptionsDialog.this.imageWidthField.setText(String.valueOf((int)(pw * (h / ph))));
					}
				}
			}
		};
		
		this.squarePixelsBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				if (RenderOptionsDialog.this.squarePixelsBox.isSelected()) {
					RenderOptionsDialog.this.updateImageHeight();
				}
			}
		});
		
		this.imageWidthField.addFocusListener(f);
		this.imageHeightField.addFocusListener(f);
		
		this.supersampleWidthField = new JTextField(6);
		this.supersampleHeightField = new JTextField(6);
		
		this.applyButton = new JButton("Apply");
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		this.checkBrightnessButton = new JButton("Brightness Test");
		
		this.imageSizePanel.add(new JLabel("Width: "));
		this.imageSizePanel.add(this.imageWidthField);
		this.imageSizePanel.add(new JLabel("Height: "));
		this.imageSizePanel.add(this.imageHeightField);
		this.imageSizePanel.add(this.squarePixelsBox);
		
		this.antialiasingPanel.add(new JLabel("Super Sample Width: "));
		this.antialiasingPanel.add(this.supersampleWidthField);
		this.antialiasingPanel.add(new JLabel("Super Sample Height: "));
		this.antialiasingPanel.add(this.supersampleHeightField);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		c.gridheight = 3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(this.imageSizePanel, c);
		this.dataPanel.add(this.imageSizePanel);
		
		c.gridheight = 2;
		gb.setConstraints(this.antialiasingPanel, c);
		this.dataPanel.add(this.antialiasingPanel);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridheight = 1;
		gb.setConstraints(this.castShadowsBox, c);
		this.dataPanel.add(this.castShadowsBox);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		gb.setConstraints(this.checkBrightnessButton, c);
		this.dataPanel.add(this.checkBrightnessButton);
		
		this.buttonPanel.add(this.applyButton);
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.dataPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.frame = new JFrame("Render Options...");
		this.frame.setSize(360, 270);
		
		this.frame.getContentPane().add(this);
		
		this.applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) { apply(); }
		});
		
		this.okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
				hideDialog();
			}
		});
		
		this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) { hideDialog(); }
		});
		
		this.checkBrightnessButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				double b = RenderOptionsDialog.this.renderPanel.calculateAverageBrightness();
				
				JOptionPane.showMessageDialog(null, "Average brightness = " + b,
											"Average Brightness", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) { hideDialog(); }
		});
	}
	
	/**
	 * Shows this dialog in a JFrame.
	 */
	public void showDialog() {
		if (this.open == true)
			return;
		
		this.updateAllFields();
		this.frame.setVisible(true);
		this.open = true;
	}
	
	/**
	 * Hides this dialog if it is open. This dialog can be reopened
	 * by calling the showDialog method.
	 */
	public void hideDialog() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
		}
	}
	
	/**
	 * Closes (and disposes) this dialog if it is open.
	 */
	public void closeDialog() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
			
			this.frame.dispose();
		}
	}
	
	/**
	 * Applies the changes made in this dialog to the RenderPanel object being edited.
	 */
	public void apply() {
		this.renderPanel.setImageWidth(Integer.parseInt(this.imageWidthField.getText()));
		this.renderPanel.setImageHeight(Integer.parseInt(this.imageHeightField.getText()));
		
		this.renderPanel.setSupersampleWidth(Integer.parseInt(this.supersampleWidthField.getText()));
		this.renderPanel.setSupersampleHeight(Integer.parseInt(this.supersampleHeightField.getText()));
		
		RayTracingEngine.castShadows = this.castShadowsBox.isSelected();
	}
	
	/**
	 * Updates all fields of this dialog so that they match the RenderPanel object being edited.
	 */
	public void updateAllFields() {
		this.updateImageSizeFields();
		this.updateAntialiasingFields();
		this.updateShadowFields();
	}
	
	/**
	 * Updates the image size fields of this dialog so that they match the RenderPanel object being edited.
	 */
	public void updateImageSizeFields() {
		this.imageWidthField.setText(String.valueOf(this.renderPanel.getImageWidth()));
		this.imageHeightField.setText(String.valueOf(this.renderPanel.getImageHeight()));
		
		if (this.squarePixelsBox.isSelected()) this.updateImageHeight();
	}
	
	public void updateImageWidth() {
		int h = 100;
		double pw = RenderOptionsDialog.this.renderPanel.getProjectionWidth();
		double ph = RenderOptionsDialog.this.renderPanel.getProjectionHeight();
		
		try {
			h = Integer.parseInt(RenderOptionsDialog.this.imageHeightField.getText());
		} catch (NumberFormatException nf) {
			RenderOptionsDialog.this.imageHeightField.setText("100");
		}
		
		RenderOptionsDialog.this.imageWidthField.setText(String.valueOf((int)(pw * (h / ph))));
	}
	
	public void updateImageHeight() {
		int w = 100;
		double pw = RenderOptionsDialog.this.renderPanel.getProjectionWidth();
		double ph = RenderOptionsDialog.this.renderPanel.getProjectionHeight();
		
		try {
			w = Integer.parseInt(RenderOptionsDialog.this.imageWidthField.getText());
		} catch (NumberFormatException nf) {
			RenderOptionsDialog.this.imageWidthField.setText("100");
		}
		
		RenderOptionsDialog.this.imageHeightField.setText(String.valueOf((int)(ph * (w / pw))));
	}
	
	/**
	 * Updates the anti aliasing options fields of this dialog so that they match the
	 * RenderPanel object being edited.
	 */
	public void updateAntialiasingFields() {
		this.supersampleWidthField.setText(String.valueOf(this.renderPanel.getSupersampleWidth()));
		this.supersampleHeightField.setText(String.valueOf(this.renderPanel.getSupersampleHeight()));
	}
	
	/**
	 * Updates the cast shadows check box so that it displays the value of RayTracingEngine.castShadows.
	 */
	public void updateShadowFields() {
		this.castShadowsBox.setSelected(RayTracingEngine.castShadows);
	}
}
