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

package com.almostrealism.physics;

import java.io.IOException;

import javax.swing.JFrame;

import com.almostrealism.primitives.AbsorptionPlane;
import com.almostrealism.primitives.Pinhole;
import com.almostrealism.primitives.Plane;
import io.almostrealism.relation.Producer;
import org.almostrealism.Ops;
import org.almostrealism.algebra.ImmutableVector;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ProbabilityDistribution;
import org.almostrealism.color.Spectrum;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.Fast;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.physics.PhysicalConstants;
import org.almostrealism.stats.BRDF;
import org.almostrealism.stats.SphericalProbabilityDistribution;
import org.almostrealism.physics.Clock;

import com.almostrealism.stats.UniformHemisphericalDistribution;
import com.almostrealism.geometry.Sphere;
import com.almostrealism.light.PlanarLight;
import org.almostrealism.CodeFeatures;
import org.almostrealism.util.PriorityQueue;
import io.almostrealism.relation.Evaluable;

import static org.almostrealism.Ops.*;

import java.lang.Math;

/**
 * A {@link SpecularAbsorber} is an {@link Absorber} implementation that absorbs photons
 * contained within a three dimensional volume. The photons absorbed are re-emitted based
 * on the law of reflection. There is also optional blur functionality.
 * 
 * @author  Samuel Tepper
 */
public class SpecularAbsorber extends VolumeAbsorber
							implements Absorber, Fast, BRDF, Spectrum,
									PhysicalConstants, CodeFeatures {
	public static double verbose = Math.pow(10.0, -7.0);
	
	private Clock clock;
	private PriorityQueue Queue = new PriorityQueue();
	private Vector P, N, L, resultant;
	
	private SphericalProbabilityDistribution brdf;
	private ProbabilityDistribution spectra;
	private double reflectDepth, absorbDepth;
	private double startwave, range;
	private double delay;
	private double origPosition[];
	
	public static void main(String args[]) {
		double x = 50.0;
		
		
		// Create a SpecularAbsorber and confine it to a sphere.
		SpecularAbsorber b = new SpecularAbsorber();
		//b.setVolume(new Sphere(x / 10.0));
		Plane p = new Plane();
		p.setSurfaceNormal(new ImmutableVector(0.0, -1.0, 0.0));
		p.setOrientation(new double[] {0.0, 0.0, 1.0});
		p.setWidth(x / 2.0);
		p.setHeight(x / 2.0);
		p.setThickness(0.1);
		b.setVolume(p);
		
		
		
		VolumeAbsorber bl = new VolumeAbsorber(new Sphere(x / 8.0), new BlackBody());
		
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(x / 300.0);
		plane.setWidth(300);
		plane.setHeight(300);
		plane.setThickness(0.05);
		plane.setSurfaceNormal(new ImmutableVector(0.0, 0.0, -1.0));
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		Pinhole pinhole = new Pinhole();
		pinhole.setRadius(x / 8.0);
		pinhole.setThickness(0.05);
		pinhole.setSurfaceNormal(new ImmutableVector(0.0, 0.0, -1.0));
		pinhole.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		// Create a light bulb
		PlanarLight l = new PlanarLight();
		l.setWidth(x / 12.0);
		l.setHeight(x / 12.0);
		l.setSurfaceNormal(new Vector(0.0, Math.sqrt(2.0) / -2.0, Math.sqrt(2.0) / 2.0));
		l.setOrientation(new Vector(0.0, Math.sqrt(2.0) / 2.0, Math.sqrt(2.0) / 2.0));
		l.setPower(PlanarLight.wattsToEvMsec * 0.01);
		l.setLightPropagation(false);
		
		// Add SpecularAbsorber and light bulb to absorber set
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(2.0 * x);
		a.addAbsorber(b, Ops.ops().vector(0.0, -x, 0.0));
		// a.addAbsorber(bl, new double[] {0.0, 0.0, 0.0});
		a.addAbsorber(l, Ops.ops().vector(0.0, 0.0, -x));
		a.addAbsorber(plane, Ops.ops().vector(0.0, 0.0, x + 10.0));
		a.addAbsorber(pinhole, Ops.ops().vector(0.0, 0.0, x));
		
		// Create photon field and set absorber to the absorber set
		// containing the stuff we want to look at...
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		// Create a clock and add the photon field
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Specular Absorber Test");
		frame.getContentPane().add(plane.getDisplay());
		frame.setSize(150, 150);
		frame.setVisible(true);
		
		
		long start = System.currentTimeMillis();
		
		// Run the simulation and print out flux measurements every second
		while (true) {
			c.tick().get().run();
			
			if (Math.random() < SpecularAbsorber.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				try {
					plane.saveImage("specular-sim.jpg");
				} catch (IOException ioe) {
					System.out.println("SpecularAbsorber: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
	}
	
	public SpecularAbsorber() {
		super.absorber = this;
		this.brdf = new UniformHemisphericalDistribution();
	}
	
	public void setAbsorbDelay(double t) { this.delay = t + this.clock.getTime(); }
	
	public boolean absorb(Vector Position, Vector Incoming, double Energy) {
		if (this.volume != null && !this.volume.inside(v(Position))) return false;
		
		if (this.volume != null && this.absorbDepth != 0.0) {
			double in = this.volume.intersect(Position, Incoming.minus());
			
			if (this.absorbDepth > 0.0 && in > this.absorbDepth) {
				if (this.volume.intersect(Position, Incoming) >= this.absorbDepth)
					return false;
			} else if (this.absorbDepth < 0.0 && in < this.absorbDepth) {
				if (this.volume.intersect(Position, Incoming) <= this.absorbDepth)
					return false;
			}
		}
		
		double r = Math.random();
		double tempwave = (HC) / Energy;
		
		if (r < SpecularAbsorber.verbose)
			System.out.println("SpecularAbsorber: " + (tempwave * 1000.0) + " nanometers.");
			
		if (startwave > 0.0) {
			if (tempwave < this.startwave ||
					tempwave > this.startwave + this.range)
				return true;
		}
		
		if (this.spectra != null) {
			if (Math.random() < (1 - this.spectra.getProbability(tempwave))) {
				return true;
			}
		}
		
		if (r < SpecularAbsorber.verbose)
			System.out.println("SpecularAbsorber: Absorbing " +
								Incoming.length() + " " + Energy);
		
		Object data[] = { Position, Incoming, new double[] { Energy }};
		Queue.put(data, this.delay);
		
		return true;
	}

	@Override
	public Producer<Vector> emit() {
		P = (Vector) ((Object[])Queue.peekNext())[0];
		L = (Vector) ((Object[])Queue.next())[1];
		
		if (this.volume != null && this.reflectDepth != 0.0) {
			Vector minusL = L.minus();
			double in = this.volume.intersect(P, minusL);
			
			if (this.reflectDepth > 0.0 && in > this.reflectDepth) {
				if (this.volume.intersect(P, L) >= this.reflectDepth)
					return v(L);
			} else if (this.reflectDepth < 0.0 && in < this.reflectDepth) {
				if (this.volume.intersect(P, L) <= this.reflectDepth)
					return v(L);
			}
			
			L = minusL;
		} else {
			L = L.minus();
		}
		
		N = volume.getNormalAt(v(P)).get().evaluate();
		if (N.dotProduct(L) < 0) N = N.minus();
		return this.brdf.getSample(L.toArray(), N.toArray());
	}

	@Override
	public double getEmitEnergy() {
		// return the energy of the top item
		return ((double[][])Queue.peekNext())[2][0];
	}

	@Override
	public double getNextEmit() {
		return Queue.peek() - this.clock.getTime();
//		if (Queue.peek() - this.clock.getTime() < this.clock.getTickInterval()){
//			return 0.0;
//		}
//		return Integer.MAX_VALUE;
	}

	public Producer<Vector> getEmitPosition() {
		// return position of next queue item
		if (Queue.size() > 0)
			return v((Vector) ((Object[]) Queue.peekNext())[0]);
		else
			return null;
	}
	
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
	public void setAbsorbDepth(double depth) { this.absorbDepth = depth; }
	public double getAbsorbDepth() { return this.absorbDepth; }
	public void setReflectDepth(double depth) { this.reflectDepth = depth; }
	public double getReflectDepth() { return this.reflectDepth; }
	public void setSpectra(ProbabilityDistribution spectra) { this.spectra = spectra; }
	public ProbabilityDistribution getSpectra() { return this.spectra; }
	public void setBRDF(SphericalProbabilityDistribution brdf) { this.brdf = brdf; }
	public SphericalProbabilityDistribution getBRDF() { return this.brdf; }
	public void setColorRangeStart(double s) { this.startwave = s; }
	public double getColorRangeStart() { return this.startwave; }
	public void setColorRangeLength(double l) { this.range = l; }
	public double getColorRangeLength() { return this.range; }
	public double[] getColorRange() { return new double[] {this.startwave, this.range}; }
	
	/**
	 * @param StartWaveLength  Nanometers.
	 * @param range Nanometers.
	 */
	public void setColorRange(double StartWaveLength, double range) {
		this.startwave = StartWaveLength;
		this.range = range;
	}

	public void setOrigPosition(double[] x) { this.origPosition = x; }
}