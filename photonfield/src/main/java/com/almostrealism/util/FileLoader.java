/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.util;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.swing.JFrame;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import com.almostrealism.absorption.PinholeCameraAbsorber;
import org.almostrealism.primitives.AbsorptionPlane;
import org.almostrealism.algebra.VectorMath;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.color.RGB;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.PhysicalConstants;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.physics.Clock;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Factory;
import io.almostrealism.uml.Nameable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.almostrealism.physics.AbsorberHashSet;
import com.almostrealism.physics.AbsorberSet;
import com.almostrealism.physics.DefaultPhotonField;

public class FileLoader extends DefaultHandler implements CodeFeatures {
	public static double verbose = Math.pow(10.0, -5.0);
	public static boolean localRayTrace = true;
	public static int ssDim = 3;
	public static int jobSize = 225;
	public static int colorDepth = 48;
	
	private StringBuffer buf;
	private InputStream in;
	
	private Stack stack;
	private Stack scales;
	private Hashtable heap;
	
	private AbsorberSet set, lset;
	private Absorber absorber;
	private Object current, ref;
	private String name, named, toPrint;
	private boolean clone, print;
	private double scale, pscale;
	
	private PropertyDescriptor pdesc[];
	private Method vecmath[];
	private Field pconstants[];
	
	private Object target;
	private Method setter, methods[];
	private String method;
	private List args;
	
	private boolean abs;
	
	public static void main(String args[]) throws SAXException, IOException {
//		Settings.init();
//		Settings.produceOutput = true;
//		Settings.produceRayTracingEngineOutput = true;
//		Settings.rayEngineOut = new JTextAreaPrintWriter(new JTextArea(30, 50));
		
		System.out.print("Setting default color depth: ");
		RGB.defaultDepth = FileLoader.colorDepth;
		System.out.println(RGB.defaultDepth + " bits.");
		
		AbsorberSet a = FileLoader.loadSet(new FileInputStream(args[0]));
		
		System.out.println("FileLoader: Loaded " + a.size() + " absorbers.");
		System.out.println("FileLoader: Bound = " + a.getBound());
		
		if (a instanceof AbsorberHashSet) {
			System.out.println("FileLoader: Spread Angle = " +
								((AbsorberHashSet)a).getSpreadAngle());
			System.out.println("FileLoader: Spread Count = " +
								((AbsorberHashSet)a).getSpreadCount());
		}
		
//		try {
//			AbsorberSetNode n = new AbsorberSetNode(a);
//			NodeDisplay d = n.getDisplay();
//			JFrame frame = new JFrame("AbsorberSetNode");
//			int w = d.getGridWidth() * 40;
//			int h = 20 + d.getGridHeight() * 20;
//			frame.getContentPane().add(d.getContainer());
//			frame.setSize(w, h);
//			frame.setVisible(true);
//		} catch (IllegalArgumentException e) {
//			System.out.println("FileLoader: Illegal argument (" +
//								e.getMessage() + ")");
//		} catch (IntrospectionException e) {
//			System.out.println("FileLoader: Introspection error (" +
//								e.getMessage() + ")");
//		} catch (IllegalAccessException e) {
//			System.out.println("FileLoader: Illegal access (" +
//								e.getMessage() + ")");
//		} catch (InvocationTargetException e) {
//			System.out.println("FileLoader: Error invoking method (" +
//								e.getCause() + ")");
//			e.getCause().printStackTrace();
//		}
		
		AbsorptionPlane plane = null;
		Iterator itr = a.absorberIterator();
		
		w: while (itr.hasNext()) {
			Object o = itr.next();
			
			if (o instanceof AbsorptionPlane) {
				plane = (AbsorptionPlane) o;
				break w;
			} else if (o instanceof PinholeCameraAbsorber) {
				plane = ((PinholeCameraAbsorber) o).getAbsorptionPlane();
				break w;
			}
		}
		
		// Create photon field and set absorber to the absorber set
		// containing the stuff we want to look at.
		DefaultPhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		// Create a clock and add the photon field
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		if (args.length < 2 || !args[1].equals("on")) {
			plane.disableDisplay();
		} else {
			JFrame frame = new JFrame("Photon Field Simulation");
			frame.getContentPane().add(plane.getDisplay());
			frame.setSize(150, 150);
			frame.setVisible(true);
		}
		
		boolean noFile = false;
		if (args.length > 2 && args[2].equals("off")) noFile = true;
		
		if (!noFile) {
			f.setLogFile("photons.txt");
			f.setLogFrequency(500);
		}
		
		long start = System.currentTimeMillis();
		String uri = "PhotonField/" + start + ".xml";
//		PhotonFieldSceneLoader.putCache(uri, ((AbsorberHashSet) a).getRayTracer().getScene());
		
		// Run the simulation
		c: while (true) {
			c.tick().get().run();
			
			if (c.getTicks() == 1) System.out.println("FileLoader: Tick...");
			
			if ((c.getTicks() == 100000 || c.getTicks() % 100000 == 0)
					&& a instanceof AbsorberHashSet) {
				Factory<Scene<ShadableSurface>> loader = null; // new PhotonFieldSceneLoader(uri, true); TODO

//				TODO
//				RayTracingJobFactory factory = new RayTracingJobFactory(uri, loader.getWidth(), loader.getHeight(),
//												FileLoader.ssDim, FileLoader.ssDim,
//												FileLoader.jobSize, System.currentTimeMillis());
//				factory.setSceneLoader(loader.getClass().getName()); TODO
//				OutputServer.getCurrentServer().getNodeServer().addTask(factory); TODO
				System.out.println("FileLoader: Added ray tracing task.");
			}
			
			if (Math.random() < FileLoader.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				if (noFile) continue c;
				
				try {
					plane.saveImage("photon-field-sim.ppm");
				} catch (IOException ioe) {
					System.out.println("FileLoader: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}
	
	public static AbsorberSet loadSet(InputStream in)
									throws SAXException, IOException {
		return FileLoader.loadSet(in, null);
	}
	
	public static AbsorberSet loadSet(InputStream in, Factory<Scene<ShadableSurface>> loader)
									throws SAXException, IOException {
		FileLoader l = new FileLoader();
		l.setInputStream(in);
		l.parse();
		AbsorberSet s = l.getSet();
		
		if (s instanceof AbsorberHashSet) {
			((AbsorberHashSet)s).init();
			if (loader != null) ((AbsorberHashSet)s).loadColorBuffers(loader);
		}
		
		return s;
	}
	
	public FileLoader() {
		this.buf = new StringBuffer();
		this.stack = new Stack();
		this.scales = new Stack();
		this.heap = new Hashtable();
		
		this.scale = 1.0;
		this.pscale = 1.0;
		
		System.out.println("FileLoader: Loading VectorMath operations...");
		this.vecmath = VectorMath.class.getDeclaredMethods();
		
		System.out.println("FileLoader: Loading physical constants...");
		this.pconstants = PhysicalConstants.class.getFields();
		for (int i = 0; i < this.pconstants.length; i++) {
			String name = this.pconstants[i].getName();
			
			try {
				this.heap.put(name, this.pconstants[i].get(null));
			} catch (IllegalArgumentException e) {
				System.out.println("FileLoader: Error instantiating " + name +
									" (" + e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access (" +
									e.getMessage() + ")");
			}
		}
	}
	
	public void parse() throws SAXException, IOException {
		try {
			SAXParserFactory.newInstance().newSAXParser().parse(this.in, this);
		} catch (ParserConfigurationException pce) {
			System.out.println("FileLoader: Parser configuration error (" +
								pce.getMessage() + ")");
		}
	}
	
	public void setInputStream(InputStream in) { this.in = in; }
	
	public AbsorberSet getSet() { return this.set; }

	@Override
	public void characters(char c[], int start, int length) throws SAXException {
		if (this.buf != null)
			this.buf.append(c, start, length);
	}

	@Override
	public void startElement(String uri, String lName, String qName, Attributes attr)
																	throws SAXException {
		this.buf = new StringBuffer();
		
		String a = attr.getValue("absolute");
		if (a != null) this.abs = Boolean.parseBoolean(a);
		
		if (this.lset != null) {
			System.out.println("FileLoader: Multiple top level elements detected.");
			System.out.println("FileLoader: Created top level set.");
			this.set = this.lset;
			this.lset = null;
		}
		
		String nm = attr.getValue("name");
		if (nm != null) {
			this.name = nm;
			this.named = qName;
		}
		
		String sprint = attr.getValue("print");
		if (sprint != null) {
			this.print = Boolean.parseBoolean(sprint);
			this.toPrint = qName;
		}
		
		String sclone = attr.getValue("clone");
		if (sclone == null)
			this.clone = false;
		else
			this.clone = Boolean.parseBoolean(sclone);
		
		String spscale = attr.getValue("scale");
		if (spscale == null)
			this.pscale = 1.0;
		else
			this.pscale = Double.parseDouble(spscale);
		
		double p[] = {0.0, 0.0, 0.0};
		
		String pos = attr.getValue("position");
		if (pos != null) p = (double[]) this.heap.get(pos);
		
		String x = attr.getValue("x");
		if (x != null) p[0] = Double.parseDouble(x);
		String y = attr.getValue("y");
		if (y != null) p[1] = Double.parseDouble(y);
		String z = attr.getValue("z");
		if (z != null) p[2] = Double.parseDouble(z);
		
		if (qName.equals("absorber-set")) {
			AbsorberSet s = null;
			String type = attr.getValue("classname");
			
			if (type == null) {
				s = new AbsorberHashSet();
			} else {
				try {
					s = (AbsorberSet) Class.forName(type).newInstance();
				} catch (InstantiationException e) {
					System.out.println("FileLoader: Error instantiating " + type +
										" (" + e.getMessage() + ")");
				} catch (IllegalAccessException e) {
					System.out.println("FileLoader: Illegal access (" +
										e.getMessage() + ")");
				} catch (ClassNotFoundException e) {
					System.out.println("FileLoader: " + type + " class not found.");
				} catch (ClassCastException e) {
					System.out.println("FileLoader: " + type + " is not an AbsorberSet.");
				}
				
				if (s == null) {
					System.out.println("FileLoader: Using AbsorberHashSet.");
					s = new AbsorberHashSet();
				}
			}
			
			if (this.set != null) {
				this.set.addAbsorber(s, vector(p[0] * scale, p[1] * scale, p[2] * scale));
				this.stack.push(this.set);
				this.scales.push(new Double(this.scale));
			}
			
			this.current = s;
			this.pdesc = null;
			
			String scale = attr.getValue("scale");
			if (scale != null)
				this.scale = Double.parseDouble(scale);
			else
				this.scale = 1.0;
			
			this.set = s;
		} else if (qName.equals("absorber")) {
			String type = attr.getValue("classname");
			
			if (type == null) {
				System.out.println("FileLoader: No classname specified for Absorber.");
				return;
			} else if (this.absorber != null) {
				System.out.println("FileLoader: Nested absorbers detected.");
				return;
			}
			
			try {
				this.absorber = (Absorber) Class.forName(type).newInstance();
				
				this.current = this.absorber;
				this.pdesc = null;
				
				if (this.set == null) {
					this.set = new AbsorberHashSet();
					System.out.println("FileLoader: Created top level set.");
				}
				
				this.set.addAbsorber(this.absorber, vector(p[0] * scale, p[1] * scale, p[2] * scale));
			} catch (InstantiationException e) {
				System.out.println("FileLoader: Error instantiating " + type +
									" (" + e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access (" +
									e.getMessage() + ")");
			} catch (ClassNotFoundException e) {
				System.out.println("FileLoader: " + type + " class not found.");
			} catch (ClassCastException e) {
				System.out.println("FileLoader: " + type + " is not an Absorber.");
			}
			
			if (this.current instanceof Nameable && qName == this.named) {
				((Nameable) this.current).setName(name);
			}
		} else if (qName.equals("vector")) {
			if (this.args != null) this.args.add(p);
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, p);
				this.name = null;
			}
		} else if (qName.equals("property")) {
			String name = attr.getValue("name");
			
			if (this.pdesc == null) {
				try {
					this.pdesc =
						Introspector.getBeanInfo(this.current.getClass())
									.getPropertyDescriptors();
				} catch (IntrospectionException e) {
					System.out.println("FileLoader: Introspection exception (" +
										e.getMessage() + ")");
					return;
				}
			}
			
			this.setter = null;
			
			for (int i = 0; i < this.pdesc.length; i++) {
				if (this.pdesc[i].getName().equals(name))
					setter = this.pdesc[i].getWriteMethod();
			}
			
			if (setter == null) {
				System.out.println("FileLoader: No setter for " + name + " found.");
				return;
			}
			
			this.args = new ArrayList();
		} else if (qName.equals("vecmath")) {
			String name = attr.getValue("op");
			this.setter = null;
			
			for (int i = 0; i < this.vecmath.length; i++) {
				if (this.vecmath[i].getName().equals(name))
					setter = this.vecmath[i];
			}
			
			if (setter == null) {
				System.out.println("FileLoader: VectorMath operation " + name + " not found.");
				return;
			}
			
			this.args = new ArrayList();
		} else if (qName.equals("call")) {
			this.method = attr.getValue("method");
			this.target = this.current;
			this.methods = this.current.getClass().getMethods();
			this.setter = null;
			this.args = new ArrayList();
		} else if (qName.equals("object")) {
			String name = attr.getValue("name");
			String type = attr.getValue("classname");
			
			try {
				Object value = Class.forName(type).newInstance();
				this.heap.put(name, value);
				this.current = value;
				this.pdesc = null;
				
				if (this.current instanceof Nameable && qName == this.named) {
					((Nameable) this.current).setName(name);
				}
			} catch (InstantiationException e) {
				System.out.println("FileLoader: Error instantiating " + type +
									" (" + e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access (" +
									e.getMessage() + ")");
			} catch (ClassNotFoundException e) {
				System.out.println("FileLoader: " + type + " class not found.");
			}
		}
	}
	
	public void endElement(String uri, String lName, String qName) {
		if (qName.equals("absorber-set")) {
			AbsorberSet s = null;
			if (!this.stack.isEmpty()) {
				s = (AbsorberSet) this.stack.pop();
				this.scale = ((Double) this.scales.pop()).doubleValue();
			}
			
			if (s == null) {
				this.lset = new AbsorberHashSet();
				this.lset.addAbsorber(this.set, ZeroVector.getInstance());
				this.lset = s;
			} else {
				this.set = s;
			}
		} else if (qName.equals("absorber")) {
			if (this.current == this.absorber) {
				if (this.stack.isEmpty())
					this.current = this.set;
				else
					this.current = this.stack.peek();
			}
			
			this.absorber = null;
		} else if (qName.equals("property")) {
			if (this.setter == null) return;
			
			try {
				this.setter.invoke(this.current, this.args.toArray());
			} catch (IllegalArgumentException e) {
				System.out.println("FileLoader: Illegal argument setting property (" +
									e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access setting property (" +
									e.getMessage() + ")");
			} catch (InvocationTargetException e) {
				System.out.println("FileLoader: Error invoking property setter (" +
									e.getCause() + ")");
			}
			
			this.args = null;
		} else if (qName.equals("vecmath")) {
			if (this.setter == null) return;
			
			Object o = null;
			
			try {
				o = this.setter.invoke(null, this.args.toArray());
			} catch (IllegalArgumentException e) {
				System.out.println("FileLoader: Illegal argument for VectorMath operation (" +
									e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access to VectorMath operation (" +
									e.getMessage() + ")");
			} catch (InvocationTargetException e) {
				System.out.println("FileLoader: Error invoking VectorMath operation (" +
									e.getMessage() + ")");
			}
			
			if (o == null) {
				this.args = null;
				return;
			}
			
			if (o instanceof String) {
				System.out.println("VectorMath: " + o);
				return;
			}
			
			if (this.name == null || !this.named.equals(qName)) {
				this.args = new ArrayList();
				this.args.add(o);
			} else {
				this.args = null;
				this.heap.put(this.name, o);
				this.name = null;
			}
		} else if (qName.equals("call")) {
			Method mt = null;
			Method m[] = this.methods;
			
			i: for (int i = 0; i < m.length; i++) {
				if (!m[i].getName().equals(method)) continue i;
				Class types[] = m[i].getParameterTypes();
				if (this.args.size() != types.length) continue i;
				
//				for (int j = 0; j < types.length; j++) {
//					if (!types[j].isAssignableFrom(this.args.get(j).getClass()))
//						continue i;
//				}
				
				mt = m[i];
				break i;
			}
			
			if (mt == null) {
				System.out.println("FileLoader: Method " + method + " not found.");
				return;
			}
			
			try {
				mt.invoke(this.target, this.args.toArray());
			} catch (IllegalArgumentException e) {
				System.out.println("FileLoader: Illegal argument calling method (" +
									e.getMessage() + ")");
				System.out.println("FileLoader: [Arguments] ");
				
				Iterator itr = this.args.iterator();
				
				while (itr.hasNext()) {
					Object o = itr.next();
					System.out.println("\t" + o + " \t " + o.getClass());
				}
			} catch (IllegalAccessException e) {
				System.out.println("FileLoader: Illegal access calling method (" +
									e.getMessage() + ")");
			} catch (InvocationTargetException e) {
				System.out.println("FileLoader: Error invoking method (" +
									e.getCause().getMessage() + ")");
			}
			
			this.args = null;
		} else if (qName.equals("watts")) {
			double d = Double.parseDouble(this.buf.toString());
			
			if (this.args != null)
				this.args.add(new Double(d * PhysicalConstants.wattsToEvMsec));
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, new Double(d * PhysicalConstants.wattsToEvMsec));
				this.name = null;
			}
		} else if (qName.equals("eVs")) {
			if (this.args != null)
				this.args.add(new Double(this.buf.toString()));
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, new Double(this.buf.toString()));
				this.name = null;
			}
		} else if (qName.equals("decimal")) {
			double d;
			
			if (this.buf != null && this.buf.length() > 0)
				d = Double.parseDouble(this.buf.toString());
			else if (this.ref instanceof Number)
				d = ((Number)this.ref).doubleValue();
			else
				return;
			
			if (!this.abs)
				d = d * this.scale * this.pscale;
			else
				this.abs = false;
			
			if (this.args != null)
				this.args.add(new Double(d));
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, new Double(d));
				this.name = null;
			}
			
			if (this.print && qName.equals(this.toPrint)) {
				System.out.println("Print: " + d);
				this.print = false;
				this.toPrint = null;
			}
		} else if (qName.equals("integer")) {
			int d = Integer.parseInt(this.buf.toString());
			
			if (!this.abs)
				d = (int)(d * this.scale * this.pscale);
			else
				this.abs = false;
			
			if (this.args != null)
				this.args.add(new Integer(d));
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, new Integer(d));
				this.name = null;
			}
		} else if (qName.equals("boolean")) {
			if (this.args != null)
				this.args.add(new Boolean(this.buf.toString()));
			
			if (this.name != null && this.named.equals(qName)) {
				this.heap.put(this.name, new Boolean(this.buf.toString()));
				this.name = null;
			}
		} else if (qName.equals("reference")) {
			String n = this.buf.toString();
			Object o = this.heap.get(n);
			
			this.buf = null;
			
			if (o == null) {
				System.out.println("FileLoader: Object reference " +
									n + " not found.");
				return;
			}
			
			if (this.clone) {
				if (o instanceof double[])
					o = VectorMath.clone((double[])o);
				else
					System.out.println("FileLoader: Unable to clone " + o);
				
				this.clone = false;
			}
			
			if (o instanceof Integer) {
				o = new Integer((int) (this.pscale * ((Number)o).doubleValue()));
			} else if (o instanceof Number) {
				o = new Double(this.pscale * ((Number)o).doubleValue());
			}
			
			if (this.args != null) this.args.add(o);
			this.ref = o;
		}
		
		this.pscale = 1.0;
	}
}
