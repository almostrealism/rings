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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.almostrealism.space.Vector;
import org.almostrealism.texture.GraphicsConverter;
import org.almostrealism.texture.RGB;

import com.almostrealism.physics.shaders.RigidBodyStateShader;
import com.almostrealism.projection.Camera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.rayshade.BlendingShader;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ReflectionShader;
import com.almostrealism.rayshade.Shader;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.lighting.DirectionalAmbientLight;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.lighting.SphericalLight;
import com.almostrealism.ui.DebugOutputPanel;
import com.almostrealism.ui.JTextAreaPrintWriter;

/**
 * @author Mike Murray
 */
public class Simulation extends Scene implements Runnable {
	public static final double G = 6.67 * Math.pow(10.0, -11.0);
	
	public static interface Force {
		public Vector evaluateForce(RigidBody.State state, RigidBody.State allStates[]);
	}
	
	private Camera c;
	
	private Scene scene;
	private String dir;
	
	private RigidBodyEditPanel editPanel;
	
	private UpdateListener listener;
	
	private int width, height, imageWidth, imageHeight;
	private double ox, oy, scale;
	private Image image;
	
	private boolean sleep, render, logState = false;
	private double dt, fdt, vdt, totalTime;
	private int itr;
	
	private RigidBody.State bodies[];
	private List forces;
	
	java.util.Vector inputFiles;
	
	public static void main(String args[]) {
		RigidBodyEditPanel editPanel = new RigidBodyEditPanel();
		List bodies = editPanel.bodies();
		
		double dt = 1.0;
		
		String command = args[0];
		
		int w = Integer.parseInt(args[1]);
		int h = Integer.parseInt(args[2]);
		int itr = Integer.parseInt(args[3]);
		String dir = args[4];
		
		PinholeCamera c = new PinholeCamera();
		
		Simulation s = null;
		
		if (command.equals("execg")) {
			JPanel panel = new JPanel();
			JFrame frame = new JFrame("Physics Simulation");
			frame.setSize(200, 200);
			frame.getContentPane().add(panel, BorderLayout.CENTER);
			
			JFrame editFrame = new JFrame("Physics Properties");
			editFrame.setSize(800, 300);
			editFrame.getContentPane().add(editPanel, BorderLayout.CENTER);
			
			editFrame.setVisible(true);
			frame.setVisible(true);
			
			s = editPanel.simulation(w, h, dt, itr, null, dir);
		} else if (command.equals("exec") ||
					command.equals("render") ||
					command.equals("jitter")) {
			Properties p = new Properties();
			
			try {
				p.load(new FileInputStream(dir + "latest.state"));
			} catch (IOException ioe) {
				System.out.println("Simulation: IO error loading body data (" + ioe.getMessage() + ")");
				System.exit(1);
			}
			
			s = new Simulation(w, h, new RigidBody[0], dt, itr, null, dir);
			s.setSleepEachFrame(false);
			
			Iterator b = s.loadProperties(p).iterator();
			while (b.hasNext()) s.addSurface((ShadableSurface)b.next());
		} else {
			System.out.println("Invalid argument: " + command);
			System.exit(1);
		}
		
		if (command.equals("exec")) {
			s.addGravity();
			s.start();
		} else if (command.equals("jitter")) {
			s.jitter(Double.parseDouble(args[5]));
			
			try {
				long time = System.currentTimeMillis();
				Properties p = s.generateProperties();
				
				String head = "Simulation state";
				
				p.store(new FileOutputStream(dir + time + ".state"), head);
				p.store(new FileOutputStream(dir + "latest.state"), head);
			} catch (IOException ioe) {
				System.out.println("IO error writing state");
			}
		} else {
			s.writeImage(0, "render");
		}
	}
	
	public Simulation() { this(2, 1, new RigidBody[0], 1.0, 1); }
	
	public Simulation(int width, int height) { this(width, height, new RigidBody[0], 1.0, 1); }
	
	public Simulation(int width, int height, RigidBody bodies[], double dt, int itr) {
		this(width, height, bodies, dt, itr, null, null);
	}
	
	public Simulation(int width, int height, RigidBody bodies[], double dt, int itr, UpdateListener l) {
		this(width, height, bodies, dt, itr, l, null);
	}
	
	public Simulation(int width, int height, RigidBody bodies[], double dt, int itr, UpdateListener l,
						String outputDir) {
		this(width, height, width / 2.0, height / 2.0, 1.0, bodies, dt, itr, l, outputDir);
	}
	
	/**
	 * Constructs a new Simulation object using the specified parameters.
	 * 
	 * @param width  Width of image produced.
	 * @param height  Height of image produced.
	 * @param ox  X coordinate of origin.
	 * @param oy  Y coordinate of origin.
	 * @param scale  Scale factor.
	 * @param bodies  Array containing RigidBody objects to use in simulation.
	 * @param dt  Time interval to use for each iteration of simulation.
	 * @param itr  Number of iterations to run.
	 * @param l  UpdateListener instance to update after each iteration (null accepted).
	 * @param outputDir  Path to directory to write images (null accepted).
	 */
	public Simulation(int width, int height, double ox, double oy, double scale,
			RigidBody bodies[], double dt, int itr, UpdateListener l, String outputDir) {
		this.listener = l;
		
		this.width = width;
		this.height = height;
		this.imageWidth = this.width;
		this.imageHeight = this.height;
		
		this.ox = ox;
		this.oy = oy;
		this.scale = scale;
		this.dt = dt;
		this.itr = itr;
		this.dir = outputDir;
		
		this.bodies = new RigidBody.State[bodies.length];
		
		for (int i = 0; i < bodies.length; i++) {
			this.bodies[i] = bodies[i].getState();
			if (bodies[i] instanceof ShadableSurface) super.add((ShadableSurface)bodies[i]);
		}
		
		this.sleep = true;
		
		this.forces = new ArrayList();
		this.inputFiles = new java.util.Vector();
	}
	
	/**
	 * Sets the number of frames to render per second.
	 * 
	 * @param fps  The number of frames per second.
	 */
	public void setFPS(double fps) { this.fdt = 1.0 / fps; }
	
	/**
	 * If the value of vdt is set to anything greater than 0.0, the time interval for each
	 * iteration will be set so that it is the value of vdt divided by the average velocity
	 * of the objects in the screen.
	 */
	public void setVDT(double vdt) { this.vdt = vdt; }
	
	/**
	 * @return  An AWT Image object storing the most recent image data for this Simulation object.
	 */
	public Image getImage() { return this.image; }
	
	/**
	 * @return  The total time in seconds since the start of the simulation.
	 */
	public double getTime() { return this.totalTime; }
	
	/**
	 * @return  A clone of the superclass of this Simulation object.
	 */
	public Scene getScene() { return (Scene)super.clone(); }
	
	/**
	 * Sets edit RigidBodyEditPanel object stored by this Simulation.
	 * 
	 * @param editPanel  RigidBodyEditPanel object to use.
	 */
	public void setEditPanel(RigidBodyEditPanel editPanel) { this.editPanel = editPanel; }
	
	/**
	 * Sets the sleep each frame flag.
	 * 
	 * @param sleep  True if the simulation thread should wait the actual time between frames,
	 *               false otherwise.
	 */
	public void setSleepEachFrame(boolean sleep) { this.sleep = sleep; }
	
	/**
	 * @return  True if the simulation will render an image for each frame, false otherwise.
	 */
	public boolean getRenderEachFrame() { return render; }
	
	/**
	 * @param render  True if the simulation should render an image for each frame, false otherwise.
	 */
	public void setRenderEachFrame(boolean render) { this.render = render; }
	
	/**
	 * @return  True if the simulation will output a properties file for each frame, false otherwise.
	 */
	public boolean getLogEachFrame() { return this.logState; }
	
	/**
	 * @param log  True if the simulation should output a properties file for each frame, false otherwise.
	 */
	public void setLogEachFrame(boolean log) { this.logState = log; }
	
	/**
	 * Adds a Force object to the set of forces that will be evaluated each iteration.
	 * 
	 * @param f  The Force object to add.
	 */
	public void addForce(Force f) { this.forces.add(f); }
	
	/**
	 * Adds the force of (newtonian) gravity to the simulation.
	 */
	public void addGravity() {
		this.addForce(new Force() {
			public Vector evaluateForce(RigidBody.State state, RigidBody.State allStates[]) {
				Vector g = new Vector(0.0, 0.0, 0.0);
				
				i: for (int i = 0; i < allStates.length; i++) {
					if (state == allStates[i]) continue i;
					
					Vector d = allStates[i].getLocation().subtract(state.getLocation());
					double f = Simulation.G * state.getMass() * allStates[i].getMass();
					double l = d.lengthSq();
					
					d.multiplyBy(f / (Math.sqrt(l) * l));
					
					g.addTo(d);
				}
				
				return g;
			}
		});
	}
	
	/**
	 * Sets the surfaces stored by this Simulation object.
	 * 
	 * @throws IllegalArgumentException  If any of the Surface objects in the specified array
	 *                                   are not instances of RigidBody.
	 */
	public void setSurfaces(ShadableSurface s[]) {
		RigidBody.State newBodies[] = new RigidBody.State[s.length];
		
		for (int i = 0; i < s.length; i++) {
			if (s[i] instanceof RigidBody)
				newBodies[i] = ((RigidBody)s[i]).getState();
			else
				throw new IllegalArgumentException("Illegal argument: " + s[i]);
		}
		
		super.setSurfaces(s);
	}
	
	/**
	 * Adds the specified Surface object to the surfaces stored by this Simulation object.
	 * 
	 * @throws IllegalArgumentException  If the Surface object specified is not an instance of RigidBody.
	 */
	public void addSurface(ShadableSurface s) {
		if (s instanceof RigidBody == false) throw new IllegalArgumentException("Illegal argument: " + s);
		
		RigidBody.State newBodies[] = new RigidBody.State[this.bodies.length + 1];
		
		for (int i = 0; i < this.bodies.length; i++) {
			newBodies[i] = this.bodies[i];
		}
		
		newBodies[newBodies.length - 1] = ((RigidBody)s).getState();
		
		super.add(s);
		this.bodies = newBodies;
	}
	
	/**
	 * Removes the Surface object stored at the specified index from this Simulation object.
	 */
	public void removeSurface(int index) {
		super.remove(index);
		
		RigidBody.State newBodies[] = new RigidBody.State[this.bodies.length - 1];
		
		for (int i = 0; i < index; i++) newBodies[i] = this.bodies[i];
		for (int i = index; i < newBodies.length; i++) newBodies[i] = this.bodies[i + 1];
		
		this.bodies = newBodies;
	}
	
	/**
	 * Moves all of the rigid bodies stored by this Simulation object
	 * by a random amount in the range [-t, t].
	 * 
	 * @param t  Bounds for jittering.
	 */
	public void jitter(double t) {
		for (int i = 0; i < this.bodies.length; i++) {
			Vector l = this.bodies[i].getLocation();
			
			double x = l.getX() + t * (2 * Math.random() - 1.0);
			double y = l.getY() + t * (2 * Math.random() - 1.0);
			double z = l.getZ() + t * (2 * Math.random() - 1.0);
			
			this.bodies[i].setLocation(new Vector(x, y, z));
			((RigidBody) super.get(i)).updateModel();
		}
	}
	
	public double getAverageLinearVelocity() {
		double total = 0.0;
		
		for (int i = 0; i < this.bodies.length; i++) {
			total += this.bodies[i].getLinearVelocity().length();
		}
		
		return total / this.bodies.length;
	}
	
	/**
	 * Calls the run method on this Simulation object.
	 */
	public void start() { this.run(); }
	
	/**
	 * Runs the simulation.
	 */
	public void run() {
		System.out.println("Starting simulation (" + this.itr + "): dt = " + this.dt + "  fdt = " + this.fdt);
		
		if (Settings.produceOutput == true) {
			DebugOutputPanel outputPanel = new DebugOutputPanel();
			outputPanel.showPanel();
		}
		
		java.util.Vector inputFiles = new java.util.Vector();
		
		String instance = "0";
		
		try {
			Properties pr = new Properties();
			pr.load(new FileInputStream(this.dir + "instance"));
			instance = pr.getProperty("instance");
		} catch (FileNotFoundException fnf) {
			System.out.println("Instance file not found. Zero will be used.");
		} catch (IOException ioe) {
			System.out.println("Error reading instance file. Zero will be used.");
		}
		
		for (int i = 0; i < this.itr; i++) {
			try {
				if (this.sleep && i * this.dt % this.fdt == 0) Thread.sleep((int)(this.fdt * 1000));
			} catch (InterruptedException ie) {
				ie.printStackTrace();
			}
			
			for (int j = 0; j < this.bodies.length; j++) this.bodies[j].update(this.dt);
			
			boolean intersected[][] = new boolean[this.bodies.length][this.bodies.length];
			
			for (int j = 0; j < this.bodies.length; j++) {
				Vector g = new Vector(0.0, 0.0, 0.0);
				
				Iterator itr = this.forces.iterator();
				while (itr.hasNext()) g.addTo(((Force)itr.next()).evaluateForce(this.bodies[j], this.bodies));
				
				this.bodies[j].setForce(g);
			}
			
			for (int j = 0; j < this.bodies.length; j++) {
				k: for (int k = 0; k < this.bodies.length; k++) {
					if (k == j) continue k;
					if (intersected[j][k]) continue k;
					
					Vector intersect[] = ((RigidBody) super.get(j)).intersect((RigidBody) super.get(k));
					
					if (intersect.length >= 2) {
						intersected[j][k] = true;
						intersected[k][j] = true;
						
						Vector p = intersect[0];
						Vector n = intersect[1];
						
						System.out.println(this.totalTime + ": Intersection (" + this.bodies[j] + " / " + this.bodies[k] + "): " + p.toString() + " / " + n.toString());
						
						Vector pa = this.bodies[j].getLinearVelocity().add(this.bodies[j].getAngularVelocity().crossProduct(p.subtract(this.bodies[j].getLocation())));
						Vector pb = this.bodies[k].getLinearVelocity().add(this.bodies[k].getAngularVelocity().crossProduct(p.subtract(this.bodies[k].getLocation())));
						double e = (this.bodies[j].getRestitution() + this.bodies[k].getRestitution()) / 2.0;
						double vr = n.dotProduct(pa.subtract(pb));
						
						if (vr >= 0) continue k;
						
						Vector ra = p.subtract(this.bodies[j].getLocation());
						Vector rb = p.subtract(this.bodies[k].getLocation());
						
						double l = (-(1.0 + e) * vr) / (1/this.bodies[j].getMass() + 1/this.bodies[k].getMass() + n.dotProduct(this.bodies[j].getInertia().getInverse().transformAsOffset(ra.crossProduct(n)).crossProduct(ra)) +
								n.dotProduct(this.bodies[k].getInertia().getInverse().transformAsOffset(rb.crossProduct(n)).crossProduct(rb)));
						
						Vector li = n.multiply(l);
						
						this.bodies[j].linearImpulse(li);
						this.bodies[k].linearImpulse(li.minus());
						
						this.bodies[j].angularImpulse(p.subtract(this.bodies[j].getLocation()).crossProduct(li));
						this.bodies[k].angularImpulse(p.subtract(this.bodies[k].getLocation()).crossProduct(li));
					}
				}
			}
			
			this.totalTime = this.totalTime + this.dt;
			
			if (this.totalTime % this.fdt == 0 || this.vdt > 0.0) {
				if (this.editPanel != null) this.editPanel.updateTableData();
				
				try {
					long time = System.currentTimeMillis();
					Properties p = this.generateProperties();
					
					System.out.println("Writing simulation state: " + time);
					
					String head = "Simulation state for instance " + instance + ": " + this.totalTime;
					
					if (this.logState) p.store(new FileOutputStream(this.dir + time + ".state"), head);
					p.store(new FileOutputStream(this.dir + "latest.state"), head);
				} catch (IOException ioe) {
					System.out.println("IO error writing state " + i * this.dt);
				}
				
				if (this.render) this.writeImage(i, instance);
			}
			
			if (this.vdt > 0.0) {
				double a = this.getAverageLinearVelocity();
				
				if (a == 0.0)
					this.dt = this.vdt;
				else
					this.dt = this.vdt / this.getAverageLinearVelocity();
				
				System.out.println("dt = " + this.dt);
			}
			
			if (this.listener != null) this.listener.update();
		}
		
		if (this.scene != null) this.writeEncodeScript(instance);
		
		System.exit(0);
	}
	
	/**
	 * @return  A Properties object containing all of the data required to reconstruct the current state
	 *          of the RigidBody objects stored by this Simulation object.
	 */
	public Properties generateProperties() {
		Properties p = new Properties();
		
		p.setProperty("simulation.dt", String.valueOf(this.dt));
		p.setProperty("simulation.time", String.valueOf(this.totalTime));
		p.setProperty("simulation.fdt", String.valueOf(this.fdt));
		p.setProperty("simulation.vdt", String.valueOf(this.vdt));
		
		p.setProperty("bodies.length", String.valueOf(this.bodies.length));
		
		if (RayTracingEngine.castShadows == false) p.setProperty("render.shadows", "false");
		
		Vector cl = ((PinholeCamera)super.getCamera()).getLocation();
		Vector cv = ((PinholeCamera)super.getCamera()).getViewingDirection();
		
		double foc = ((PinholeCamera)super.getCamera()).getFocalLength();
		double w = ((PinholeCamera)super.getCamera()).getProjectionWidth();
		double h = ((PinholeCamera)super.getCamera()).getProjectionHeight();
		
		p.setProperty("camera.loc.x", String.valueOf(cl.getX()));
		p.setProperty("camera.loc.y", String.valueOf(cl.getY()));
		p.setProperty("camera.loc.z", String.valueOf(cl.getZ()));
		
		p.setProperty("camera.view.x", String.valueOf(cv.getX()));
		p.setProperty("camera.view.y", String.valueOf(cv.getY()));
		p.setProperty("camera.view.z", String.valueOf(cv.getZ()));
		
		p.setProperty("camera.foc", String.valueOf(foc));
		p.setProperty("camera.proj.w", String.valueOf(w));
		p.setProperty("camera.proj.h", String.valueOf(h));
		
		for (int i = 0; i < this.bodies.length; i++) {
			AbstractSurface surface = null;
			if (super.get(i) instanceof AbstractSurface) surface = (AbstractSurface) super.get(i);
			
			if (surface instanceof Sphere) {
				Sphere s = (Sphere)surface;
				
				p.setProperty("bodies." + i + ".type", "sphere");
				p.setProperty("bodies." + i + ".size", String.valueOf(s.getSize()));
				
				SphericalLight light = s.getLight();
				
				if (light != null) {
					double at[] = light.getAttenuationCoefficients();
					
					p.setProperty("bodies." + i + ".light.on", "true");
					p.setProperty("bodies." + i + ".light.intensity", String.valueOf(light.getIntensity()));
					p.setProperty("bodies." + i + ".light.samples", String.valueOf(light.getSampleCount()));
					p.setProperty("bodies." + i + ".light.ata", String.valueOf(at[0]));
					p.setProperty("bodies." + i + ".light.atb", String.valueOf(at[1]));
					p.setProperty("bodies." + i + ".light.atc", String.valueOf(at[2]));
				}
			}
			
			if (surface != null) {
				Iterator itr = surface.getShaderSet().iterator();
				
				w: while (itr.hasNext()) {
					Shader sh = (Shader)itr.next();
					
					if (sh instanceof RigidBodyStateShader) {
						int type = ((RigidBodyStateShader)sh).getType();
						String rshtype = "";
						
						if (type == RigidBodyStateShader.FORCE)
							rshtype = "force";
						else if (type == RigidBodyStateShader.VELOCITY)
							rshtype = "velocity";
						else
							continue w;
						
						p.setProperty("bodies." + i + ".shade.rbstate", rshtype);
					} else if (sh instanceof ReflectionShader) {
						p.setProperty("bodies." + i + ".shade.ref",
										String.valueOf(((ReflectionShader)sh).getReflectivity()));
					}
				}
			}
			
			p.setProperty("bodies." + i + ".mass", String.valueOf(this.bodies[i].getMass()));
			
			Vector loc = this.bodies[i].getLocation();
			p.setProperty("bodies." + i + ".loc.x", String.valueOf(loc.getX()));
			p.setProperty("bodies." + i + ".loc.y", String.valueOf(loc.getY()));
			p.setProperty("bodies." + i + ".loc.z", String.valueOf(loc.getZ()));
			
			Vector rot = this.bodies[i].getRotation();
			p.setProperty("bodies." + i + ".rot.x", String.valueOf(rot.getX()));
			p.setProperty("bodies." + i + ".rot.y", String.valueOf(rot.getY()));
			p.setProperty("bodies." + i + ".rot.z", String.valueOf(rot.getZ()));
			
			Vector lv = this.bodies[i].getLinearVelocity();
			p.setProperty("bodies." + i + ".lv.x", String.valueOf(lv.getX()));
			p.setProperty("bodies." + i + ".lv.y", String.valueOf(lv.getY()));
			p.setProperty("bodies." + i + ".lv.z", String.valueOf(lv.getZ()));
			
			Vector av = this.bodies[i].getAngularVelocity();
			p.setProperty("bodies." + i + ".av.x", String.valueOf(av.getX()));
			p.setProperty("bodies." + i + ".av.y", String.valueOf(av.getY()));
			p.setProperty("bodies." + i + ".av.z", String.valueOf(av.getZ()));
		}
		
		return p;
	}
	
	public List loadProperties(Properties p) {
		PinholeCamera c = null;
		
		String d = p.getProperty("render.debug");
		if (d != null && d.equals("true")) {
			Settings.produceOutput = true;
			Settings.produceRayTracingEngineOutput = true;
			Settings.rayEngineOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			Settings.produceShaderOutput = true;
			Settings.shaderOut = new JTextAreaPrintWriter(new JTextArea(20, 40));
			DiffuseShader.produceOutput = true;
			
			DebugOutputPanel outputPanel = new DebugOutputPanel();
			outputPanel.showPanel();
		}
		
		this.fdt = Double.parseDouble(p.getProperty("simulation.fdt", "1.0"));
		this.vdt = Double.parseDouble(p.getProperty("simulation.vdt", "-1.0"));
		this.totalTime = Double.parseDouble(p.getProperty("simulation.time", "0.0"));
		
		String shadows = p.getProperty("render.shadows");
		if (shadows != null && shadows.equals("false")) RayTracingEngine.castShadows = false;
		
		List bodies = new ArrayList();
		
		Vector cl = new Vector(0.0, 0.0, 0.0);
		Vector cv = new Vector(0.0, 0.0, 1.0);
		
		String cx = p.getProperty("camera.loc.x");
		String cy = p.getProperty("camera.loc.y");
		String cz = p.getProperty("camera.loc.z");
		
		String vx = p.getProperty("camera.view.x");
		String vy = p.getProperty("camera.view.y");
		String vz = p.getProperty("camera.view.z");
		
		String f = p.getProperty("camera.foc");
		String pw = p.getProperty("camera.proj.w");
		String ph = p.getProperty("camera.proj.h");
		
		if (cx != null) cl.setX(Double.parseDouble(cx));
		if (cy != null) cl.setY(Double.parseDouble(cy));
		if (cz != null) cl.setZ(Double.parseDouble(cz));
		
		if (vx != null) cv.setX(Double.parseDouble(vx));
		if (vy != null) cv.setY(Double.parseDouble(vy));
		if (vz != null) cv.setZ(Double.parseDouble(vz));
		
		c = new PinholeCamera(cl, cv, new Vector(0.0, 1.0, 0.0));
		
		if (f != null) c.setFocalLength(Double.parseDouble(f));
		if (pw != null) c.setProjectionWidth(Double.parseDouble(pw));
		if (ph != null) c.setProjectionHeight(Double.parseDouble(ph));
		
		c.updateUVW();
		
		dt = Double.parseDouble(p.getProperty("simulation.dt", "1"));
		
		int len = Integer.parseInt(p.getProperty("bodies.length", "0"));
		
		i: for (int i = 0; i < len; i++) {
			String type = p.getProperty("bodies." + i + ".type");
			double size = Double.parseDouble(p.getProperty("bodies." + i + ".size", "1.0"));
			
			RigidBody b = null;
			
			if (type == null) {
				continue i;
			} else if (type.equals("sphere")) {
				b = new Sphere();
				((Sphere)b).setRadius(size);
				((Sphere)b).setColor(new RGB(0.8, 0.8, 0.8));
				
				String lit = p.getProperty("bodies." + i + ".light.on");
				
				if (lit != null) {
					double intensity = Double.parseDouble(p.getProperty("bodies." + i + ".light.intensity", "0.0"));
					int samples = Integer.parseInt(p.getProperty("bodies." + i + ".light.samples", "0"));
					
					double ata = Double.parseDouble(p.getProperty("bodies." + i + ".light.ata", "0.0"));
					double atb = Double.parseDouble(p.getProperty("bodies." + i + ".light.atb", "0.0"));
					double atc = Double.parseDouble(p.getProperty("bodies." + i + ".light.atc", "1.0"));
					
					((Sphere)b).setLighting(true);
					
					SphericalLight light = ((Sphere)b).getLight();
					light.setColor(new RGB(1.0, 1.0, 1.0));
					light.setIntensity(intensity);
					light.setSampleCount(samples);
					light.setAttenuationCoefficients(ata, atb, atc);
					
					super.addLight((Light)b);
				}
			}
			
			if (b instanceof AbstractSurface) {
				AbstractSurface s = (AbstractSurface)b;
				
				String rbstate = p.getProperty("bodies." + i + ".shade.rbstate");
				
				if (rbstate != null) {
					BlendingShader bs = new BlendingShader(new RGB(0.8, 0.0, 0.0), new RGB(0.0, 0.0, 0.8));
					
					if (rbstate.equals("force"))
						s.addShader(new RigidBodyStateShader(RigidBodyStateShader.FORCE, 0.0, 1.0, bs));
					else if (rbstate.equals("velocity"))
						s.addShader(new RigidBodyStateShader(RigidBodyStateShader.VELOCITY, 0.0, 1.0, bs));
				}
				
				String ref = p.getProperty("bodies." + i + ".shade.ref");
				
				if (ref != null) {
					s.addShader(new ReflectionShader(Double.parseDouble(ref), new RGB(1.0, 1.0, 1.0)));
				}
			}
			
			RigidBody.State s = b.getState();
			
			double mass = Double.parseDouble(p.getProperty("bodies." + i + ".mass", "1.0"));
			
			double locX = Double.parseDouble(p.getProperty("bodies." + i + ".loc.x", "0.0"));
			double locY = Double.parseDouble(p.getProperty("bodies." + i + ".loc.y", "0.0"));
			double locZ = Double.parseDouble(p.getProperty("bodies." + i + ".loc.z", "0.0"));
			
			double rotX = Double.parseDouble(p.getProperty("bodies." + i + ".rot.x", "0.0"));
			double rotY = Double.parseDouble(p.getProperty("bodies." + i + ".rot.y", "0.0"));
			double rotZ = Double.parseDouble(p.getProperty("bodies." + i + ".rot.z", "0.0"));
			
			double lvX = Double.parseDouble(p.getProperty("bodies." + i + ".lv.x", "0.0"));
			double lvY = Double.parseDouble(p.getProperty("bodies." + i + ".lv.y", "0.0"));
			double lvZ = Double.parseDouble(p.getProperty("bodies." + i + ".lv.z", "0.0"));
			
			double avX = Double.parseDouble(p.getProperty("bodies." + i + ".av.x", "0.0"));
			double avY = Double.parseDouble(p.getProperty("bodies." + i + ".av.y", "0.0"));
			double avZ = Double.parseDouble(p.getProperty("bodies." + i + ".av.z", "0.0"));
			
			s.setMass(mass);
			s.setLocation(new Vector(locX, locY, locZ));
			s.setRotation(new Vector(rotX, rotY, rotZ));
			s.setLinearVelocity(new Vector(lvX, lvY, lvZ));
			s.setAngularVelocity(new Vector(avX, avY, avZ));
			
			b.updateModel();
			
			bodies.add(b);
		}
		
		double ambient = Double.parseDouble(p.getProperty("light.ambient", "0.0"));
		
		if (ambient != 0.0) this.addLight(new DirectionalAmbientLight(ambient,
										new RGB(1.0, 1.0, 1.0), new Vector(0.0, -1.0, -0.3)));
		
		String viewFrom = p.getProperty("camera.view.from");
		String viewTo = p.getProperty("camera.view.to");
		
		if (viewFrom != null && viewTo != null) {
			int from = Integer.parseInt(viewFrom);
			int to = Integer.parseInt(viewTo);
			
			Vector vd = ((RigidBody)bodies.get(to)).getState().getLocation().subtract(
							((RigidBody)bodies.get(from)).getState().getLocation());
			vd.divideBy(vd.length());
			
			Vector l = vd.multiply(((AbstractSurface)bodies.get(from)).getSize() * 1.05);
			
			c.setLocation(l);
			c.setViewDirection(vd);
		}
		
		super.setCamera(c);
		
		return bodies;
	}
	
	/**
	 * Writes the current image to a file that is labeled using the specified values.
	 * 
	 * @param i  The iteration number of the image.
	 * @param instance  The instance string for the animation.
	 */
	public void writeImage(int i, String instance) {
		try {
			System.out.print("Encoding frame " + i + "/" + this.itr + ": ");
			
			String fn = this.dir + "frame_" + instance + "." + i + ".jpeg";
			File f = new File(fn);
			
			RGB image[][] = RayTracingEngine.render(this, this.imageWidth, this.imageHeight, 1, 1, null);
			this.image = GraphicsConverter.convertToAWTImage(image);
			
			BufferedImage buff = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_RGB);
			Graphics g = buff.getGraphics();
			g.drawImage(this.image, 0, 0, null);
			g.setColor(Color.black);
			g.setFont(new Font("Monospaced", Font.PLAIN, 16));
			// g.drawString(this.bodies[0].toString(), 10, this.imageHeight - 30);

//			TODO  Write image
//			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(f));
//			JPEGEncodeParam param = encoder.getDefaultJPEGEncodeParam(buff);
//			param.setQuality(1.0f, true);
//			encoder.encode(buff, param);
			
			this.inputFiles.addElement(f);
			System.out.println(fn);
		} catch (Exception ioe) {
			System.err.println("Error writing image file for frame " + i + " : " + ioe.toString());
		}
	}
	
	/**
	 * Write a sript to be used to compose the output image to form an animation.
	 * 
	 * @param instance  The instance string for the animation.
	 */
	public void writeEncodeScript(String instance) {
		System.out.print("Writing encode script: ");
		
		try (PrintWriter out = new PrintWriter(new FileWriter(new File(this.dir + "encode.sh")))) {
			out.println("#!/bin/sh");
			out.print("mencoder mf://");
			
			Iterator itr = this.inputFiles.iterator();
			int i = 0;
			int l = this.inputFiles.size();
			
			while (itr.hasNext()) {
				out.print(((File)itr.next()).getName());
				if (i < l) out.print(",");
				
				i++;
			}
			
			int w = this.imageWidth;
			int h = this.imageHeight;
			int fps = (int)(1 / this.fdt);
			
			out.print(" -mf w=" + w + ":h=" + h + ":fps=" + fps + ":type=jpg -ovc lavc -lavcopts vcodec=mpeg4 -oac copy -o output.avi");
			out.println();
			out.flush();
			out.close();
			
			System.out.println("Done");
		} catch (IOException ioe) {
			System.out.println("Error writing encode script: " + ioe);
		}
	}
}
