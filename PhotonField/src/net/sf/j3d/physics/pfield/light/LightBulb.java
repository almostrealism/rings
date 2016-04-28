/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.light;

import net.sf.j3d.physics.pfield.Absorber;
import net.sf.j3d.physics.pfield.Clock;
import net.sf.j3d.physics.pfield.Volume;
import net.sf.j3d.physics.pfield.distribution.SphericalProbabilityDistribution;
import net.sf.j3d.physics.pfield.util.PhysicalConstants;
import net.sf.j3d.physics.pfield.util.ProbabilityDistribution;
import net.sf.j3d.physics.pfield.util.Transparent;
import net.sf.j3d.physics.pfield.util.VectorMath;

/**
 * A LightBulb emits photons with wavelengths between 380 nanometers
 * and 780 nanometers.
 * 
 * @author  Mike Murray
 */
public class LightBulb implements Volume, Absorber, Transparent, PhysicalConstants {
	protected Clock clock;
	
	protected double power, delta, last;
	protected ProbabilityDistribution spectra;
	protected SphericalProbabilityDistribution brdf;
	
	private double specEnd = H * C / 0.380;
	private double specStart = H * C / 0.780;
	protected double specAvg = (specStart + specEnd) / 2.0;
	
	/**
	 * @param p  Power rating of this light bulb in eV/msec. Watts can be converted
	 *           to this measurement by multiplying by LightBulb.wattsToEvMsec.
	 */
	public void setPower(double p) {
		this.power = p;
		this.delta = this.specAvg / this.power;
	}
	
	/**
	 * @return  Power rating of this light bulb in eV/msec. This can be converted to watts
	 *          by multiplying by LightBulb.evMsecToWatts.
	 */
	public double getPower() { return this.power; }
	
	/**
	 * Returns false. A LightBulb does not absorb photons.
	 */
	public boolean absorb(double[] x, double[] p, double energy) { return false; }
	
	/**
	 * Returns a uniform spherical random vector.
	 */
	public double[] emit() {
		this.last += this.delta;
		return VectorMath.uniformSphericalRandom();
	}

	/**
	 * Returns a random energy value in the visible spectrum.
	 */
	public double getEmitEnergy() {
		if (this.spectra == null)
			return this.specStart + Math.random() * (this.specEnd - this.specStart);
		else
			return this.spectra.getSample(Math.random());
	}
	
	/**
	 * Time in microseconds until the next photon should be emitted. The frequency
	 * of photon emission is related to the power of the LightBulb.
	 */
	public double getNextEmit() {
		return this.last + this.delta - this.clock.getTime();
	}
	
	/**
	 * Returns the location of this LightBulb.
	 */
	public double[] getEmitPosition() { return new double[] {0.0, 0.0, 0.0}; }
	
	public void setSpectra(ProbabilityDistribution spectra) { this.spectra = spectra; }
	public ProbabilityDistribution getSpectra() { return this.spectra; }
	
	public void setBRDF(SphericalProbabilityDistribution brdf) { this.brdf = brdf; }
	public SphericalProbabilityDistribution getBRDF() { return this.brdf; }
	
	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }

	public double[] getNormal(double[] x) { return null; }
	public boolean inside(double[] x) { return false; }
	public double intersect(double[] p, double[] d) { return Double.MAX_VALUE - 1.0; }

	public double[] getSpatialCoords(double uv[]) { return new double[3]; }
	public double[] getSurfaceCoords(double[] xyz) { return new double[2]; }
}
