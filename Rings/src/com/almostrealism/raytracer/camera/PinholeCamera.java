/*
 * Copyright (C) 2004-05  Mike Murray
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

/*
 * This code was inspired by an algorithm presented in Realistic Ray Tracing by Peter Shirley.
 */

package com.almostrealism.raytracer.camera;

import com.almostrealism.raytracer.engine.Ray;
import com.almostrealism.util.Vector;

import net.sf.j3d.run.Settings;

/**
 * A PinholeCamera object represents a camera in 3D. A PinholeCamera object stores the location, viewing direction,
 * up direction, focal length, and projection dimensions which are used for rendering.
 * When constructing a PinholeCamera object you must make these specifications carefully.
 * The camera location is, as expected, the location from which the camera views, represented as a vector.
 * This value is by default at the origin. The viewing direction is a vector that represents
 * the direction the camera is viewing. This value is by default aligned to the positive z axis,
 * or (0.0, 0.0, 1.0). The up direction is a vector that represents the orientation of the camera's "up."
 * This value is by default aligned with the positive y axis or (0.0, 1.0, 0.0).
 * The focal length of the camera can be thought of as the distance from the camera location to the projection.
 * The focal length is also the tangent of half the vertical field of view. The projection dimensions are the
 * dimensions of the projection that the camera will produce. By default the projection dimensions are set
 * to 0.36 by 0.24 to produce a 35mm film aspect ratio.
 * A Camera object also stores three perpendicular vectors that describe a coordinate system.
 * This is the camera coordinate system and is used for projection. These vectors are computed and updated
 * automatically based on the viewing direction and up direction vectors.
 * 
 * @author Mike Murray
 */
public class PinholeCamera extends OrthographicCamera {
  private double focalLength = 1.0;
  private double blur = 0.0;

	/**
	 * Constructs a PinholeCamera object with all default values as described above.
	 */
	public PinholeCamera() {
		super();
		
		super.setProjectionDimensions(0.36, 0.24);
		this.setFocalLength(0.1);
	}
	
	/**
	 * Constructs a PinholeCamera object with the specified location, viewing direction, and up direction,
	 * but with default focal length and projection dimensions as specified above.
	 */
	public PinholeCamera(Vector location, Vector viewDirection, Vector upDirection) {
		super(location, viewDirection, upDirection);
		
		super.setProjectionDimensions(0.36, 0.24);
		this.setFocalLength(0.1);
	}
	
	/**
	 * Constructs a PinholeCamera object with the specified location, viewing direction, up direction,
	 * focal length, and projection dimensions.
	 */
	public PinholeCamera(Vector location, Vector viewDirection, Vector upDirection,
			double focalLength, double projectionX, double projectionY) {
		super(location, viewDirection, upDirection, projectionX, projectionY);
		
		this.setFocalLength(focalLength);
	}
	
	/**
	 * Constructs a PinholeCamera object with the specified location, viewing direction, up direction,
	 * and focal length. Projection dimensions are determined using the specified fields of view.
	 * 
	 * @param location  Camera location.
	 * @param viewDiretion  Camera viewing direction.
	 * @param upDirection  Camera up direction.
	 * @param focalLength  Camera focal length.
	 * @param fov  Camera fields of view (radians) {horizontal FOV, vertical FOV}.
	 */
	public PinholeCamera(Vector location, Vector viewDirection, Vector upDirection,
	        double focalLength, double fov[]) {
	    if (fov.length < 2) throw new IllegalArgumentException("Illegal argument: Wrong size array.");
	    
		if (Settings.produceOutput && Settings.produceCameraOutput) {
			Settings.cameraOut.println("Constructing new camera (" + this.toString() + ")...");
		}
		
		this.setLocation(location);
		this.setViewingDirection(viewDirection);
		this.setUpDirection(upDirection);
		
		this.setFocalLength(focalLength);
		this.setProjectionDimensions(2 * focalLength * Math.tan(fov[0] / 2), 2 * focalLength * Math.tan(fov[1] / 2));
	}
	
	/**
	 * Sets the focal length of this PinholeCamera object to the specified focal length.
	 */
	public void setFocalLength(double focalLength) { this.focalLength = focalLength; }
	
	/**
	 * Returns the focal length of this PinholeCamera object as a double value.
	 */
	public double getFocalLength() { return this.focalLength; }
	
	/**
	 * @return  {Horizontal FOV, Vertical FOV} Measured in radians.
	 */
	public double[] getFOV() {
		return new double [] { 2.0 * Math.atan(super.getProjectionWidth() / (2.0 * this.focalLength)),
								2.0 * Math.atan(super.getProjectionHeight() / (2.0 * this.focalLength)) };
	}
	
	public void setBlur(double blur) { this.blur = blur; }
	
	public double getBlur() { return this.blur; }
	
	/**
	 * Returns a Ray object that represents a line of sight from the camera represented by this PinholeCamera object.
	 * The first two parameters are the coordinates across the camera. These coordinates corespond to the pixels on the rendered image.
	 * The second two parameters specifiy the total integer width and height of the screen.
	 * Although the pixels on the screen must be in integer coordinates, this method provides the ability to create super high resolution images
	 * by allowing you to devote a single pixel to only a fraction of the theoretical camera surface. This effect can be used to produce large images
	 * from small scenes while retaining acuracy.
	 */
	public Ray rayAt(double i, double j, int screenWidth, int screenHeight) {
		if (Settings.produceOutput && Settings.produceCameraOutput) {
			Settings.cameraOut.println("CAMERA: U = " + this.u.toString() + ", V = " + this.v.toString() + ", W = " + this.w.toString());
		}
		
		double au = -(super.getProjectionWidth() / 2);
		double av = -(super.getProjectionHeight() / 2);
		double bu = super.getProjectionWidth() / 2;
		double bv = super.getProjectionHeight() / 2;
		
		Vector p = super.u.multiply((au + (bu - au) * (i / (screenWidth - 1))));
		Vector q = super.v.multiply((av + (bv - av) * (j / (screenHeight - 1))));
		Vector r = super.w.multiply(-this.focalLength);
		
		Vector rayDirection = p;
		rayDirection.addTo(q);
		rayDirection.addTo(r);
		
		double l = rayDirection.length();
		
		if (this.blur != 0.0) {
			double a = this.blur * (-0.5 + Math.random());
			double b = this.blur * (-0.5 + Math.random());
			
			Vector u, v, w = (Vector) rayDirection.clone();
			
			Vector t = (Vector) rayDirection.clone();
			
			if (t.getX() < t.getY() && t.getY() < t.getZ()) {
				t.setX(1.0);
			} else if (t.getY() < t.getX() && t.getY() < t.getZ()) {
				t.setY(1.0);
			} else {
				t.setZ(1.0);
			}
			
			w.divideBy(w.length());
			
			u = t.crossProduct(w);
			u.divideBy(u.length());
			
			v = w.crossProduct(u);
			
			rayDirection.addTo(u.multiply(a));
			rayDirection.addTo(v.multiply(b));
			rayDirection.multiplyBy(l / rayDirection.length());
		}
		
		Ray ray = new Ray(super.getLocation(), rayDirection);
		
		if (Settings.produceOutput && Settings.produceCameraOutput) {
			Settings.cameraOut.println("CAMERA (" + this.toString() + ") : Ray at (" + i + ", " + j + ", " + screenWidth + ", " + screenHeight + ") = " + ray.toString());
		}
		
		return ray;
	}
	
	public String toString() {
		return "PinholeCamera - " +
				super.getLocation() + " " +
				super.getViewDirection() + " " +
				super.getProjectionWidth() + "x" +
				super.getProjectionHeight();
	}
}

