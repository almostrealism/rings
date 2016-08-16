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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.almostrealism.space.Vector;
import org.almostrealism.swing.Dialog;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;
import org.almostrealism.swing.dialogs.DialogCloseEvent;
import org.almostrealism.swing.panels.EditVectorPanel;

import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.primitives.Triangle;
import com.almostrealism.raytracer.surfaceUI.SurfaceUI;


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
		ShadableSurface s = triangle.getSurface();
		
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
