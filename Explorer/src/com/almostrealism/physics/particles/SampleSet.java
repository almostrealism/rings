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

package com.almostrealism.physics.particles;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupOnElapsedTime;

public class SampleSet extends Behavior implements GeometryUpdater {
	private static final float TIMESTEP = 0.005f;
	private static final float FORCE = -50f;
	
	private WakeupCondition time;
	private ParticleSystem system;
	
	private float samples[], mult[];
	private InputFunction input[];
	private int add[];
	
	public SampleSet(ParticleSystem system, int delay, float samples[],
					int add[], float multiply[], InputFunction input[]) {
		this.time = new WakeupOnElapsedTime(delay);
		this.system = system;
		
		this.samples = samples;
		this.add = add;
		this.mult = multiply;
		this.input = input;
	}
	
	public void initialize() { wakeupOn(time); }
	
	public void processStimulus(Enumeration criteria) {
		this.system.updateData(this);
		wakeupOn(time);
	}
	
	public void updateData(Geometry geo) {
		if (geo instanceof ParticleSystem.ParticleGeometry == false) return;
		ParticleSystem.ParticleGeometry s = (ParticleSystem.ParticleGeometry) geo;
		for (int i = 0; i < s.total * 3; i = i + 3) this.updateParticle(i, s);
	}
	
	protected void updateParticle(int i, ParticleSystem.ParticleGeometry geo) {
		geo.cs[i] += geo.vel[i] * TIMESTEP +
						0.5 * geo.acel[i] * TIMESTEP * TIMESTEP;
		geo.cs[i + 1] += geo.vel[i + 1] * TIMESTEP + 
						0.5 * geo.acel[i+1] * TIMESTEP * TIMESTEP;
		geo.cs[i + 2] += geo.vel[i + 2] * TIMESTEP +
						0.5 * geo.acel[i+2] * TIMESTEP * TIMESTEP;
		
		if (Float.isNaN(geo.cs[i])) geo.cs[i] = 0f;
		if (Float.isNaN(geo.cs[i + 1])) geo.cs[i + 1] = 0f;
		if (Float.isNaN(geo.cs[i + 2])) geo.cs[i + 2] = 0f;
		
		geo.vel[i] += geo.acel[i] * TIMESTEP;
		geo.vel[i + 1] += geo.acel[i + 1] * TIMESTEP;
		geo.vel[i + 2] += geo.acel[i + 2] * TIMESTEP;
		
		float f[] = this.force(geo.cs[i], geo.cs[i + 1], geo.cs[i + 2], i);
		
		geo.acel[i] = f[0];
		geo.acel[i + 1] = f[1];
		geo.acel[i + 2] = f[2];
		
		updateColour(i, f);
	}
	
	public float[] force(float x, float y, float z, int k) {
		float f[] = new float[3];
		
		/*
		i: for (int i = 0; i < tot; i = i + 3) {
			if (i == k) continue i;
			double dx = x - cs[i];
			double dy = y - cs[i + 1];
			double dz = z - cs[i + 2];
			
			double r = dx * dx + dy * dy + dz * dz;
			double rr = r * Math.sqrt(r);
			rr = rr / FORCE;
			f[0] += dx / rr;
			f[1] += dy / rr;
			f[2] += dz / rr;
		}
		*/
		
		if (Float.isNaN(f[0])) f[0] = 0f;
		if (Float.isNaN(f[1])) f[1] = 0f;
		if (Float.isNaN(f[2])) f[2] = 0f;
		
		return f;
	}

	private void updateColour(int i, float f[]) {
		float r = f[0] * f[0] + f[1] * f[1] + f[2] * f[2];
		r = 10 * r / FORCE;
		
//		color[i + 1] = 1 - r;
//		if (color[i + 1] < 0.0f) color[i + 1] = 0.0f;
//		
//		color[i + 2] = 1 - r;
//		if (color[i + 2] < 0.0f) color[i + 2] = 0.0f;
	}
}
