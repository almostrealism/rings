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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.primitives.Plane;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;
import net.sf.j3d.ui.event.DialogCloseEvent;
import net.sf.j3d.ui.event.Event;
import net.sf.j3d.ui.event.EventGenerator;
import net.sf.j3d.ui.event.EventHandler;
import net.sf.j3d.ui.event.EventListener;
import net.sf.j3d.ui.event.SceneCloseEvent;
import net.sf.j3d.ui.event.SceneOpenEvent;
import net.sf.j3d.ui.event.SurfaceEditEvent;
import net.sf.j3d.ui.event.SurfaceEvent;
import net.sf.j3d.ui.event.SurfaceRemoveEvent;


/**
 * An EditPlaneDialog object can be used to allow a user to specify the orientation of a Plane object
 * that is wrapped by a SurfaceUI object.
 */
public class EditPlaneDialog extends JPanel implements Dialog, EventListener, EventGenerator {
  private Plane plane;
  
  private boolean open;
  
  private EventHandler handler;
  
  private JFrame frame;
  private JPanel editPanel, buttonPanel;
  private JRadioButton xyOption, xzOption, yzOption;
  private JButton okButton, cancelButton;

	/**
	 * Constructs a new EditPlaneDialog object that can be used to modify the specified SurfaceUI object.
	 * 
	 * @throws IllegalArgumentException  If the specified SurfaceUI object does not wrap a Plane object.
	 */
	public EditPlaneDialog(SurfaceUI plane) {
		super(new java.awt.BorderLayout());
		
		Surface s = plane.getSurface();
		
		if (s instanceof Plane)
			this.plane = (Plane)plane.getSurface();
		else
			throw new IllegalArgumentException("SurfaceUI object does not wrap a Plane object.");
		
		this.editPanel = new JPanel(new java.awt.GridLayout(0, 1));
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.xyOption = new JRadioButton("XY");
		this.xzOption = new JRadioButton("XZ");
		this.yzOption = new JRadioButton("YZ");
		
		ButtonGroup group = new ButtonGroup();
		group.add(this.xyOption);
		group.add(this.xzOption);
		group.add(this.yzOption);
		
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.editPanel.add(this.xyOption);
		this.editPanel.add(this.xzOption);
		this.editPanel.add(this.yzOption);
		
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.editPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.frame = new JFrame("Edit Plane");
		this.frame.getContentPane().add(this);
		this.frame.setSize(150, 175);
		
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
		
		this.updateOptions();
		this.frame.setVisible(true);
		this.open = true;
	}
	
	/**
	 * Closes (and disposes) the JFrame this dialog is displayed in.
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
	 * Applies the changes made in the dialog to the PlaneUI object being edited and fires the necessary events
	 * if the current EventHandler is not set to null.
	 */
	public void apply() {
		int selection = 0;
		
		if (this.xyOption.isSelected())
			selection = Plane.XY;
		else if (this.xzOption.isSelected())
			selection = Plane.XZ;
		else if (this.yzOption.isSelected())
			selection = Plane.YZ;
		else
			return;
		
		if (selection != this.plane.getType()) {
			this.plane.setType(selection);
			
			if (this.handler != null)
				this.handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.dataChangeEvent, this.plane));
			
			this.updateOptions();
		}
	}
	
	/**
	 * Updates the option selected in this dialog so it matches the PlaneUI object being edited.
	 */
	public void updateOptions() {
		int type = this.plane.getType();
		
		if (type == Plane.XY)
			this.xyOption.setSelected(true);
		else if (type == Plane.XZ)
			this.xzOption.setSelected(true);
		else if (type == Plane.YZ)
			this.yzOption.setSelected(true);
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (this.open == false)
			return;
		
		if (event instanceof SurfaceEvent && ((SurfaceEvent)event).getTarget() != this.plane)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent || event instanceof SurfaceRemoveEvent) {
			this.closeDialog();
			return;
		}
		
		if (event instanceof SurfaceEditEvent && ((SurfaceEditEvent)event).isDataChangeEvent() == true) {
			this.updateOptions();
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditPlaneDialog object. Setting this to null
	 * will deactivate event reporting.
	 */
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Returns the EventHandler object used by this EditTriangleDialog object.
	 */
	public EventHandler getEventHandler() {
		return this.handler;
	}
}
