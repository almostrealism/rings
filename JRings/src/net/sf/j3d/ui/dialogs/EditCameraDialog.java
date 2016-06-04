/*
* Copyright (C) 2004-05  Mike Murray
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

package net.sf.j3d.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.ParseException;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.camera.ThinLensCamera;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.util.Vector;

import net.sf.j3d.run.Settings;
import net.sf.j3d.ui.event.CameraEditEvent;
import net.sf.j3d.ui.event.CameraEvent;
import net.sf.j3d.ui.event.DialogCloseEvent;
import net.sf.j3d.ui.event.Event;
import net.sf.j3d.ui.event.EventGenerator;
import net.sf.j3d.ui.event.EventHandler;
import net.sf.j3d.ui.event.EventListener;
import net.sf.j3d.ui.event.SceneCloseEvent;
import net.sf.j3d.ui.event.SceneOpenEvent;
import net.sf.j3d.ui.panels.EditVectorPanel;


/**
 * An EditCameraDialog object can be used to specify the settings of a Camera object.
 */
public class EditCameraDialog extends JPanel implements Dialog, EventListener, EventGenerator {
  protected static final String cameraTypes[] = {"Orthographic Camera",
  												"Pinhole Camera",
												"Thin Lens Camera"};
  
  protected static final String viewPresetNames[] = {"Custom",
  													"Inward (-Z)", "Outward(Z)",
													"Up (Y)", "Down (-Y)",
													"Angled left (-Z)", "Angled right (-Z)",
													"Angled up (-Z)", "Angled down (-Z)"};
  protected static final Vector viewPresets[][] = {{null},
  												{new Vector(0.0, 0.0, -1.0), new Vector(0.0, 1.0, 0.0)},
  												{new Vector(0.0, 0.0, 1.0), new Vector(0.0, 1.0, 0.0)},
												{new Vector(0.0, 1.0, 0.0), new Vector(0.0, 0.0, 1.0)},
												{new Vector(0.0, -1.0, 0.0), new Vector(0.0, 0.0, -1.0)},
												{new Vector(-0.5, 0.0, -1.0), new Vector(0.0, 1.0, 0.0)},
												{new Vector(0.5, 0.0, -1.0), new Vector(0.0, 1.0, 0.0)},
												{new Vector(0.0, 0.5, -1.0), new Vector(0.0, 1.0, 0.0)},
												{new Vector(0.0, -0.5, -1.0), new Vector(0.0, 1.0, 0.0)}};
  
  private Scene scene;
  private OrthographicCamera camera;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JPanel typePanel, dataPanel, buttonPanel;
  
  private JPanel viewPanel;
  private EditVectorPanel locationPanel, viewDirectionPanel, upDirectionPanel;
  private JPanel focalLengthPanel, dimensionsPanel, fovPanel, lensPanel;
  
  private JComboBox typeBox;
  private JComboBox viewPresetsBox;
  private JFormattedTextField focalLengthField;
  private JFormattedTextField projectionWidthField, projectionHeightField;
  private JFormattedTextField hFovField, vFovField;
  private JComboBox angleMeasureBox;
  private JFormattedTextField radiusField;
  
  private JButton applyButton, okButton, cancelButton;
  
	/**
	 * Constructs a new EditCameraDialog object that can be used to modify the specified Camera object.
	 */
	public EditCameraDialog(Scene scene) {
		super(new BorderLayout());
		
		this.scene = scene;
		this.camera = (OrthographicCamera)scene.getCamera();
		
		this.typePanel = new JPanel(new GridLayout(1, 0));
		this.dataPanel = new JPanel(new GridLayout(0, 4));
		this.buttonPanel = new JPanel(new FlowLayout());
		
		this.typeBox = new JComboBox(EditCameraDialog.cameraTypes);
		
		if (this.camera instanceof ThinLensCamera) {
			this.typeBox.setSelectedIndex(2);
		} else if (this.camera instanceof PinholeCamera) {
			this.typeBox.setSelectedIndex(1);
		} else if (this.camera instanceof OrthographicCamera) {
			this.typeBox.setSelectedIndex(0);
		}
		
		this.typeBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (EditCameraDialog.this.typeBox.getSelectedIndex() == 0) {
					EditCameraDialog.this.camera = new OrthographicCamera();
				} else if (EditCameraDialog.this.typeBox.getSelectedIndex() == 1) {
					EditCameraDialog.this.camera = new PinholeCamera();
					EditCameraDialog.this.updateFocalLengthField();
					EditCameraDialog.this.updateDimensionsFields();
					EditCameraDialog.this.updateFovFields();
				} else if (EditCameraDialog.this.typeBox.getSelectedIndex() == 2) {
					EditCameraDialog.this.camera = new ThinLensCamera();
					EditCameraDialog.this.updateLensFields();
					EditCameraDialog.this.updateFocalLengthField();
					EditCameraDialog.this.updateDimensionsFields();
					EditCameraDialog.this.updateFovFields();
				} else {
					return;
				}
				
				EditCameraDialog.this.scene.setCamera(EditCameraDialog.this.camera);
				
				EditCameraDialog.this.updateDisplayedFields();
			}
		});
		
		this.typePanel.add(new JLabel("\tCamera type: "));
		this.typePanel.add(this.typeBox);
		
		this.locationPanel = new EditVectorPanel();
		this.locationPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Location"));
		this.locationPanel.add(new JLabel("      "));
		
		this.viewPanel = new JPanel(new BorderLayout());
		this.viewDirectionPanel = new EditVectorPanel();
		this.viewPresetsBox = new JComboBox(EditCameraDialog.viewPresetNames);
		this.viewPanel.add(this.viewDirectionPanel, BorderLayout.CENTER);
		this.viewPanel.add(this.viewPresetsBox, BorderLayout.SOUTH);
		this.viewPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Viewing Direction"));
		
		this.upDirectionPanel = new EditVectorPanel();
		this.upDirectionPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Up Direction"));
		this.upDirectionPanel.add(new JLabel("      "));
		
		this.focalLengthPanel = new JPanel(new FlowLayout());
		this.focalLengthPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Focal Length"));
		
		this.dimensionsPanel = new JPanel(new GridLayout(0, 2));
		this.dimensionsPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Projection Dimensions"));
		
		this.fovPanel = new JPanel(new GridLayout(0, 2));
		this.fovPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Field of View"));
		
		this.lensPanel = new JPanel(new FlowLayout());
		this.lensPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Lens"));
		
		this.focalLengthField = new JFormattedTextField(Settings.decimalFormat);
		this.focalLengthField.setColumns(6);
		
		this.projectionWidthField = new JFormattedTextField(Settings.decimalFormat);
		this.projectionHeightField = new JFormattedTextField(Settings.decimalFormat);
		this.projectionWidthField.setColumns(6);
		this.projectionHeightField.setColumns(6);
		
		this.hFovField = new JFormattedTextField(Settings.decimalFormat);
		this.vFovField = new JFormattedTextField(Settings.decimalFormat);
		this.hFovField.setColumns(6);
		this.vFovField.setColumns(6);
		
		this.radiusField = new JFormattedTextField(Settings.decimalFormat);
		this.radiusField.setColumns(6);
		
		this.applyButton = new JButton("Apply");
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.focalLengthPanel.add(new JLabel("Focal Length: "));
		this.focalLengthPanel.add(this.focalLengthField);
		
		this.dimensionsPanel.add(new JLabel("Width: "));
		this.dimensionsPanel.add(this.projectionWidthField);
		this.dimensionsPanel.add(new JLabel("Height: "));
		this.dimensionsPanel.add(this.projectionHeightField);
		this.dimensionsPanel.add(new JLabel("      "));
		this.dimensionsPanel.add(new JLabel("      "));
		this.dimensionsPanel.add(new JLabel("      "));
		this.dimensionsPanel.add(new JLabel("      "));
		
		this.fovPanel.add(new JLabel("Horizontal: "));
		this.fovPanel.add(this.hFovField);
		this.fovPanel.add(new JLabel("Vertical:"));
		this.fovPanel.add(this.vFovField);
		this.fovPanel.add(new JLabel("      "));
		this.fovPanel.add(new JLabel("      "));
		
		this.lensPanel.add(new JLabel("Radius: "));
		this.lensPanel.add(this.radiusField);
		
		this.angleMeasureBox = new JComboBox(new String[] {"Radians", "Degrees"});
		this.angleMeasureBox.addItemListener(new java.awt.event.ItemListener() {
			int lastSelection = angleMeasureBox.getSelectedIndex();
			
			public void itemStateChanged(java.awt.event.ItemEvent event) {
				double hAngle = 0, vAngle = 0;
				
				if (EditCameraDialog.this.hFovField.getText().equals("") == false)
					hAngle = ((Number)EditCameraDialog.this.hFovField.getValue()).doubleValue();
				
				if (EditCameraDialog.this.vFovField.getText().equals("") == false)
					vAngle = ((Number)EditCameraDialog.this.vFovField.getValue()).doubleValue();
				
				if (lastSelection == angleMeasureBox.getSelectedIndex()) return;
				
				if (lastSelection == 1) {
					hAngle = Math.toRadians(hAngle);
					vAngle = Math.toRadians(vAngle);
					
					lastSelection = 0;
				} else if (lastSelection == 0) {
					hAngle = Math.toDegrees(hAngle);
					vAngle = Math.toDegrees(vAngle);
					
					lastSelection = 1;
				}
				
				if (EditCameraDialog.this.hFovField.getText().equals("") == false)
					EditCameraDialog.this.hFovField.setValue(new Double(hAngle));
				
				if (EditCameraDialog.this.vFovField.getText().equals("") == false)
					EditCameraDialog.this.vFovField.setValue(new Double(vAngle));
			}
		});
		
		this.fovPanel.add(this.angleMeasureBox);
		
		InputVerifier v = new InputVerifier() {
			public boolean verify(JComponent source) {
				JFormattedTextField field = (JFormattedTextField)source;
				AbstractFormatter formatter = field.getFormatter();
				
				if (formatter != null) {
					String text = field.getText();
					
					try {
						formatter.stringToValue(text);
						field.commitEdit();
					} catch (ParseException pe) {
						return false;
					}
				}
				
				if (source == EditCameraDialog.this.hFovField ||
					source == EditCameraDialog.this.vFovField) {
					
					double h = ((Number)EditCameraDialog.this.hFovField.getValue()).doubleValue();
					double v = ((Number)EditCameraDialog.this.vFovField.getValue()).doubleValue();
					
					if (((String)EditCameraDialog.this.angleMeasureBox.getSelectedItem()).equalsIgnoreCase("degrees")) {
						h = Math.toRadians(h);
						v = Math.toRadians(v);
					}
					
					double f = ((Number)EditCameraDialog.this.focalLengthField.getValue()).doubleValue();
					
					double width = 2.0 * f * Math.tan(h / 2.0);
					double height = 2.0 * f * Math.tan(v / 2.0);
					
					EditCameraDialog.this.projectionWidthField.setValue(new Double(width));
					EditCameraDialog.this.projectionHeightField.setValue(new Double(height));
				} else if (source == EditCameraDialog.this.projectionWidthField ||
						source == EditCameraDialog.this.projectionHeightField ||
						source == EditCameraDialog.this.focalLengthField) {
					
					EditCameraDialog.this.updateFovFields();
				}
				
				return true;
			}
			
			// public boolean shouldYieldFocus(JComponent source) { return true; }
		};
		
		this.projectionWidthField.setInputVerifier(v);
		this.projectionHeightField.setInputVerifier(v);
		this.hFovField.setInputVerifier(v);
		this.vFovField.setInputVerifier(v);
		this.focalLengthField.setInputVerifier(v);
		
		this.dataPanel.add(this.locationPanel);
		this.dataPanel.add(this.viewPanel);
		this.dataPanel.add(this.upDirectionPanel);
		this.dataPanel.add(this.dimensionsPanel);
		this.dataPanel.add(this.focalLengthPanel);
		this.dataPanel.add(this.fovPanel);
		this.dataPanel.add(this.lensPanel);
		
		this.buttonPanel.add(this.applyButton);
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(typePanel, BorderLayout.NORTH);
		this.add(dataPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		this.frame = new JFrame("Edit Camera");
		this.frame.getContentPane().add(this);
		this.frame.setSize(700, 360);
		
		this.viewPresetsBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				int i = EditCameraDialog.this.viewPresetsBox.getSelectedIndex();
				
				if (i > 0) {
					EditCameraDialog.this.viewDirectionPanel.setSelectedVector(EditCameraDialog.viewPresets[i][0]);
					EditCameraDialog.this.upDirectionPanel.setSelectedVector(EditCameraDialog.viewPresets[i][1]);
				}
			}
		});
		
		KeyAdapter k = new KeyAdapter() {
			public void keyTyped(KeyEvent event) {
				EditCameraDialog.this.viewPresetsBox.setSelectedIndex(0);
			}
		};
		
		this.viewDirectionPanel.addKeyListener(k);
		this.upDirectionPanel.addKeyListener(k);
		
		this.applyButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
			}
		});
		
		this.okButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				apply();
				closeDialog();
			}
		});
		
		this.cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				closeDialog();
			}
		});
		
		this.frame.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent event) {
				closeDialog();
			}
		});
	}
	
	/**
	 * Shows this dialog in a JFrame.
	 */
	public void showDialog() {
		if (this.open == true)
			return;
		
		this.updateDisplayedFields();
		this.updateAllFields();
		this.frame.setVisible(true);
		this.open = true;
	}
	
	/**
	 * Closes (and disposes) this dialog if it is open.
	 */
	public void closeDialog() {
		if (this.open == true) {
			this.frame.setVisible(false);
			this.open = false;
			
			this.frame.dispose();
			
			if (this.handler != null)
				this.handler.fireEvent(new DialogCloseEvent(this));
		}
	}
	
	/**
	 * Applies the changes made in this dialog to the Camera object being edited and fires the necessary events
	 * if the current EventHandler is not set to null.
	 */
	public void apply() {
		int eventCode = 0;
		
		Vector location = this.locationPanel.getSelectedVector();
		
		if (this.camera.getLocation().equals(location) == false) {
			this.camera.setLocation(location);
			eventCode += CameraEditEvent.locationChangeEvent;
		}
		
		Vector viewDirection = this.viewDirectionPanel.getSelectedVector();
		
		if (this.camera.getViewingDirection().equals(viewDirection) == false) {
			this.camera.setViewingDirection(viewDirection);
			eventCode += CameraEditEvent.viewingDirectionChangeEvent;
		}
		
		Vector upDirection = this.upDirectionPanel.getSelectedVector();
		
		if (this.camera.getUpDirection().equals(upDirection) == false) {
			this.camera.setUpDirection(upDirection);
			eventCode += CameraEditEvent.upDirectionChangeEvent;
		}
		
		double focalLength = ((Number)this.focalLengthField.getValue()).doubleValue();
		
		if (this.camera instanceof PinholeCamera && focalLength != ((PinholeCamera)this.camera).getFocalLength()) {
			((PinholeCamera)this.camera).setFocalLength(focalLength);
			eventCode += CameraEditEvent.focalLengthChangeEvent;
		}
		
		double projectionWidth = ((Number)this.projectionWidthField.getValue()).doubleValue();
		double projectionHeight = ((Number)this.projectionHeightField.getValue()).doubleValue();
		
		if (projectionWidth != this.camera.getProjectionWidth() || projectionHeight != this.camera.getProjectionHeight()) {
			this.camera.setProjectionDimensions(projectionWidth, projectionHeight);
			eventCode += CameraEditEvent.projectionDimensionsChangeEvent;
		}
		
		if (this.camera instanceof ThinLensCamera) {
			double radius = ((Number)this.radiusField.getValue()).doubleValue();
			((ThinLensCamera)this.camera).setLensRadius(radius);
		}
		
		if (this.handler != null) {
			this.handler.fireEvent(new CameraEditEvent(eventCode, this.camera));
		}
		
		this.updateAllFields();
	}
	
	private void updateDisplayedFields() {
		if (this.camera instanceof ThinLensCamera) {
			this.lensPanel.setVisible(true);
			this.fovPanel.setVisible(true);
			this.focalLengthPanel.setVisible(true);
		} else if (this.camera instanceof PinholeCamera) {
			this.lensPanel.setVisible(false);
			this.fovPanel.setVisible(true);
			this.focalLengthPanel.setVisible(true);
		} else if (this.camera instanceof OrthographicCamera) {
			this.lensPanel.setVisible(false);
			this.fovPanel.setVisible(false);
			this.focalLengthPanel.setVisible(false);
		}
		
		this.validate();
	}
	
	/**
	 * Updates all fields of this dialog so that they match the Camera object being edited.
	 */
	public void updateAllFields() {
		this.updateLocationFields();
		this.updateViewingDirectionFields();
		this.updateUpDirectionFields();
		this.updateFocalLengthField();
		this.updateDimensionsFields();
		this.updateFovFields();
		this.updateLensFields();
	}
	
	/**
	 * Updates the location fields of this dialog so that they match the location of the
	 * Camera object being edited.
	 */
	public void updateLocationFields() {
		this.locationPanel.setSelectedVector(this.camera.getLocation());
	}
	
	/**
	 * Updates the viewing direction fields of this dialog so that they match the viewing direction
	 * of the Camera object being edited.
	 */
	public void updateViewingDirectionFields() {
		Vector v = this.camera.getViewingDirection();
		Vector u = this.camera.getUpDirection();
		
		this.viewDirectionPanel.setSelectedVector(this.camera.getViewingDirection());
		
		i: for (int i = 1; i < EditCameraDialog.viewPresets.length; i++) {
			if (EditCameraDialog.viewPresets[i][0].equals(v) && EditCameraDialog.viewPresets[i][1].equals(u)) {
				this.viewPresetsBox.setSelectedIndex(i);
				break i;
			}
		}
	}
	
	/**
	 * Updates the up direction fields of this dialog so that they match the up direction of the
	 * Camera object being edited.
	 */
	public void updateUpDirectionFields() {
		this.upDirectionPanel.setSelectedVector(this.camera.getUpDirection());
	}
	
	/**
	 * Updates the focal length field of this dialog so that it matches the focal length
	 * of the Camera object being edited.
	 */
	public void updateFocalLengthField() {
		if (this.camera instanceof PinholeCamera)
			this.focalLengthField.setValue(new Double(((PinholeCamera)this.camera).getFocalLength()));
	}
	
	/**
	 * Updates the dimensions fields of this dialog so that they match the projection dimensions
	 * of the Camera object being edited.
	 */
	public void updateDimensionsFields() {
		this.projectionWidthField.setValue(new Double(this.camera.getProjectionWidth()));
		this.projectionHeightField.setValue(new Double(this.camera.getProjectionHeight()));
	}
	
	/**
	 * Updates the FOV fields of this dialog so that they match the FOV values of the Camera
	 * object being edited.
	 */
	public void updateFovFields() {
		double w = ((Number)this.projectionWidthField.getValue()).doubleValue();
		double h = ((Number)this.projectionHeightField.getValue()).doubleValue();
		double f = ((Number)this.focalLengthField.getValue()).doubleValue();
		
		double hf = 2.0 * Math.atan(w / (2.0 * f));
		double vf = 2.0 * Math.atan(h / (2.0 * f));
		
		if (((String)EditCameraDialog.this.angleMeasureBox.getSelectedItem()).equalsIgnoreCase("degrees")) {
			hf = Math.toDegrees(hf);
			vf = Math.toDegrees(vf);
		}
		
		this.hFovField.setValue(new Double(hf));
		this.vFovField.setValue(new Double(vf));
	}
	
	/**
	 * Updates the lens fields of this dialog so that they match the lens info of the Camera
	 * object being edited if the Camera is an instance of ThinLensCamera.
	 */
	public void updateLensFields() {
		if (!(this.camera instanceof ThinLensCamera)) return;
		
		this.radiusField.setValue(new Double(((ThinLensCamera)this.camera).getLensRadius()));
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (this.open == false)
			return;
		
		if (event instanceof CameraEvent && ((CameraEvent)event).getTarget() != this.camera)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent) {
			this.closeDialog();
			return;
		}
		
		if (event instanceof CameraEditEvent) {
			CameraEditEvent editEvent = (CameraEditEvent)event;
			
			if (editEvent.isLocationChangeEvent() == true) {
				this.updateLocationFields();
			}
			
			if (editEvent.isViewingDirectionChangeEvent() == true) {
				this.updateViewingDirectionFields();
			}
			
			if (editEvent.isUpDirectionChangeEvent() == true) {
				this.updateUpDirectionFields();
			}
			
			if (editEvent.isFocalLengthChangeEvent() == true) {
				this.updateFocalLengthField();
			}
			
			if (editEvent.isProjectionDimensionsChangeEvent()) {
				this.updateDimensionsFields();
			}
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditCameraDialog object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Returns the EventHandler object used by this EditCameraDialog object.
	 */
	public EventHandler getEventHandler() {
		return this.handler;
	}
	
	public void finalize() { if (this.handler != null) this.handler.removeListener(this); }
}
