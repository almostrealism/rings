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


import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.almostrealism.util.Vector;

/**
 * @author Mike Murray
 */
public class RigidBodyTableModel extends AbstractTableModel {
	public static final int LINEAR = 1;
	public static final int ANGULAR = 2;
	
	public static final String linearColumnNames[] = {"Class", "Mass",
													"Location", "Linear Velocity",
													"Linear Momentum", "Force"};
	
	public static final String angularColumnNames[] = {"Class", "Mass",
													"Rotation", "Angular Velocity",
													"Angular Momentum", "Torque"};
	private List bodies;
	private int type;
	
	public RigidBodyTableModel(List bodies, int type) {
		this.bodies = bodies;
		this.type = type;
	}
	
	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() { return 6; }
	
	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() { return this.bodies.size(); }
	
	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName()
	 */
	public String getColumnName(int index) {
		if (this.type == RigidBodyTableModel.ANGULAR)
			return RigidBodyTableModel.angularColumnNames[index];
		else
			return RigidBodyTableModel.linearColumnNames[index];
	}
	
	/**
	 * @see javax.swing.table.AbstractTableModel#isCellEditable()
	 */
	public boolean isCellEditable(int row, int column) {
		if (column > 0) {
			return (column != 4);
		} else {
			return false;
		}
	}
	
	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int column) {
		RigidBody bd = (RigidBody)this.bodies.toArray()[row];
		RigidBody.State b = bd.getState();
		
		
		if (this.type == RigidBodyTableModel.ANGULAR) {
			if (column == 0) {
				String name = bd.getClass().getName();
				name = name.substring(name.lastIndexOf(".") + 1);
				
				return name;
			} else if (column == 1) {
				return new Double(b.getMass());
			} else if (column == 2) {
				return b.getRotation();
			} else if (column == 3) {
				return new Double(b.getAngularVelocity().length());
			} else if (column == 4) {
				return b.getAngularMomentum();
			} else if (column == 5) {
				return b.getTorque();
			} else {
				return null;
			}
		} else {
			if (column == 0) {
				String name = bd.getClass().getName();
				name = name.substring(name.lastIndexOf(".") + 1);
				
				return name;
			} else if (column == 1) {
				return new Double(b.getMass());
			} else if (column == 2) {
				return b.getLocation();
			} else if (column == 3) {
				return new Double(b.getLinearVelocity().length());
			} else if (column == 4) {
				return b.getLinearMomentum();
			} else if (column == 5) {
				return b.getForce();
			} else {
				return null;
			}
		}
	}
	
	public void setValueAt(Object value, int row, int column) {
		if (!this.isCellEditable(row, column)) return;
		
		RigidBody.State b = (RigidBody.State)this.bodies.toArray()[row];
		
		if (this.type == RigidBodyTableModel.ANGULAR) {
			if (column == 1) {
				b.setMass(((Double)value).doubleValue());
			} else if (column == 2) {
				b.setRotation((Vector)value);
			} else if (column == 3) {
				b.setAngularVelocity((Vector)value);
			} else if (column == 5) {
				b.setTorque((Vector)value);
			} else { return; }
		} else {
			if (column == 1) {
				b.setMass(((Double)value).doubleValue());
			} else if (column == 2) {
				b.setLocation((Vector)value);
			} else if (column == 3) {
				b.setLinearVelocity((Vector)value);
			} else if (column == 5) {
				b.setForce((Vector)value);
			} else { return; }
		}
		
		super.fireTableCellUpdated(row, column);
	}
}
