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

/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.physics;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;

import org.almostrealism.swing.panels.ExtendedCellEditor;
import org.almostrealism.swing.panels.ExtendedCellRenderer;

/**
 * @author Mike Murray
 */
public class RigidBodyEditPanel extends JPanel {
	private ArrayList bodies;
	
	private Simulation sim;
	
	private RigidBodyTableModel lModel, aModel;
	private JTable lTable, aTable;
	
	/**
	 * Constructs a new RigidBodyEditPanel.
	 */
	public RigidBodyEditPanel() {
		super(new BorderLayout());
		
		this.bodies = new ArrayList();
		
		this.lModel = new RigidBodyTableModel(bodies, RigidBodyTableModel.LINEAR);
		this.aModel = new RigidBodyTableModel(bodies, RigidBodyTableModel.ANGULAR);
		
		this.lTable = new JTable(this.lModel);
		this.lTable.setDefaultEditor(Object.class, new ExtendedCellEditor());
		this.lTable.setDefaultRenderer(Object.class, new ExtendedCellRenderer());
		this.lTable.setPreferredScrollableViewportSize(new Dimension(1500, 70));
		
		this.aTable = new JTable(this.aModel);
		this.aTable.setDefaultEditor(Object.class, new ExtendedCellEditor());
		this.aTable.setDefaultRenderer(Object.class, new ExtendedCellRenderer());
		this.aTable.setPreferredScrollableViewportSize(new Dimension(1500, 70));
		
		this.lTable.getColumn(this.lModel.getColumnName(2)).setMinWidth(200);
		this.lTable.getColumn(this.lModel.getColumnName(3)).setMinWidth(200);
		
		this.aTable.getColumn(this.aModel.getColumnName(2)).setMinWidth(200);
		this.aTable.getColumn(this.aModel.getColumnName(3)).setMinWidth(200);
		
		super.add(new JScrollPane(this.lTable), BorderLayout.CENTER);
		super.add(new JScrollPane(this.aTable), BorderLayout.SOUTH);
	}
	
	/**
	 * @return  A List object containing the RigidBody objects stored by this RigidBodyEditPanel object.
	 */
	public List bodies() { return this.bodies; }
	
	/**
	 * @return  The Simulation object stored by this RigidBodyEditPanel.
	 */
	public Simulation simulation() { return this.sim; }
	
	/**
	 * Constructs a Simulation object using the specified parameters and the RigidBody objects
	 * stored by this RigidBodyEditPanel object. If a Simulation object has already been
	 * constructed this method returns the instance stored by the last call to this method.
	 * 
	 * @param width  Image width.
	 * @param height  Image height.
	 * @param dt  Time interval between iterations.
	 * @param itr  Number of iterations.
	 * @param l  UpdateListener object to notify after each frame.
	 * @return  The Simulation object constructed.
	 */
	public Simulation simulation(int width, int height, double dt, int itr, UpdateListener l) {
		return this.simulation(width, height, dt, itr, l, null);
	}
	
	/**
	 * Constructs a Simulation object using the specified parameters and the RigidBody objects
	 * stored by this RigidBodyEditPanel object. If a Simulation object has already been
	 * constructed this method returns the instance stored by the last call to this method.
	 * 
	 * @param width  Image width.
	 * @param height  Image height.
	 * @param dt  Time interval between iterations.
	 * @param itr  Number of iterations.
	 * @param l  UpdateListener object to notify after each frame.
	 * @param scene  The Scene object to render for each frame.
	 * @param outputDir  The path to directory to write images.
	 * @return  The Simulation object constructed.
	 */
	public Simulation simulation(int width, int height, double dt, int itr, UpdateListener l, String outputDir) {
		if (this.sim == null) {
			this.sim = new Simulation(width, height, (RigidBody[])this.bodies.toArray(new RigidBody[0]),
									dt, itr, l, outputDir);
			
			this.sim.setEditPanel(this);
			
			return this.sim;
		} else {
			return this.sim;
		}
	}
	
	public void updateTableData() {
		this.lTable.tableChanged(new TableModelEvent(this.lModel));
		this.aTable.tableChanged(new TableModelEvent(this.aModel));
	}
}
