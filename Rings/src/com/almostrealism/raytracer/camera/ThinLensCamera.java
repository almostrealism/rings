/*
 * Copyright (C) 2005  Mike Murray
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

package com.almostrealism.raytracer.camera;

import com.almostrealism.raytracer.engine.Ray;

import net.sf.j3d.run.Settings;
import net.sf.j3d.util.Vector;

/**
 * A ThinLensCamera object provides a camera with viewing rays that originate
 * from a random point on a circular lens. By default the width and height of
 * the projection are set to 0.036 by 0.024 and the focal length is set to 0.05.
 * Also, the focus distance is set to 1.0 and the radius of the lens is set to 0.005
 * (default focal length / 10 to produce an f-number of 5).
 * 
 * @author Mike Murray
 */
public class ThinLensCamera extends PinholeCamera {
  private double focalLength, radius, w, h;

	/**
	 * Constructs a new ThinLensCamera object.
	 */
	public ThinLensCamera() {
		super();
		
		this.setProjectionDimensions(0.036, 0.024);
		this.setFocalLength(0.05);
		this.setFocus(1.0);
		this.setLensRadius(this.getFocalLength() / 10);
	}
	
	protected void updateProjectionDimensions() {
		double u = (super.getFocalLength() - this.focalLength) / this.focalLength;
		super.setProjectionDimensions(this.w * u, this.h * u);
	}
	
	/**
	 * Sets the projection dimensions used by this ThinLensCamera object to the specified values.
	 */
	public void setProjectionDimensions(double w, double h) {
		this.w = w;
		this.h = h;
		this.updateProjectionDimensions();
	}
	
	/**
	 * Sets the projection width of this ThinLensCamera object to the specified projection width.
	 */
	public void setProjectionWidth(double w) {
		this.w = w;
		this.updateProjectionDimensions();
	}
	
	/**
	 * Sets the projection height of this ThinLensCamera object to the specified projection height.
	 */
	public void setProjectionHeight(double h) {
		this.h = h;
		this.updateProjectionDimensions();
	}
	
	/**
	 * Returns the projection width of this ThinLensCamera object as a double value.
	 */
	public double getProjectionWidth() { return this.w; }
	
	/**
	 * Returns the projection height of this ThinLensCamera object as a double value.
	 */
	public double getProjectionHeight() { return this.h; }
	
	/**
	 * Sets the distance at which this ThinLensCamera object is focused.
	 * 
	 * @param focus  The focus distance to use.
	 */
	public void setFocus(double focus) {
		super.setFocalLength(focus);
		this.updateProjectionDimensions();
	}
	
	/**
	 * @return  The distance at which this ThinLensCamera object is focused.
	 */
	public double getFocus() { return super.getFocalLength(); }
	
	/**
	 * Sets the focal length used by this ThinLensCamera object.
	 * 
	 * @param focalLength  The focal length value to use.
	 */
	public void setFocalLength(double focalLength) {
		this.focalLength = focalLength;
		this.updateProjectionDimensions();
	}
	
	/**
	 * @return  The focal length value used by this ThinLensCamera object.
	 */
	public double getFocalLength() { return this.focalLength; }
	
	/**
	 * Sets the radius of the lens used by this ThinLensCamera object.
	 * 
	 * @param radius  The radius to use.
	 */
	public void setLensRadius(double radius) { this.radius = radius; }
	
	/**
	 * @return  The radius of the lens used by this ThinLensCamera object.
	 */
	public double getLensRadius() { return this.radius; }
	
	/**
	 * @return  {Horizontal FOV, Vertical FOV} Measured in radians.
	 */
	public double[] getFOV() {
		double u = (super.getFocalLength() - this.focalLength) / this.focalLength;
		
		return new double [] { 2.0 * Math.atan((u * w) / (2.0 * super.getFocalLength())),
								2.0 * Math.atan((u * h) / (2.0 * super.getFocalLength())) };
	}
	
	/**
	 * @return  A ray that is in the same direction as the ray returned
	 * by the PinholeCamera, but with a position that is a random sample
	 * from a disk.
	 */
	public Ray rayAt(double i, double j, int screenW, int screenH) {
		// super.setProjectionDimensions(w * u, h * u);
		Ray ray = super.rayAt(i, j, screenW, screenH);
		// uper.setProjectionDimensions(w, h);
		
		double r = Math.random() * this.radius;
		double theta = Math.random() * Math.PI * 2;
		Vector v = new Vector(r, Math.PI / 2.0, theta, Vector.SPHERICAL_COORDINATES);
		v.addTo(super.getLocation());
		
		Vector d = ray.getOrigin();
		d.subtractFrom(v);
		
		Vector rd = ray.getDirection();
		double rdl = rd.length();
		
		if (Settings.produceCameraOutput) {
			Settings.cameraOut.println("ThinLensCamera: " + w + " X " + h + " U = " + u);
			Settings.cameraOut.println("ThinLensCamera: Lens sample = " + v);
			Settings.cameraOut.println("ThinLensCamera: Ray origin = " + ray.getOrigin() +
										" direction = " + ray.getDirection());
		}
		
		ray.setOrigin(v);
		
		rd.addTo(d);
		rd.multiplyBy(rd.length() / rdl);
		ray.setDirection(rd);
		
		if (Settings.produceCameraOutput) {
			Settings.cameraOut.println("ThinLensCamera: New ray origin = " + ray.getOrigin() +
										" direction = " + ray.getDirection());
		}
		
		return ray;
	}
}
