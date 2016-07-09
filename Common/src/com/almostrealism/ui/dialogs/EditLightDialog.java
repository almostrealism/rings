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

package com.almostrealism.ui.dialogs;

import java.awt.FlowLayout;
import java.awt.GridLayout;






import javax.swing.*;
import javax.swing.border.*;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.graphics.*;
import com.almostrealism.raytracer.lighting.*;
import com.almostrealism.ui.event.*;
import com.almostrealism.ui.panels.*;
import com.almostrealism.util.*;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.run.Settings;

// TODO  Add presets for attenuation constants

/**
 * An EditLightDialog object can be used to gather input from a user
 * to specify the settings for a Light object.
 * 
 * @author Mike Murray
 */
public class EditLightDialog extends JPanel implements Dialog, EventListener, EventGenerator {
  private Light light;
  
  private EventHandler handler;
  
  private boolean open;
  
  private JFrame frame;
  
  private JPanel dataPanel, buttonPanel;
  private EditVectorPanel locationPanel, directionPanel;
  private PercentagePanel intensityPanel;
  private JPanel samplesPanel, attenuationPanel;
  private EditRGBPanel colorPanel;
  
  private JFormattedTextField attenuationAField, attenuationBField, attenuationCField;
  private JFormattedTextField samplesField, widthField, heightField, radiusField;
  
  private JButton applyButton, okButton, cancelButton;

	/**
	 * Constructs a new EditLightDialog object that can be used to modify the specified Light object.
	 */
	public EditLightDialog(Light light) {
		this.light = light;
		
		if (this.light instanceof PointLight ||
				this.light instanceof SphericalLight ||
				this.light instanceof RectangularLight) {
			this.dataPanel = new JPanel(new GridLayout(2, 2));
		} else {
			this.dataPanel = new JPanel(new GridLayout(1, 0));
		}
		
		this.buttonPanel = new JPanel(new FlowLayout());
		
		if (this.light instanceof PointLight ||
				this.light instanceof SphericalLight ||
				this.light instanceof RectangularLight) {
			this.locationPanel = new EditVectorPanel();
			this.locationPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Location"));
			this.locationPanel.add(new JLabel("      "));
			
			this.dataPanel.add(this.locationPanel);
		}
		
		if (	this.light instanceof SphericalLight ||
				this.light instanceof RectangularLight) {
			this.samplesPanel = new JPanel(new java.awt.GridLayout(0, 2));
			this.samplesPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Samples"));
		} else if (this.light instanceof PointLight) {
			this.attenuationPanel = new JPanel(new java.awt.GridLayout(0, 2));
			this.attenuationPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Attenuation Coefficients"));
		}
		
		if (this.light instanceof DirectionalAmbientLight) {
			this.directionPanel = new EditVectorPanel();
			this.directionPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Direction"));
			this.directionPanel.add(new JLabel("      "));
			
			this.dataPanel.add(this.directionPanel);
		}
		
		this.colorPanel = new EditRGBPanel();
		this.colorPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Color"));
		
		this.intensityPanel = new PercentagePanel();
		this.intensityPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Intensity"));
		
		this.intensityPanel.add(new JLabel("    "));
		this.intensityPanel.add(new JLabel("    "));
		
		if (this.light instanceof PointLight) {
			this.attenuationAField = new JFormattedTextField(Settings.decimalFormat);
			this.attenuationBField = new JFormattedTextField(Settings.decimalFormat);
			this.attenuationCField = new JFormattedTextField(Settings.decimalFormat);
			
			this.attenuationAField.setColumns(6);
			this.attenuationBField.setColumns(6);
			this.attenuationCField.setColumns(6);
			
			this.attenuationPanel.add(new JLabel("A (distance squared) = "));
			this.attenuationPanel.add(this.attenuationAField);
			this.attenuationPanel.add(new JLabel("B (distance) = "));
			this.attenuationPanel.add(this.attenuationBField);
			this.attenuationPanel.add(new JLabel("C (constant) = "));
			this.attenuationPanel.add(this.attenuationCField);
			this.attenuationPanel.add(new JLabel("      "));
			
			this.dataPanel.add(this.attenuationPanel);
		} else if (this.light instanceof SphericalLight ||
					this.light instanceof RectangularLight) {
			this.samplesField = new JFormattedTextField(Settings.integerFormat);
			this.samplesField.setColumns(6);
			
			this.samplesPanel.add(new JLabel("Sample count = "));
			this.samplesPanel.add(this.samplesField);
			
			if (this.light instanceof SphericalLight) {
				this.radiusField = new JFormattedTextField(Settings.decimalFormat);
				this.samplesField.setColumns(6);
				
				this.samplesPanel.add(new JLabel("Radius = "));
				this.samplesPanel.add(this.radiusField);
				
				this.samplesPanel.add(new JLabel("      "));
				this.samplesPanel.add(new JLabel("      "));
				this.samplesPanel.add(new JLabel("      "));
			} else if (this.light instanceof RectangularLight) {
				this.widthField = new JFormattedTextField(Settings.decimalFormat);
				this.heightField = new JFormattedTextField(Settings.decimalFormat);
				this.widthField.setColumns(6);
				this.heightField.setColumns(6);
				
				this.samplesPanel.add(new JLabel("Width = "));
				this.samplesPanel.add(this.widthField);
				this.samplesPanel.add(new JLabel("Height = "));
				this.samplesPanel.add(this.heightField);
				
				this.samplesPanel.add(new JLabel("      "));
			}
			
			this.dataPanel.add(this.samplesPanel);
		}
		
		this.applyButton = new JButton("Apply");
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		if (this.light instanceof DirectionalAmbientLight) {
			this.dataPanel.add(this.directionPanel);
		}
		
		this.dataPanel.add(this.colorPanel);
		this.dataPanel.add(this.intensityPanel);
		
		this.buttonPanel.add(this.applyButton);
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.dataPanel);
		this.add(this.buttonPanel);
		
		this.frame = new JFrame("Edit Light");
		
		if (this.light instanceof DirectionalAmbientLight) {
			this.frame.setSize(600, 210);
		} else if (this.light instanceof AmbientLight) {
			this.frame.setSize(400, 210);
		} else if (this.light instanceof PointLight ||
				this.light instanceof SphericalLight ||
				this.light instanceof RectangularLight) {
			this.frame.setSize(600, 420);
		}
		
		this.frame.getContentPane().add(this);
		
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
	 * Applies the changes made in this dialog to the Light object being edited and fires the necessary events
	 * if the current EventHandler is not set to null.
	 */
	public void apply() {
		int eventCode = 0;
		
		if (this.light instanceof SphericalLight ||
				this.light instanceof RectangularLight) {
			AbstractSurface s = (AbstractSurface)this.light;
			
			Vector newLocation = this.locationPanel.getSelectedVector();
			
			if (!newLocation.equals(s.getLocation())) {
				s.setLocation(newLocation);
				eventCode += LightEditEvent.locationChangeEvent;
			}
			
			int samples = ((Number)this.samplesField.getValue()).intValue();
			
			if (s instanceof SphericalLight) {
				((SphericalLight)s).setSampleCount(samples);
				
				((SphericalLight)s).setSize(((Number)this.radiusField.getValue()).doubleValue());
			} else if (s instanceof RectangularLight) {
				((RectangularLight)s).setSampleCount(samples);
				
				((RectangularLight)s).setWidth(((Number)this.widthField.getValue()).doubleValue());
				((RectangularLight)s).setHeight(((Number)this.heightField.getValue()).doubleValue());
			}
		}
		
		if (this.light instanceof PointLight) {
			PointLight pointLight = (PointLight)this.light;
			
			Vector newLocation = this.locationPanel.getSelectedVector();
			
			if (newLocation.equals(pointLight.getLocation()) == false) {
				pointLight.setLocation(newLocation);
				eventCode += LightEditEvent.locationChangeEvent;
			}
			
			double da = ((Number)this.attenuationAField.getValue()).doubleValue();
			double db = ((Number)this.attenuationBField.getValue()).doubleValue();
			double dc = ((Number)this.attenuationCField.getValue()).doubleValue();
			
			double d[] = pointLight.getAttenuationCoefficients();
			
			if (da != d[0] || db != d[1] || dc != d[2]) {
				pointLight.setAttenuationCoefficients(da, db, dc);
				eventCode += LightEditEvent.attenuationCoefficientChangeEvent;
			}
		}
		
		if (this.light instanceof DirectionalAmbientLight) {
			DirectionalAmbientLight directionalLight = (DirectionalAmbientLight)this.light;
			
			Vector newDirection = this.directionPanel.getSelectedVector();
			
			if (newDirection.equals(directionalLight) == false) {
				directionalLight.setDirection(newDirection);
				eventCode += LightEditEvent.directionChangeEvent;
			}
		}
		
		RGB color = this.colorPanel.getSelectedColor();
		
		if (color.equals(this.light.getColor()) == false) {
			this.light.setColor(color);
			eventCode += LightEditEvent.colorChangeEvent;
		}
		
		double intensity = this.intensityPanel.getValue();
		
		if (intensity != this.light.getIntensity()) {
			this.light.setIntensity(intensity);
			eventCode += LightEditEvent.intensityChangeEvent;
		}
		
		if (this.handler != null) {
			this.handler.fireEvent(new LightEditEvent(eventCode, this.light));
		}
		
		this.updateAllFields();
	}
	
	/**
	 * Updates all fields of this dialog so that they match the Light object being edited.
	 */
	public void updateAllFields() {
		this.updateLocationFields();
		this.updateAttenuationFields();
		this.updateSampleFields();
		this.updateDirectionFields();
		this.updateColorFields();
		this.updateIntensityField();
	}
	
	/**
	 * Updates the location fields of this dialog so that the match the Light object being edited.
	 */
	public void updateLocationFields() {
		if (this.light instanceof PointLight) {
			this.locationPanel.setSelectedVector(((PointLight)this.light).getLocation());
		} else if (this.light instanceof SurfaceLight) {
			this.locationPanel.setSelectedVector(((AbstractSurface)this.light).getLocation());
		}
	}
	
	/**
	 * Updates the attenuation coefficient fields of this dialog so that they match the Light object
	 * being edited.
	 */
	public void updateAttenuationFields() {
		if (this.light instanceof PointLight) {
			double d[] = ((PointLight)this.light).getAttenuationCoefficients();
			
			this.attenuationAField.setValue(new Double(d[0]));
			this.attenuationBField.setValue(new Double(d[1]));
			this.attenuationCField.setValue(new Double(d[2]));
		}
	}
	
	/**
	 * Updates the sample fields of this dialog so that they match the Light object
	 * being edited.
	 */
	public void updateSampleFields() {
		if (this.light instanceof SphericalLight) {
			this.samplesField.setValue(new Integer(((SphericalLight)this.light).getSampleCount()));
			
			this.radiusField.setValue(new Double(((SphericalLight)this.light).getSize()));
		} else if (this.light instanceof RectangularLight) {
			this.samplesField.setValue(new Integer(((RectangularLight)this.light).getSampleCount()));
			
			this.widthField.setValue(new Double(((RectangularLight)this.light).getWidth()));
			this.heightField.setValue(new Double(((RectangularLight)this.light).getHeight()));
		}
	}
	
	/**
	 * Updates the direction fields of this dialog so that they match the Light object being edited.
	 */
	public void updateDirectionFields() {
		if (this.light instanceof DirectionalAmbientLight)
			this.directionPanel.setSelectedVector(((DirectionalAmbientLight)this.light).getDirection());
	}
	
	/**
	 * Updates the color fields of this dialog so that they match the Light object being edited.
	 */
	public void updateColorFields() {
		this.colorPanel.setSelectedColor(this.light.getColor());
	}
	
	/**
	 * Updates the intensity field of this dialog so that it matches the Light object being edited.
	 */
	public void updateIntensityField() {
		this.intensityPanel.setValue(this.light.getIntensity());
	}
	
	/**
	 * Method called when an event is fired.
	 */
	public void eventFired(Event event) {
		if (this.open == false)
			return;
		
		if (event instanceof LightEvent && ((LightEvent)event).getTarget() != this.light)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent) {
			this.closeDialog();
			return;
		}
		
		if (event instanceof LightEditEvent) {
			LightEditEvent editEvent = (LightEditEvent)event;
			
			if (editEvent.isLocationChangeEvent() == true) {
				this.updateLocationFields();
			}
			
			if (editEvent.isAttenuationCoefficientChangeEvent() == true) {
				this.updateAttenuationFields();
			}
			
			if (editEvent.isDirectionChangeEvent() == true) {
				this.updateDirectionFields();
			}
			
			if (editEvent.isColorChangeEvent() == true) {
				this.updateColorFields();
			}
			
			if (editEvent.isIntensityChangeEvent() == true) {
				this.updateIntensityField();
			}
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditLightDialog object. Setting this to null will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) { this.handler = handler; }
	
	/**
	 * Returns the EventHandler object used by this EditLightDialog object.
	 */
	public EventHandler getEventHandler() { return this.handler; }
}
