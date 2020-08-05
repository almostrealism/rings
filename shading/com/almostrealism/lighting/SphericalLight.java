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

package com.almostrealism.lighting;

import org.almostrealism.algebra.TransformMatrix;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.Light;

import com.almostrealism.primitives.Sphere;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RGBProducer;
import org.almostrealism.util.AdaptProducer;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

/**
 * A SphericalLight object provides PointLight samples that are randomly distributed
 * across the surface of a sphere.
 * 
 * @author  Michael Murray
 */
public class SphericalLight extends Sphere implements SurfaceLight {
  private double intensity, atta, attb, attc;
  
  private int samples;

	/** Constructs a new {@link SphericalLight}. */
	public SphericalLight() {
		super(new Vector(0.0, 0.0, 0.0), 0.0);
		
		this.intensity = 1.0;
		this.samples = 1;
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	 * Constructs a new {@link SphericalLight}.
	 * 
	 * @param location  Location for sphere.
	 * @param radius  Radius of sphere.
	 */
	public SphericalLight(Vector location, double radius) {
		super(location, radius);
		
		this.intensity = 1.0;
		this.samples = 1;
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}

	@Override
	public Producer<RGB> getColorAt(Producer<Vector> point) {
		return new AdaptProducer<>(getColorAt(), point);
	}

	/**
	 * Sets the number of samples to use for this SphericalLight object.
	 * 
	 * @param samples
	 */
	public void setSampleCount(int samples) { this.samples = samples; }
	
	/**
	 * @return  The number of samples to use for this SphericalLight object.
	 */
	public int getSampleCount() { return this.samples; }
	
	/**
	 * @see com.almostrealism.lighting.SurfaceLight#getSamples(int)
	 */
	public Light[] getSamples(int total) {
		PointLight l[] = new PointLight[total];
		
		double in = this.intensity / total;
		
		for (int i = 0; i < total; i++) {
			double r = super.getSize();
			double u = Math.random() * 2.0 * Math.PI;
			double v = Math.random() * 2.0 * Math.PI;
			
			double x = r * Math.sin(u) * Math.cos(v);
			double y = r * Math.sin(u) * Math.sin(v);
			double z = r * Math.cos(u);
			
			Vector p = new Vector(x, y, z);
			
			super.getTransform(true).transform(p, TransformMatrix.TRANSFORM_AS_LOCATION);

			// TODO  This should pass along the ColorProucer directly rather than evaluating it
			l[i] = new PointLight(p, in, getColorAt(StaticProducer.of(p)).evaluate(new Object[0]));
			l[i].setAttenuationCoefficients(this.atta, this.attb, this.attc);
		}
		
		return l;
	}
	
	/** @see com.almostrealism.lighting.SurfaceLight#getSamples() */
	@Override
	public Light[] getSamples() { return this.getSamples(this.samples); }

	@Deprecated
	public RGBProducer getColorAt(Vector p) { return getColorAt().evaluate(new Object[] { p }); }

	/** @see org.almostrealism.color.Light#setIntensity(double) */
	@Override
	public void setIntensity(double intensity) { this.intensity = intensity; }

	/** @see org.almostrealism.color.Light#getIntensity() */
	@Override
	public double getIntensity() { return this.intensity; }
	
	/** Sets the attenuation coefficients to be used when light samples are created. */
	public void setAttenuationCoefficients(double a, double b, double c) {
		this.atta = a;
		this.attb = b;
		this.attc = c;
	}
	
	/** @return  An array containing the attenuation coefficients used when light samples are created. */
	public double[] getAttenuationCoefficients() { return new double[] { this.atta, this.attb, this.attc }; }
	
	/** @see org.almostrealism.algebra.ParticleGroup#getParticleVertices() */
	public double[][] getParticleVertices() { return new double[0][0]; }
	
	/** @return  "Spherical Light". */
	public String toString() { return "Spherical Light"; }
}
