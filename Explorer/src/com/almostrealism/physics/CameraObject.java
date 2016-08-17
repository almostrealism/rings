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

import org.almostrealism.space.Vector;

import com.almostrealism.projection.PinholeCamera;

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