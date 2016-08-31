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

package com.almostrealism.photon.xml;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFormattedTextField;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.almostrealism.space.VectorMath;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.texture.RGB;
import org.almostrealism.util.Nameable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.AbsorberHashSet;
import com.almostrealism.photon.AbsorberSet;
import com.almostrealism.photon.Volume;
import com.almostrealism.photon.util.buffers.AveragedVectorMap2D;
import com.almostrealism.photon.util.buffers.BufferListener;
import com.almostrealism.photon.util.buffers.ColorBuffer;
import com.almostrealism.raytracer.Settings;

public class AbsorberSetNode extends Node implements BufferListener {
	public static int bufferDisplayDim = 100;
	
	public static class PositionPanel extends JPanel {
		private JFormattedTextField xField, yField, zField;
		private double value[];
		
		public PositionPanel(double value[]) {
			this.value = value;
			
			this.xField = new JFormattedTextField(Settings.decimalFormat);
			this.yField = new JFormattedTextField(Settings.decimalFormat);
			this.zField = new JFormattedTextField(Settings.decimalFormat);
			
			this.xField.setColumns(6);
			this.yField.setColumns(6);
			this.zField.setColumns(6);
			
			FocusListener listener = new FocusListener() {
				public void focusGained(FocusEvent event) {
					PositionPanel.this.getValue();
					JTextField field = (JTextField)event.getSource();
					field.setSelectionStart(0);
					field.setSelectionEnd(field.getText().length());
				}

				public void focusLost(FocusEvent event) {
					PositionPanel.this.getValue();
				}
			};
			
			this.xField.addFocusListener(listener);
			this.yField.addFocusListener(listener);
			this.zField.addFocusListener(listener);
			
			this.xField.setValue(new Double(value[0]));
			this.yField.setValue(new Double(value[1]));
			this.zField.setValue(new Double(value[2]));
			
			super.add(new JLabel("X: "));
			super.add(this.xField);
			super.add(new JLabel("Y: "));
			super.add(this.yField);
			super.add(new JLabel("Z: "));
			super.add(this.zField);
		}
		
		public double[] getValue() {
			this.value[0] = ((Number)this.xField.getValue()).doubleValue();
			this.value[1] = ((Number)this.yField.getValue()).doubleValue();
			this.value[2] = ((Number)this.zField.getValue()).doubleValue();
			return this.value;
		}
	}
	
	public static class DimensionPanel extends JPanel {
		private JFormattedTextField wField, hField, mField;
		
		public DimensionPanel(int w, int h, double m) {
			this.wField = new JFormattedTextField(Settings.integerFormat);
			this.hField = new JFormattedTextField(Settings.integerFormat);
			this.mField = new JFormattedTextField(Settings.decimalFormat);
			
			this.wField.setColumns(6);
			this.hField.setColumns(6);
			this.mField.setColumns(6);
			
			this.wField.setValue(new Integer(w));
			this.hField.setValue(new Integer(h));
			this.mField.setValue(new Double(m));
			
			super.add(new JLabel("W: "));
			super.add(this.wField);
			super.add(new JLabel("H: "));
			super.add(this.hField);
			super.add(new JLabel("M: "));
			super.add(this.mField);
		}
		
		public int[] getDimensions() {
			int dim[] = new int[2];
			dim[0] = ((Number)this.wField.getValue()).intValue();
			dim[1] = ((Number)this.hField.getValue()).intValue();
			return dim;
		}
		
		public double getScale() {
			return ((Number)this.mField.getValue()).doubleValue();
		}
	}
	
	private Hashtable absorbers, items, posPanels, dimPanels;
	private Hashtable colorBufPanels, incBufPanels, exitBufPanels;
	
	public AbsorberSetNode(AbsorberSet n) throws IntrospectionException,
												IllegalArgumentException,
												IllegalAccessException,
												InvocationTargetException {
		super(n);
		
		this.absorbers = new Hashtable();
		this.items = new Hashtable();
		this.posPanels = new Hashtable();
		this.dimPanels = new Hashtable();
		this.colorBufPanels = new Hashtable();
		this.incBufPanels = new Hashtable();
		this.exitBufPanels = new Hashtable();
		
		if (n instanceof AbsorberHashSet) {
			((AbsorberHashSet)n).setBufferListener(this);
		}
	}
	
	public Container getAbsorberContainer(Absorber a, Node n) {
		String name = a.toString();
		if (a instanceof Nameable) name = ((Nameable)a).getName();
		JInternalFrame frame = new JInternalFrame(name, true);
		frame.getContentPane().setLayout(new BorderLayout());
		
		if (super.obj instanceof AbsorberHashSet == false)
			return frame;
		
		AbsorberHashSet set = (AbsorberHashSet) super.obj;
		Iterator itr = set.iterator(false);
		
		w: while (itr.hasNext()) {
			AbsorberHashSet.StoredItem it = (AbsorberHashSet.StoredItem) itr.next();
			if (it.absorber != a) continue w;
			
			PositionPanel posPanel = this.getPositionPanel(it, n);
			DimensionPanel dimPanel = this.getDimensionPanel(it, n);
			
			JPanel mainPanel = new JPanel(new BorderLayout());
			JPanel confPanel = new JPanel(new GridLayout(0, 1));
			confPanel.add(posPanel);
			confPanel.add(dimPanel);
			mainPanel.add(confPanel, BorderLayout.NORTH);
			
			JTabbedPane tabs = new JTabbedPane();
			tabs.addTab("Absorber", mainPanel);
			
			Volume v = it.getVolume();
			
			if (v != null) {
				ImageCanvas cbPanel = new ImageCanvas(AbsorberSetNode.bufferDisplayDim,
														AbsorberSetNode.bufferDisplayDim);
				ImageCanvas ibPanel = new ImageCanvas(AbsorberSetNode.bufferDisplayDim,
														AbsorberSetNode.bufferDisplayDim);
				ImageCanvas ebPanel = new ImageCanvas(AbsorberSetNode.bufferDisplayDim,
														AbsorberSetNode.bufferDisplayDim);
				this.colorBufPanels.put(v, cbPanel);
				this.incBufPanels.put(v, ibPanel);
				this.exitBufPanels.put(v, ebPanel);
				tabs.addTab("Color Buffer", cbPanel);
				tabs.addTab("Incidence Buffer", ibPanel);
				tabs.addTab("Exitance Buffer", ebPanel);
			}
			
			frame.getContentPane().add(tabs);
			
			return mainPanel;
		}
		
		return null;
	}
	
	public void listElements(Document doc, Element node, List l) {
		l = new ArrayList();
		Iterator itr = this.items.values().iterator();
		
		while (itr.hasNext()) {
			Node n = (Node) itr.next();
			Element e = doc.createElement("absorber");
			Object k = this.items.get(n);
			PositionPanel pp = (PositionPanel)this.posPanels.get(k);
			
			if (pp != null) {
				double p[] = pp.getValue();
				e.setAttribute("x", String.valueOf(p[0]));
				e.setAttribute("y", String.valueOf(p[1]));
				e.setAttribute("z", String.valueOf(p[2]));
			}
			
			l.clear();
			n.listElements(doc, e, l);
			
			Iterator litr = l.iterator();
			while (litr.hasNext()) node.appendChild((Element)litr.next());
			
			DimensionPanel dp = (DimensionPanel) this.dimPanels.get(k);
			
			if (dp != null) {
				int dim[] = dp.getDimensions();
				double m = dp.getScale();
				
				Element de = doc.createElement("call");
				de.setAttribute("method", "setColorBufferDimensions");
				
				Element we = doc.createElement("decimal");
				we.setNodeValue(String.valueOf(dim[0]));
				de.appendChild(we);
				Element he = doc.createElement("decimal");
				he.setNodeValue(String.valueOf(dim[1]));
				de.appendChild(he);
				Element me = doc.createElement("decimal");
				me.setNodeValue(String.valueOf(m));
				de.appendChild(me);
				
				node.appendChild(de);
			}
		}
	}
	
	public NodeDisplay getDisplay() {
		LayeredNodeDisplay display = new LayeredNodeDisplay(this);
		
		int mtw = -1, tw = 0, th = 0, lh = 0;
		
		Iterator itr = ((AbsorberSet)super.obj).absorberIterator();
		
		w: while (itr.hasNext()) {
			Absorber a = (Absorber) itr.next();
			
			NodeDisplay d = null;
			String name = null;
			
			if (a instanceof Nameable)
				name = ((Nameable)a).getName();
			if (name == null)
				name = a.toString();
			
			if (this.items.containsKey(a)) {
				Node an = (Node)this.absorbers.get(a);
				if (an.getParent() == null) an.setParent(this);
				d = ((Node)this.absorbers.get(a)).getDisplay();
			} else {
				Node n = new Node();
				n.setParent(this);
				n.setName(name);
				
				try {
					n.setObject(a);
					this.absorbers.put(a, n);
					d = n.getDisplay();
				} catch (IllegalArgumentException e) {
					System.out.println("AbsorberSetNode: Illegal argument (" +
										e.getMessage() + ")");
				} catch (IntrospectionException e) {
					System.out.println("AbsorberSetNode: Introspection error (" +
										e.getMessage() + ")");
				} catch (IllegalAccessException e) {
					System.out.println("AbsorberSetNode: Illegal access (" +
										e.getMessage() + ")");
				} catch (InvocationTargetException e) {
					System.out.println("AbsorberSetNode: Error invoking method (" +
										e.getCause().getMessage() + ")");
				}
			}
			
			if (d == null) continue w;
			
			int w = Math.max(3, d.getGridWidth()) * 120;
			int h = (2 + d.getGridHeight()) * 35;
			
			if (tw + w > 1600) {
				mtw = 1000;
				tw = 0;
				lh = lh + th;
				th = 0;
			}
			
			if (d.getFrame() instanceof JInternalFrame == false) continue w;
			
			JInternalFrame f = (JInternalFrame) d.getFrame();
			f.setSize(w, h);
			f.setLocation(tw, lh + 3);
			f.setVisible(true);
			tw = tw + w + 3;
			if (h > th) th = h;
			
			display.add(f);
		}
		
		if (mtw > 0) tw = 2000;
		
		display.setGridWidth(tw / 80);
		display.setGridHeight((lh + th - 20) / 30);
		
		return display;
	}
	
	protected PositionPanel getPositionPanel(AbsorberHashSet.StoredItem it, Node n) {
		if (this.posPanels.containsKey(it)) return (PositionPanel) this.posPanels.get(it);
		
		PositionPanel panel = new PositionPanel(it.position);
		this.posPanels.put(it, panel);
		this.items.put(n, it);
		
		return panel;
	}
	
	protected DimensionPanel getDimensionPanel(AbsorberHashSet.StoredItem it, Node n) {
		if (this.dimPanels.containsKey(it)) return (DimensionPanel) this.dimPanels.get(it);
		
		int dim[] = it.getColorBufferDimensions();
		double m = it.getColorBufferScale();
		DimensionPanel panel = new DimensionPanel(dim[0], dim[1], m);
		this.dimPanels.put(it, panel);
		this.items.put(n, it);
		
		return panel;
	}

	public void updateColorBuffer(double u, double v, Volume source, ColorBuffer target, boolean front) {
		ImageCanvas c = (ImageCanvas) this.colorBufPanels.get(source);
		if (c == null) return;
		
		c.setImageData((int) (u * this.bufferDisplayDim),
						(int) (v * this.bufferDisplayDim),
						target.getColorAt(u, v, front));
		c.repaint();
	}

	public void updateExitanceBuffer(double u, double v, Volume source, AveragedVectorMap2D target, boolean front) {
		ImageCanvas c = (ImageCanvas) this.exitBufPanels.get(source);
		if (c == null) return;
		
		double d = VectorMath.dot(source.getNormal(source.getSpatialCoords(new double[] {u, v})), 
									target.getVector(u, v, front));
		
		c.setImageData((int) (u * AbsorberSetNode.bufferDisplayDim),
						(int) (v * AbsorberSetNode.bufferDisplayDim),
						new RGB(d, d, d));
		c.repaint();
	}

	public void updateIncidenceBuffer(double u, double v, Volume source, AveragedVectorMap2D target, boolean front) {
		ImageCanvas c = (ImageCanvas) this.incBufPanels.get(source);
		if (c == null) return;
		
		double d = VectorMath.dot(source.getNormal(source.getSpatialCoords(new double[] {u, v})), 
									target.getVector(u, v, front));
		
		c.setImageData((int) (u * AbsorberSetNode.bufferDisplayDim),
						(int) (v * AbsorberSetNode.bufferDisplayDim),
						new RGB(d, d, d));
		c.repaint();
	}
}
