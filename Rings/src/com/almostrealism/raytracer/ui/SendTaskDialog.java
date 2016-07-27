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

package com.almostrealism.raytracer.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.almostrealism.flow.Server;
import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.network.RayTracingJobFactory;
import com.almostrealism.ui.Dialog;

/**
 * A SendTaskDialog object provides a dialog for collecting input required
 * to submit a rendering task.
 * 
 * @author Mike Murray
 */
public class SendTaskDialog extends JPanel implements Dialog {
  public static final String jobSizeOptions[] = {"1", "4", "9", "16", "25", "36", "49", "64", "81", "100"};
  
  private JFrame frame;
  private boolean open;
  
  private Server server;
  private int index;
  
  private JPanel renderPanel, buttonPanel;
  private JTextField hostField, portField;
  private JComboBox jobSizeField;
  private JFormattedTextField widthField, heightField, ssWidthField, ssHeightField;
  private JButton submitButton, cancelButton;

	/**
	 * Constructs a new SendTaskDialog.
	 */
	public SendTaskDialog(Server server, int index) {
		super(new BorderLayout());
		
		this.open = false;
		
		this.server = server;
		this.index = index;
		
		this.widthField = new JFormattedTextField(Settings.integerFormat);
		this.heightField = new JFormattedTextField(Settings.integerFormat);
		this.ssWidthField = new JFormattedTextField(Settings.integerFormat);
		this.ssHeightField = new JFormattedTextField(Settings.integerFormat);
		
		this.jobSizeField = new JComboBox(SendTaskDialog.jobSizeOptions);
		
		this.submitButton = new JButton("Submit");
		this.cancelButton = new JButton("Cancel");
		
		this.submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { SendTaskDialog.this.submit(); }
		});
		
		this.cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) { SendTaskDialog.this.closeDialog(); }
		});
		
		this.renderPanel = new JPanel(new GridLayout(0, 2));
		this.renderPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Render Options"));
		
		this.renderPanel.add(new JLabel("Width: "));
		this.renderPanel.add(this.widthField);
		this.renderPanel.add(new JLabel("Height: "));
		this.renderPanel.add(this.heightField);
		this.renderPanel.add(new JLabel("Super Sample Width: "));
		this.renderPanel.add(this.ssWidthField);
		this.renderPanel.add(new JLabel("Super Sample Height: "));
		this.renderPanel.add(this.ssHeightField);
		this.renderPanel.add(new JLabel("Job Size: "));
		this.renderPanel.add(this.jobSizeField);
		
		this.buttonPanel = new JPanel(new FlowLayout());
		
		this.buttonPanel.add(this.submitButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.renderPanel, BorderLayout.CENTER);
		this.add(this.buttonPanel, BorderLayout.SOUTH);
		
		this.frame = new JFrame("Submit Rendering Task");
		this.frame.getContentPane().add(this);
		
		this.frame.setSize(300, 200);
	}
	
	public void submit() {
		final int jobSize = Integer.parseInt(SendTaskDialog.jobSizeOptions[this.jobSizeField.getSelectedIndex()]);
		
		final int width = ((Number)this.widthField.getValue()).intValue();
		final int height = ((Number)this.heightField.getValue()).intValue();
		final int ssWidth = ((Number)this.ssWidthField.getValue()).intValue();
		final int ssHeight = ((Number)this.ssHeightField.getValue()).intValue();
		
		final String uri = JOptionPane.showInputDialog(this, "Enter scene URI:",
												"Scene URI",
												JOptionPane.PLAIN_MESSAGE);
		
		long id = System.currentTimeMillis();
		RayTracingJobFactory f = new RayTracingJobFactory(uri, width, height,
														ssWidth, ssHeight,
														jobSize, id);
		
		this.closeDialog();
		
		final JWindow waitWindow = new JWindow();
		waitWindow.getContentPane().add(new JLabel("    Please wait..."));
		
		waitWindow.setSize(160, 50);
		waitWindow.setLocation((Settings.screenWidth - waitWindow.getWidth()) / 2,
								(Settings.screenHeight - waitWindow.getHeight()) / 2);
		
		waitWindow.setVisible(true);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				final long id = System.currentTimeMillis();
				RayTracingJobFactory f = new RayTracingJobFactory(uri, width, height,
																ssWidth, ssHeight,
																jobSize, id);
				
				SendTaskDialog.this.server.sendTask(f.encode(), SendTaskDialog.this.index);
				
				waitWindow.setVisible(false);
				waitWindow.dispose();
				
				try {
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(null, "Task was sent using id " + id,
														"Sent task",
														JOptionPane.INFORMATION_MESSAGE);
						}
					});
				} catch (InterruptedException e) {
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		});
		
		t.start();
	}
	
	/**
	 * @see com.almostrealism.ui.Dialog#showDialog()
	 */
	public void showDialog() {
		if (!this.open) {
			this.frame.setVisible(true);
			this.open = true;
		}
	}

	/**
	 * @see com.almostrealism.ui.Dialog#closeDialog()
	 */
	public void closeDialog() {
		if (this.open) {
			this.frame.setVisible(false);
			this.open = false;
		}
	}
}
