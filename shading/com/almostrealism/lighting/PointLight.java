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

import java.util.Collection;
import java.util.concurrent.Callable;

import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Triple;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.space.ShadableSurface;

import com.almostrealism.raytracer.engine.RayTracedScene;

/**
 * An {@link PointLight} object represents a light which has its source at a point in the scene.
 * The color and intensity of the light may by specified, but by default it is white light.
 * Also, coefficients for distance attenuation may be specified also, but by default are 0.0, 0.0,
 * and 1.0 (no attenuation).
 */
public class PointLight implements Light {
	private double intensity;
	private RGB color;

	private Vector location;

	private double da, db, dc;

	/** Constructs a PointLight object with the default intensity and color at the origin. */
	public PointLight() {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/** Constructs a {@link PointLight} with the specified location and default intensity and color. */
	public PointLight(Vector location) {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/** Constructs a PointLight object with the specified intensity and default color at the origin. */
	public PointLight(double intensity) {
		this.setIntensity(intensity);
		this.setColor(new RGB(1.0, 1.0, 1.0));
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/** Constructs a PointLight object with the specified intensity and color at the origin. */
	public PointLight(double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/** Constructs a PointLight object with the specified location, intensity, and color. */
	public PointLight(Vector location, double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
		
		this.setLocation(location);
		
		this.setAttenuationCoefficients(0.0, 0.0, 1.0);
	}
	
	/**
	 * Sets the intensity of this PointLight object to the specified double value.
	 */
	public void setIntensity(double intensity) { this.intensity = intensity; }
	
	/**
	 * Sets the color of this PointLight object to the color represented by the specified RGB object.
	 */
	public void setColor(RGB color) { this.color = color; }
	
	/**
	 * Sets the location of this PointLight object to the location represented by the specified Vector object.
	 */
	public void setLocation(Vector location) {
		this.location = location;
	}
	
	/**
	 * Sets the coefficients a, b, and c for the quadratic function used for distance attenuation
	 * of the light represented by this PointLight object to the specified double values.
	 */
	public void setAttenuationCoefficients(double a, double b, double c) {
		this.da = a;
		this.db = b;
		this.dc = c;
	}
	
	/**
	 * Sets the coefficients a, b, and c for the quadratic function used for distance attenuation
	 * of the light represented by this PointLight object to the specified double values.
	 */
	public void setAttenuationCoefficients(double a[]) {
		this.da = a[0];
		this.db = a[1];
		this.dc = a[2];
	}
	
	/**
	 * Sets the coefficients a, b, and c for the quadratic function used for distance attenuation
	 * of the light represented by this PointLight object to the specified double values.
	 */
	public void setAttenuationCoefficients(Triple a) {
		this.da = a.getA();
		this.db = a.getB();
		this.dc = a.getC();
	}
	
	/** Returns the intensity of this PointLight object as a double value. */
	public double getIntensity() { return this.intensity; }
	
	/** Returns the color of this PointLight object as an RGB object. */
	public RGB getColor() { return this.color; }
	
	/**
	 * Returns the color of the light represented by this PointLight object at the
	 * specified point as an RGB object.
	 */
	public ColorProducer getColorAt(Vector point) {
		double d = point.subtract(this.location).lengthSq();
		
		RGB color = this.getColor().multiply(this.getIntensity());
		color.divideBy(da * d + db * Math.sqrt(d) + dc);
		
		return color;
	}
	
	/** Returns the location of this PointLight object as a Vector object. */
	public Vector getLocation() { return this.location; }
	
	/**
	 * Returns the coefficients a, b, and c for the quadratic function used for distance
	 * attenuation of the light represented by this PointLight object as an array of
	 * double values.
	 */
	public double[] getAttenuationCoefficients() {
		double d[] = {this.da, this.db, this.dc};
		
		return d;
	}
	
	/** Returns "Point Light". */
	public String toString() { return "Point Light"; }

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * intersection on that surface using the lighting data from the specified {@link PointLight}
	 * object and returns an {@link RGB} object that represents the color of the point.
	 * A list of all other surfaces in the scene must be specified for reflection/shadowing.
	 * This list does not include the specified surface for which the lighting calculations
	 * are to be done. If the premultiplyIntensity option is set to true the color of the
	 * point light will be adjusted by the intensity of the light and the intensity will
	 * then be set to 1.0. If the premultiplyIntensity option is set to false, the color will
	 * be left unattenuated and the shaders will be responsible for adjusting the color
	 * based on intensity.
	 */
	public static ColorProducer pointLightingCalculation(ContinuousField intersection, Vector point,
											Vector rayDirection, Callable<ColorProducer> surface,
											Collection<Callable<ColorProducer>> otherSurfaces, PointLight light,
											Light otherLights[], ShaderContext p) {
		Vector direction = point.subtract(light.getLocation());
		DirectionalAmbientLight dLight = null;
		
		if (RayTracedScene.premultiplyIntensity) {
			dLight = new DirectionalAmbientLight(1.0, light.getColorAt(point).evaluate(null), direction);
		} else {
			double in = light.getIntensity();
			light.setIntensity(1.0);
			dLight = new DirectionalAmbientLight(in, light.getColorAt(point).evaluate(null), direction);
			light.setIntensity(in);
		}
		
		return DirectionalAmbientLight.directionalAmbientLightingCalculation(
											intersection, point,
											rayDirection, surface,
											otherSurfaces, dLight, otherLights, p);
	}
}