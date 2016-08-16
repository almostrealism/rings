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

import java.awt.Graphics;

import org.almostrealism.space.TransformMatrix;
import org.almostrealism.space.Vector;

import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.engine.ParticleGroup;
import com.almostrealism.raytracer.graphics.ParticleGroupRenderer;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.lighting.SphericalLight;
import com.almostrealism.raytracer.lighting.SurfaceLight;


/**
 * @author Mike Murray
 */
public class Sphere extends com.almostrealism.raytracer.primitives.Sphere implements RigidBody,
																ParticleGroup,
																SurfaceLight {
	private State state;
	
	private SphericalLight light;
	
	private int radialSample;
	private double vertices[][];
	
	private TransformMatrix rotateXMatrix, rotateYMatrix, rotateZMatrix;
	
	/**
	 * Constructs a new Sphere object using (0.0, 0.0, 0.0) for all vector values
	 * 1.0 for mass, 1.0 for the coefficient of restitution, and 4 for the radial sample.
	 */
	public Sphere() {
		this(new Vector(0.0, 0.0, 0.0), new Vector(0.0, 0.0, 0.0),
			new Vector(0.0, 0.0, 0.0), new Vector(0.0, 0.0, 0.0),
			new Vector(0.0, 0.0, 0.0), new Vector(0.0, 0.0, 0.0),
			1.0, 1.0, 1.0, 4);
	}
	
	/**
	 * Constructs a new Sphere object using the specified initial rigid body state values.
	 * 
	 * @param x  location
	 * @param r  rotation
	 * @param v  linear velocity
	 * @param w  angular velocity
	 * @param f  force
	 * @param t  torque
	 * @param mass  mass
	 * @param e  coefficient of restitution
	 * @param radius  radius
	 */
	public Sphere(Vector x, Vector r, Vector v, Vector w, Vector f, Vector t,
					double mass, double e, double radius, int radialSample) {
		super(x, radius);
		
		double a = (2.0 / 5.0) * mass * radius * radius;
		
		this.radialSample = radialSample;
		this.vertices = new double[radialSample * radialSample][3];
		
		this.state = new State();
		this.state.init(x, r, v, w, f, t, mass, new TransformMatrix(new double[][] {{a, 0.0, 0.0, 0.0},
																			{0.0, a, 0.0, 0.0},
																			{0.0, 0.0, a, 0.0},
																			{0.0, 0.0, 0.0, 1.0}}), e);
	}
	
	public void setRadius(double radius) {
		super.setSize(radius);
		this.updateModel();
	}
	
	public double getRadius() { return super.getSize(); }
	
	public void updateModel() {
		double r = super.getSize();
		double a = (2.0 / 5.0) * this.state.mass * r * r;
		
		this.state.in = new TransformMatrix(new double[][] {{a, 0.0, 0.0, 0.0},
														{0.0, a, 0.0, 0.0},
														{0.0, 0.0, a, 0.0},
														{0.0, 0.0, 0.0, 1.0}});
		
		Vector rn = this.state.r.divide(this.state.r.length());
		
		this.rotateXMatrix = TransformMatrix.createRotateXMatrix(Math.acos(rn.getX()));
		this.rotateYMatrix = TransformMatrix.createRotateYMatrix(Math.acos(rn.getY()));
		this.rotateZMatrix = TransformMatrix.createRotateZMatrix(Math.acos(rn.getZ()));
		
		super.setLocation(this.state.x);
		
		if (this.light != null) {
			this.light.setLocation(super.getLocation());
			this.light.setSize(1.01 * r);
		}
		
		this.updateVertices();
	}
	
	public void updateVertices() {
		for (int i = 0; i < this.radialSample; i++) {
			double theta = 2 * Math.PI * i / this.radialSample;
			double sintheta = Math.sin(theta);
			double costheta = Math.cos(theta);
			
			for (int j = 0; j < this.radialSample; j++) {
				int k = i * this.radialSample + j;
				
				double phi = 2 * Math.PI * j / this.radialSample;
				
				double sinphi = Math.sin(phi);
				double cosphi = Math.cos(phi);
				
				double r = super.getSize();
				
				this.vertices[k][0] = this.state.x.getX() + r * cosphi * sintheta;
				this.vertices[k][1] = this.state.x.getY() + r * sinphi * sintheta;
				this.vertices[k][2] = this.state.x.getZ() + r * costheta;
				
				Vector v = new Vector(this.vertices[k][0], this.vertices[k][1], this.vertices[k][2]);
				
				v.subtractFrom(this.state.x);
				
				// this.rotateXMatrix.transform(v, TransformMatrix.TRANSFORM_AS_LOCATION);
				// this.rotateYMatrix.transform(v, TransformMatrix.TRANSFORM_AS_LOCATION);
				// this.rotateZMatrix.transform(v, TransformMatrix.TRANSFORM_AS_LOCATION);
				
				v.addTo(this.state.x);
				
				this.vertices[k][0] = v.getX();
				this.vertices[k][1] = v.getY();
				this.vertices[k][2] = v.getZ();
			}
		}
	}
	
	public Vector[] intersect(RigidBody b) {
		double r = super.getSize();
		
		if (b instanceof Sphere) {
			State s = ((Sphere)b).getState();
			double sr = ((Sphere)b).getSize();
			
			Vector d = s.x.subtract(this.state.x);
			
			if (d.length() < (sr + r)) {
				d.divideBy(d.length());
				return new Vector[] {d.multiply(r), d};
			} else {
				return new Vector[0];
			}
		} else if (b instanceof Plane) {
			State p = ((Plane)b).getState();
			double d = this.state.x.getY() - p.x.getY();
			
			if (Math.abs(d) <= r) {
				if (d >= 0)
					return new Vector[] {new Vector(this.state.x.getX(), p.x.getY(), this.state.x.getZ()), new Vector(0.0, 1.0, 0.0)};
				else
					return new Vector[] {new Vector(this.state.x.getX(), p.x.getY(), this.state.x.getZ()), new Vector(0.0, -1.0, 0.0)};
			} else {
				return new Vector[0];
			}
		} else {
			return new Vector[0];
		}
	}
	
	/**
	 * @see com.almostrealism.raytracer.engine.ParticleGroup#getParticleVertices()
	 */
	public double[][] getParticleVertices() { return this.vertices; }
	
	/**
	 * @see RigidBody#getState()
	 */
	public State getState() { return this.state; }
	
	/**
	 * @see RigidBody#draw(Camera, Graphics, double, double, double)
	 */
	public void draw(Camera c, Graphics g, double ox, double oy, double scale) {
		ParticleGroupRenderer.draw(this, (PinholeCamera)c, g, ox, oy, scale, 0.1, 1.0, 20);
	}
	
	public void setLighting(boolean on) {
		if (on) {
			this.light = new SphericalLight();
			this.updateModel();
		} else {
			this.light = null;
		}
	}
	
	/**
	 * Sets the SphericalLight object used by this Sphere object.
	 * 
	 * @param light  SphericalLight object to use.
	 */
	public void setLight(SphericalLight light) {
		this.light = light;
		this.updateModel();
	}
	
	/**
	 * @return  The SphericalLight object stored by this Sphere object.
	 */
	public SphericalLight getLight() { return this.light; }
	
	/**
	 * @see com.almostrealism.raytracer.lighting.SurfaceLight#getSamples(int)
	 */
	public Light[] getSamples(int samples) {
		if (this.light == null)
			return new Light[0];
		else
			return this.light.getSamples(samples);
	}
	
	/**
	 * @see com.almostrealism.raytracer.lighting.SurfaceLight#getSamples()
	 */
	public Light[] getSamples() {
		if (this.light == null)
			return new Light[0];
		else
			return this.light.getSamples();
	}
	
	/**
	 * @see com.almostrealism.raytracer.lighting.Light#setIntensity(double)
	 */
	public void setIntensity(double intensity) { if (this.light != null) this.light.setIntensity(intensity); }
	
	/**
	 * @see com.almostrealism.raytracer.lighting.Light#getIntensity()
	 */
	public double getIntensity() {
		if (this.light == null)
			return 0.0;
		else
			return this.light.getIntensity();
	}
}
