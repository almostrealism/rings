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

package com.almostrealism.replicator;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.almostrealism.algebra.Vector;
import org.almostrealism.geometry.BasicGeometry;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import io.almostrealism.uml.ViewModel;

import com.almostrealism.lighting.StandardLightingRigs;
import org.almostrealism.projection.PinholeCamera;

/**
 * TODO  Add listener support
 * 
 * @author  Michael Murray
 */
@ViewModel
public class ReplicatorTableModel extends Scene<ShadableSurface> implements TableModel {
	public static final String LEFT = "Left";
	public static final String RIGHT = "Right";
	public static final String TOP = "Top";
	public static final String BOTTOM = "Bottom";
	public static final String FRONT = "Front";
	public static final String BACK = "Back";

	private LayeredReplicant<DefaultReplicant> replicant;
	private List<String> modelNames;
	
	public ReplicatorTableModel() {
		replicant = new LayeredReplicant<>();
		modelNames = new ArrayList<String>();
		
		StandardLightingRigs.addDefaultLights(this);
		
//		ThinLensCamera c = new ThinLensCamera();
//		c.setLocation(new Vector(0.0, 0.0, -40.0));
//		c.setViewDirection(new Vector(0.0, 0.0, -1.0));
//		c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
//		c.setFocalLength(0.05);
//		c.setFocus(10.0);
//		c.setLensRadius(0.2);
		
		PinholeCamera c = new PinholeCamera();
		c.setLocation(new Vector(0.0, 0.0, -40.0));
		setCamera(c);
		add(replicant);
	}
	
	/**
	 * Adds a {@link DefaultReplicant} with 6 aliases, {@link #LEFT},
	 * {@link #RIGHT}, {@link #TOP}, {@link #BOTTOM}, {@link #FRONT},
	 * {@link #BACK}.
	 * 
	 * @param name  Name of the layer to add.
	 * @param s  Surface to replicate across the 6 directions.
	 */
	public void addLayer(String name, ShadableSurface s) {
		DefaultReplicant<ShadableSurface> r = new DefaultReplicant<>(s);
		r.put(LEFT, new BasicGeometry(new Vector(-2.0, 0.0, 0.0)));
		r.put(RIGHT, new BasicGeometry(new Vector(2.0, 0.0, 0.0)));
		r.put(TOP, new BasicGeometry(new Vector(0.0, 2.0, 0.0)));
		r.put(BOTTOM, new BasicGeometry(new Vector(0.0, -2.0, 0.0)));
		r.put(FRONT, new BasicGeometry(new Vector(0.0, 0.0, 2.0)));
		r.put(BACK, new BasicGeometry(new Vector(0.0, 0.0, -2.0)));
		replicant.addReplicant(r);
		modelNames.add(name);
		add(r);
	}
	
	@Override
	public int getRowCount() { return replicant.length(); }
	
	@Override
	public int getColumnCount() { return 26; }
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex < 2) return String.class;
		return Double.class;
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex < 2) return false;
		return true;
	}
	
	protected void setScale(int layer, String node, int vectorIndex, double value) {
		double s[] = replicant.getReplicant(layer).get(node).getScaleCoefficients();
		s[vectorIndex] = value;
		replicant.getReplicant(layer).get(node).setScaleCoefficients(s[0], s[1], s[2]);
	}
	
	protected void setOffset(int layer, String node, int vectorIndex, boolean invert, double value) {
		if (invert) value = -value;
		double p[] = replicant.getReplicant(layer).get(node).getLocation().toArray();
		p[vectorIndex] = value;
		replicant.getReplicant(layer).get(node).setLocation(new Vector(p[0], p[1], p[2]));
	}
	
	protected double getScale(int layer, String node, int vectorIndex) {
		return replicant.getReplicant(layer).get(node).getScaleCoefficients()[vectorIndex];
	}
	
	protected double getOffset(int layer, String node, int vectorIndex, boolean invert) {
		double s = replicant.getReplicant(layer).get(node).getPosition()[vectorIndex];
		return invert ? -s : s;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		double v = (Double) aValue;

		switch (columnIndex) {
			case 2 -> setScale(rowIndex, LEFT, 0, v);
			case 3 -> setScale(rowIndex, LEFT, 1, v);
			case 4 -> setScale(rowIndex, LEFT, 2, v);
			case 5 -> setScale(rowIndex, RIGHT, 0, v);
			case 6 -> setScale(rowIndex, RIGHT, 1, v);
			case 7 -> setScale(rowIndex, RIGHT, 2, v);
			case 8 -> setScale(rowIndex, TOP, 0, v);
			case 9 -> setScale(rowIndex, TOP, 1, v);
			case 10 -> setScale(rowIndex, TOP, 2, v);
			case 11 -> setScale(rowIndex, BOTTOM, 0, v);
			case 12 -> setScale(rowIndex, BOTTOM, 1, v);
			case 13 -> setScale(rowIndex, BOTTOM, 2, v);
			case 14 -> setScale(rowIndex, FRONT, 0, v);
			case 15 -> setScale(rowIndex, FRONT, 1, v);
			case 16 -> setScale(rowIndex, FRONT, 2, v);
			case 17 -> setScale(rowIndex, BACK, 0, v);
			case 18 -> setScale(rowIndex, BACK, 1, v);
			case 19 -> setScale(rowIndex, BACK, 2, v);
			case 20 -> setOffset(rowIndex, LEFT, 0, true, v);
			case 21 -> setOffset(rowIndex, RIGHT, 0, false, v);
			case 22 -> setOffset(rowIndex, TOP, 1, false, v);
			case 23 -> setOffset(rowIndex, BOTTOM, 1, true, v);
			case 24 -> setOffset(rowIndex, FRONT, 2, false, v);
			case 25 -> setOffset(rowIndex, BACK, 2, true, v);
		}
	}
	
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
			case 0: return "Layer " + rowIndex;
			case 1: return modelNames.get(rowIndex);
			case 2: return getScale(rowIndex, LEFT, 0);
			case 3: return getScale(rowIndex, LEFT, 1);
			case 4: return getScale(rowIndex, LEFT, 2);
			case 5: return getScale(rowIndex, RIGHT, 0);
			case 6: return getScale(rowIndex, RIGHT, 1);
			case 7: return getScale(rowIndex, RIGHT, 2);
			case 8: return getScale(rowIndex, TOP, 0);
			case 9: return getScale(rowIndex, TOP, 1);
			case 10: return getScale(rowIndex, TOP, 2);
			case 11: return getScale(rowIndex, BOTTOM, 0);
			case 12: return getScale(rowIndex, BOTTOM, 1);
			case 13: return getScale(rowIndex, BOTTOM, 2);
			case 14: return getScale(rowIndex, FRONT, 0);
			case 15: return getScale(rowIndex, FRONT, 1);
			case 16: return getScale(rowIndex, FRONT, 2);
			case 17: return getScale(rowIndex, BACK, 0);
			case 18: return getScale(rowIndex, BACK, 1);
			case 19: return getScale(rowIndex, BACK, 2);
			case 20: return getOffset(rowIndex, LEFT, 0, true);
			case 21: return getOffset(rowIndex, RIGHT, 0, false);
			case 22: return getOffset(rowIndex, TOP, 1, false);
			case 23: return getOffset(rowIndex, BOTTOM, 1, true);
			case 24: return getOffset(rowIndex, FRONT, 2, false);
			case 25: return getOffset(rowIndex, BACK, 2, true);
		}
		
		return "";
	}
	
	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
			case 0: return "Layer";
			case 1: return "Model";
			case 2: return "Left SX";
			case 3: return "Left SY";
			case 4: return "Left SZ";
			case 5: return "Right SX";
			case 6: return "Right SY";
			case 7: return "Right SZ";
			case 8: return "Top SX";
			case 9: return "Top SY";
			case 10: return "Top SZ";
			case 11: return "Bottom SX";
			case 12: return "Bottom SY";
			case 13: return "Bottom SZ";
			case 14: return "Front SX";
			case 15: return "Front SY";
			case 16: return "Front SZ";
			case 17: return "Back SX";
			case 18: return "Back SY";
			case 19: return "Back SZ";
			case 20: return "Left Offset";
			case 21: return "Right Offset";
			case 22: return "Top Offset";
			case 23: return "Bottom Offset";
			case 24: return "Front Offset";
			case 25: return "Back Offset";
		}
		
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#addTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see javax.swing.table.TableModel#removeTableModelListener(javax.swing.event.TableModelListener)
	 */
	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}
}
