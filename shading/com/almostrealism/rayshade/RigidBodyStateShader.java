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

package com.almostrealism.rayshade;

import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorMultiplier;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;

import org.almostrealism.physics.RigidBody;

/**
 * A RigidBodyStateShader object can be used to modify the display of other shaders based on a property
 * of the state of a RigidBody object. A RigidBodyStateShader modifies the light direction and intensity
 * based on the direction and intensity of either the velocity or force experienced by a rigid body.
 * 
 * @author Mike Murray
 */
public class RigidBodyStateShader implements Shader {
	public static final int VELOCITY = 1;
	public static final int FORCE = 2;
	
	private int type;
	private double min, max;
	private Shader shader;
	
	/**
	 * Constructs a new RigidBodyStateShader object that shades based on the
	 * state property specified by the integer type code.
	 * 
	 * @param type  Integer type code.
	 * @param min  Minimum value of state property.
	 * @param max  Maximum value of state property.
	 * @param s  Shader instance to use for shading.
	 */
	public RigidBodyStateShader(int type, double min, double max, Shader s) {
		if (type > 2 || type < 1) throw new IllegalArgumentException("Invalid type code: " + type);
		
		this.type = type;
		
		this.min = min;
		this.max = max;
		
		this.shader = s;
	}
	
	/**
	 * @return  The integer type code for this RigidBodyStateShader object.
	 */
	public int getType() { return this.type; }
	
	/**
	 * @return  The Shader object stored by this RigidBodyStateShader object.
	 */
	public Shader getShader() { return this.shader; }
	
	/**
	 * @see org.almostrealism.color.Shader#shade(ShaderContext, DiscreteField)
	 */
	public ColorProducer shade(ShaderContext p, DiscreteField f) {
		if (p.getSurface() instanceof RigidBody == false) return new RGB(1.0, 1.0, 1.0);
		
		RigidBody.State state = ((RigidBody)p.getSurface()).getState();
		
		Vector d = null;
		
		if (this.type == RigidBodyStateShader.VELOCITY)
			d = state.getLinearVelocity();
		else
			d = state.getForce();
		
		double m = (d.length() - this.min) / (this.max - this.min);
		if (m < 0.0) m = 0.0;
		if (m > 1.0) m = 1.0;
		
		d.divideBy(d.length());
		p.setLightDirection(d);
		
		return new ColorMultiplier(this.shader.shade(p, f), m);
	}
}
