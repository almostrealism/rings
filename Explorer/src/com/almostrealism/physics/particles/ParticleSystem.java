/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.physics.particles;

import javax.media.j3d.Appearance;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;

import com.almostrealism.physics.particles.conf.SampleSetConfiguration;

public class ParticleSystem extends Shape3D {
	private final static int POINTSIZE = 2;
	
	private final static Color3f endColor = new Color3f(1.0f, 1.0f, 0.6f);
	private final static Color3f startColor = new Color3f(1.0f, 0.0f, 0.6f);
	
	private PointArray points;
	private SampleSet control;
	
	private float cs[], vel[], acel[], color[];
	private int tot;

	public ParticleSystem(int tot, int delay, SampleSetConfiguration conf) {
		this.tot = tot;
		this.control = new SampleSet(this, delay, conf.samples, conf.add, conf.multi, conf.input);
		this.points = new ParticleGeometry(tot);

		// pointParts.setCapability(PointArray.ALLOW_COORDINATE_WRITE);
		// pointParts.setCapability(PointArray.ALLOW_COLOR_WRITE);
		
		this.points.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
		this.points.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
		
		// setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		
		this.createGeometry();
		
		Appearance a = new Appearance();
		PointAttributes pa = new PointAttributes();
		pa.setPointSize(POINTSIZE);    // causes z-ordering bug
		a.setPointAttributes(pa);
		super.setAppearance(a);
	}
	
	public SampleSet getControl() { return this.control; }
	protected void updateData(GeometryUpdater update) { this.points.updateData(update); }
	protected int getParticleCount() { return this.tot; }
	
	private void createGeometry() {
		this.cs = new float[tot * 3];
		this.vel = new float[tot * 3];
		this.acel = new float[tot * 3];
		this.color = new float[tot * 3];
		
		for(int i = 0; i < tot * 3; i = i + 3) initParticle(i);
		
		this.points.setCoordRefFloat(cs);
		this.points.setColorRefFloat(this.color);
		
		super.setGeometry(this.points);
	}


	private void initParticle(int i) {
		this.cs[i] = (float) ((Math.random() - 0.5) * 10);
		this.cs[i + 1] = (float) ((Math.random()) * 10);
		this.cs[i + 2] = (float) ((Math.random() - 0.5) * 10);
		
		this.acel[i] = 0.0f;
		this.acel[i + 1] = 0.0f;
		this.acel[i + 2] = 0.0f;
		
		this.color[i] = startColor.x;
		this.color[i + 1] = startColor.y;
		this.color[i + 2] = startColor.z;
	}
	
	protected static class ParticleGeometry extends PointArray {
		protected PointArray points;
		protected int total;
		protected float cs[], vel[], acel[], color[];
		
		public ParticleGeometry(int tot) {
			super(tot, PointArray.COORDINATES | PointArray.COLOR_3 | PointArray.BY_REFERENCE);
		}
	}
}
