/*
 * Copyright (C) 2004-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.physics;

import com.almostrealism.raytracer.camera.PinholeCamera;
import net.sf.j3d.util.Vector;

/**
 * @author Mike Murray
 */
public class CameraObject extends PinholeCamera implements UpdateListener {
	private RigidBody.State model;
	
	public CameraObject(RigidBody model) {
		super(model.getState().getLocation(), model.getState().getRotation(), new Vector(0.0, 1.0, 0.0));
		
		this.model = model.getState();
		this.model.addUpdateListener(this);
	}
	
	public void update() {
		super.setLocation(this.model.getLocation());
		super.setViewingDirection(this.model.getRotation());
	}
	
	public void forward(double d) {
		this.model.linearImpulse(model.getRotation().divide(model.getRotation().length()).multiply(d));
	}
	
	public void backward(double d) {
		this.model.linearImpulse(model.getRotation().divide(model.getRotation().length()).multiply(-d));
	}
	
	public void turnLeft() {
		// this.model.angularImpulse();
	}
	
	public void turnRight() {
		// this.model.angularImpulse();
	}
	
	public void jump(double d) { this.model.linearImpulse(new Vector(0.0, d, 0.0)); }
}