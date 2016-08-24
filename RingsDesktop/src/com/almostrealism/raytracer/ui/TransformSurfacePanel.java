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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.almostrealism.space.TransformMatrix;
import org.almostrealism.swing.DynamicDisplay;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventGenerator;
import org.almostrealism.swing.EventHandler;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.event.SurfaceEditEvent;

/**
 * A {@link TransformSurfacePanel} can be used to allow the user to
 * modify an {@link AbstractSurface} object's transformations.
 */
public class TransformSurfacePanel extends JPanel implements DynamicDisplay, EventListener, EventGenerator {
  private AbstractSurface surface;
  
  private EventHandler handler;
  
  private TransformationsListModel listModel;
  
  private JPanel transformationsPanel, buttonPanel;
  private JPanel movePanel;
  
  private JList transformationsList;
  private JScrollPane transformationsListScrollPane;
  
  private JButton addButton, editButton, removeButton;
  private JButton moveUpButton, moveDownButton;

	/**
	  Constructs a new TransformSurfacePanel object that can be used to modify the specified AbstractSurface object.
	*/
	
	public TransformSurfacePanel(AbstractSurface s) {
		super(new java.awt.BorderLayout());
		
		this.transformationsPanel = new JPanel(new java.awt.BorderLayout());
		this.transformationsPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Transformations:"));
		
		this.buttonPanel = new JPanel(new java.awt.FlowLayout());
		this.movePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
		
		this.transformationsList = new JList();
		this.transformationsList.setPrototypeCellValue("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		this.transformationsList.setFixedCellWidth(30);
		this.transformationsListScrollPane = new JScrollPane(this.transformationsList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		this.addButton = new JButton("Add");
		this.editButton = new JButton("Edit");
		this.removeButton = new JButton("Remove");
		
		this.moveUpButton = new JButton("Move Up");
		this.moveDownButton = new JButton("Move Down");
		
		this.movePanel.add(this.moveUpButton);
		this.movePanel.add(this.moveDownButton);
		
		this.transformationsPanel.add(this.transformationsListScrollPane, java.awt.BorderLayout.CENTER);
		this.transformationsPanel.add(this.movePanel, java.awt.BorderLayout.SOUTH);
		
		this.buttonPanel.add(this.addButton);
		this.buttonPanel.add(this.editButton);
		this.buttonPanel.add(this.removeButton);
		
		this.add(this.transformationsPanel, java.awt.BorderLayout.CENTER);
		this.add(this.buttonPanel, java.awt.BorderLayout.SOUTH);
		
		this.setSurface(s);
		
		this.addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				AddTransformDialog addDialog = new AddTransformDialog(surface, TransformSurfacePanel.this);
				addDialog.setEventHandler(handler);
				
				addDialog.showDialog();
			}
		});
		
		this.editButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (transformationsList.isSelectionEmpty() == true)
					return;
				
				int index = transformationsList.getSelectedIndex();
				
				EditTransformDialog editDialog = new EditTransformDialog(surface, index, TransformSurfacePanel.this);
				editDialog.setEventHandler(handler);
				
				editDialog.showDialog();
			}
		});
		
		this.removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (transformationsList.isSelectionEmpty() == true)
					return;
				
				int index = transformationsList.getSelectedIndex();
				
				if (index >= 0)
					surface.removeTransform(index);
				
				if (handler != null)
					handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.transformationChangeEvent, surface));
				
				updateTransformationsList();
			}
		});
		
		this.moveUpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (transformationsList.isSelectionEmpty() == true)
					return;
				
				int index = transformationsList.getSelectedIndex();
				
				if (index <= 0)
					return;
				
				TransformMatrix transform1 = surface.getTransforms()[index];
				TransformMatrix transform2 = surface.getTransforms()[index - 1];
				
				surface.setTransform(index, transform2);
				surface.setTransform(index - 1, transform1);
				
				transformationsList.setSelectedIndex(index - 1);
				
				if (handler != null)
					handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.transformationChangeEvent, surface));
			}
		});
		
		this.moveDownButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent event) {
				if (transformationsList.isSelectionEmpty() == true)
					return;
				
				int index = transformationsList.getSelectedIndex();
				
				if (index >= surface.getTransforms().length - 1)
					return;
				
				TransformMatrix transform1 = surface.getTransforms()[index];
				TransformMatrix transform2 = surface.getTransforms()[index + 1];
				
				surface.setTransform(index, transform2);
				surface.setTransform(index + 1, transform1);
				
				transformationsList.setSelectedIndex(index + 1);
				
				if (handler != null)
					handler.fireEvent(new SurfaceEditEvent(SurfaceEditEvent.transformationChangeEvent, surface));
			}
		});
	}
	
	/**
	  Sets the AbstractSurface object that this TransformSurfacePanel object modifies
	  and updates the fields to reflect the change.
	*/
	
	public void setSurface(AbstractSurface surface) {
		this.surface = surface;
		
		if (this.surface != null) {
			this.listModel = new TransformationsListModel(this.surface);
			this.transformationsList.setModel(this.listModel);
		}
		
		this.updateTransformationsList();
	}
	
	/**
	  Returns the AbstractSurface object stored by this TransformSurfacePanel object.
	*/
	
	public AbstractSurface getSurface() {
		return this.surface;
	}
	
	/**
	  Updates the list of transformations displayed by this panel so that its contents matches the AbstractSurface
	  object being edited.
	*/
	
	public void updateTransformationsList() {
		this.transformationsList.repaint();
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SurfaceEvent && ((SurfaceEvent)event).getTarget() != this.surface)
			return;
		
		if (event instanceof SurfaceEditEvent) {
			SurfaceEditEvent editEvent = (SurfaceEditEvent)event;
			
			if (editEvent.isTransformationChangeEvent() == true) {
				this.updateTransformationsList();
			}
		}
		
		if (event instanceof SurfaceRemoveEvent) {
			this.setVisible(false);
		}
	}
	
	/**
	  Sets the EventHandler object used by this TransformSurfacePanel object to the specified EventHandler object.
	  Setting this to null will deactivate event reporting.
	*/
	
	public void setEventHandler(EventHandler handler) {
		this.handler = handler;
		
		if (this.handler != null) {
			this.handler.addListener(this.listModel);
		}
	}
	
	/**
	  Returns the EventHandler object used by this TransformSurfacePanel object.
	*/
	
	public EventHandler getEventHandler() {
		return this.handler;
	}
	
	public void updateDisplay() {
		this.updateTransformationsList();
	}
}
