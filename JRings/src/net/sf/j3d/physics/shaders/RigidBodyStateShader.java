/*
 * Copyright (C) 2005-06  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package net.sf.j3d.physics.shaders;

import net.sf.j3d.physics.RigidBody;

import com.almostrealism.raytracer.shaders.Shader;
import com.almostrealism.raytracer.shaders.ShaderParameters;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;

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
	 * @see com.almostrealism.raytracer.shaders.Shader#shade(com.almostrealism.raytracer.shaders.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) {
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
		
		return this.shader.shade(p).multiply(m);
	}

	/**
	 * @see com.almostrealism.raytracer.graphics.ColorProducer#evaluate(java.lang.Object[])
	 */
	public RGB evaluate(Object args[]) { return this.shade((ShaderParameters)args[0]); }
}
