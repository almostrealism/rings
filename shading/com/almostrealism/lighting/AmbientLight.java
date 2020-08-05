/*
 * Copyright 2020 Michael Murray
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

import io.almostrealism.code.Scope;
import io.almostrealism.code.Variable;
import org.almostrealism.algebra.Triple;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.*;
import org.almostrealism.relation.TripleFunction;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.Producer;
import org.almostrealism.util.StaticProducer;

import java.util.concurrent.Callable;

/**
 * An AmbientLight object represents a light that is applied to all objects in the scene.
 * The color and intensity of the light may by specified, but by default it is white light.
 * 
 * @author  Michael Murray
 */
public class AmbientLight implements Light {
	private double intensity;
	private RGB color;

	private ColorProducer colorProducer = GeneratedColorProducer.fromFunction(this, new TripleFunction<RGB>() {
		@Override
		public RGB operate(Triple triple) {
			return color.multiply(intensity);
		}

		@Override
		public Scope<? extends Variable> getScope(String s) {
			throw new RuntimeException("getScope not implemented");
		}
	});


	/**
	 * Constructs an AmbientLight object with the default intensity and color.
	 */
	public AmbientLight() {
		this.setIntensity(1.0);
		this.setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs an AmbientLight object with the specified intensity and default color.
	 */
	public AmbientLight(double intensity) {
		this.setIntensity(intensity);
		this.setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs an AmbientLight object with the specified intensity and color.
	 */
	public AmbientLight(double intensity, RGB color) {
		this.setIntensity(intensity);
		this.setColor(color);
	}
	
	/**
	 * Sets the intensity of this AmbientLight object.
	 */
	@Override
	public void setIntensity(double intensity) { this.intensity = intensity; }
	
	/**
	 * Sets the color of this AmbientLight object to the color represented by the specified RGB object.
	 */
	@Override
	public void setColor(RGB color) { this.color = color; }
	
	/** Returns the intensity of this AmbientLight object as a double value. */
	@Override
	public double getIntensity() { return this.intensity; }
	
	/** Returns the color of this AmbientLight object as an RGB object. */
	@Override
	public RGB getColor() { return this.color; }
	
	/** Returns the {@link ColorProducer} for this {@link AmbientLight}. */
	public ColorProducer getColorAt(Producer<Vector> point) {
		return GeneratedColorProducer.fromProducer(this,
				new ColorProduct(color, RGBProducer.fromScalar(intensity)));
	}
	
	/** Returns "Ambient Light". */
	@Override
	public String toString() { return "Ambient Light"; }

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * intersection on that surface using the lighting data from the specified AmbientLight
	 * object and returns an RGB object that represents the color of the point. A list of all
	 * other surfaces in the scene must be specified for reflection/shadowing. This list does
	 * not include the specified surface for which the lighting calculations are to be done.
	 */
	public static RGBProducer ambientLightingCalculation(Producer<RGB> surface, AmbientLight light) {
		RGBProducer color = new ColorMultiplier(light.getColor(), light.getIntensity());
		if (surface instanceof ShadableSurface)
			color = new ColorProduct(color, ((ShadableSurface) surface).getColorAt());
		
		return color;
	}
}
