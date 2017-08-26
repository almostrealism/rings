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

package com.almostrealism.photon.light;

import org.almostrealism.algebra.VectorMath;
import org.almostrealism.stats.ProbabilityDistribution;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.Clock;
import com.almostrealism.photon.Volume;
import com.almostrealism.photon.distribution.SphericalProbabilityDistribution;
import com.almostrealism.photon.util.PhysicalConstants;
import com.almostrealism.photon.util.Transparent;

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
