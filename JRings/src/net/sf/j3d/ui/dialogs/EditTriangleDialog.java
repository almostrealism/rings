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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.primitives.Triangle;
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
import net.sf.j3d.ui.panels.EditVectorPanel;
import net.sf.j3d.util.Vector;


/**
 * An EditTriangleDialog object can be used to allow a user to specify the vertices of a Triangle object
 * that is wrapped by a SurfaceUI object.
 */
public class EditTriangleDialog extends JPanel implements Dialog, EventListener, EventGenerator {
  private Triangle tri;
  
  private boolean open;
  
  private EventHandler handler;
  
  private JFrame frame;
  
  private EditVectorPanel vertex1Panel, vertex2Panel, vertex3Panel;
  private JPanel dataPanel, buttonPanel;
  
  private JButton applyButton, okButton, cancelButton;

  	/**
  	 * Constructs a new EditTriangleDialog object that can be used to edit the specified SurfaceUI object.
  	 * 
  	 * @throws IllegalArgumentException  If the specified SurfaceUI object does not wrap a Triangle object.
  	 */
	public EditTriangleDialog(SurfaceUI triangle) {
		Surface s = triangle.getSurface();
		
		if (s instanceof Triangle)
			this.tri = (Triangle)s;
		else
			throw new IllegalArgumentException("SurfaceUI object does not wrap a Triangle object.");
		
		this.vertex1Panel = new EditVectorPanel();
		this.vertex1Panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Vertex 1:"));
		
		this.vertex2Panel = new EditVectorPanel();
		this.vertex2Panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Vertex 2:"));
		
		this.vertex3Panel = new EditVectorPanel();
		this.vertex3Panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Vertex 3:"));
		
		this.dataPanel = new JPanel(new java.awt.FlowLayout());
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		
		this.applyButton = new JButton("Apply");
		this.okButton = new JButton("OK");
		this.cancelButton = new JButton("Cancel");
		
		this.dataPanel.add(this.vertex1Panel);
		this.dataPanel.add(this.vertex2Panel);
		this.dataPanel.add(this.vertex3Panel);
		
		this.buttonPanel.add(this.applyButton);
		this.buttonPanel.add(this.okButton);
		this.buttonPanel.add(this.cancelButton);
		
		this.add(this.dataPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.frame = new JFrame("Edit Triangle Vertices");
		this.frame.getContentPane().add(this);
		this.frame.setSize(600, 250);
		
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
		
		this.updateVertexFields();
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
	 * Applies the changes made in this dialog to the TriangleUI object being edited and fires the necessary events
	 * if the current EventHandler is not set to null.
	 */
	public void apply() {
		boolean change = false;
		
		Vector p1 = this.vertex1Panel.getSelectedVector();
		Vector p2 = this.vertex2Panel.getSelectedVector();
		Vector p3 = this.vertex3Panel.getSelectedVector();
		
		Vector t[] = this.tri.getVertices();
		
		if (t[0].equals(p1) == false) {
			change = true;
		} else {
			p1 = t[0];
		}
		
		if (t[1].equals(p2) == false) {
			change = true;
		} else {
			p2 = t[1];
		}
		
		if (t[2].equals(p3) == false) {
			change = true;
		} else {
			p3 = t[2];
		}
		
		if (change == true) {
			this.tri.setVertices(p1, p2, p3);
			
			if (this.handler != null) {
				this.handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.dataChangeEvent, this.tri));
			}
		}
		
		this.updateVertexFields();
	}
	
	/**
	 * Updates the vertex fields of this dialog so that they match the vertex data of the TriangleUI object being edited.
	 */
	public void updateVertexFields() {
		Vector v[] = this.tri.getVertices();
		
		this.vertex1Panel.setSelectedVector(v[0]);
		this.vertex2Panel.setSelectedVector(v[1]);
		this.vertex3Panel.setSelectedVector(v[2]);
	}
	
	/**
	 * Method called when an event has been fired.
	 */
	public void eventFired(Event event) {
		if (this.open == false)
			return;
		
		if (event instanceof SurfaceEvent && ((SurfaceEvent)event).getTarget() != this.tri)
			return;
		
		if (event instanceof SceneOpenEvent || event instanceof SceneCloseEvent || event instanceof SurfaceRemoveEvent) {
			this.closeDialog();
			return;
		}
		
		if (event instanceof SurfaceEditEvent && ((SurfaceEditEvent)event).isDataChangeEvent() == true) {
			this.updateVertexFields();
		}
	}
	
	/**
	 * Sets the EventHandler object used by this EditTriangleDialog object. Setting this to null will deactivate event reporting.
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
