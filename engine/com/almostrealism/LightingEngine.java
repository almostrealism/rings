/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism;

import com.almostrealism.lighting.*;
import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Intersectable;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.RGBAdd;
import org.almostrealism.color.computations.RGBMultiply;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.color.Shadable;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.algebra.computations.RayOrigin;
import org.almostrealism.graph.PathElement;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.Producer;
import org.almostrealism.util.ProducerWithRank;
import org.almostrealism.util.StaticProducer;

import java.util.*;

// TODO  T must extend ShadableIntersection so that distance can be used as the rank
public class LightingEngine<T extends ContinuousField> extends ProducerWithRank<RGB> implements PathElement<Ray, RGB> {
	private T intersections;
	private Curve<RGB> surface;
	private Collection<Curve<RGB>> otherSurfaces;
	private List<Intersectable> allSurfaces;
	private Light light;
	private Iterable<Light> otherLights;
	private ShaderContext p;

	private Producer<RGB> shadow;
	private Producer<RGB> shade;

	public LightingEngine(T intersections,
						  Curve<RGB> surface,
						  Collection<Curve<RGB>> otherSurfaces,
						  Light light, Iterable<Light> otherLights, ShaderContext p) {
		super(((ShadableIntersection) intersections).getDistance());
		this.intersections = intersections;
		this.surface = surface;
		this.otherSurfaces = otherSurfaces;
		this.light = light;
		this.otherLights = otherLights;
		this.p = p;

		allSurfaces = new ArrayList<>();
		if (surface instanceof Intersectable) allSurfaces.add((Intersectable) surface);
		for (Curve pr : otherSurfaces) if (pr instanceof Intersectable) allSurfaces.add((Intersectable) pr);

		init();
	}

	protected void init() {
		if (LegacyRayTracingEngine.castShadows && light.castShadows) {
			shadow = new ShadowMask(light, allSurfaces, new RayOrigin(intersections.get(0)));
		} else {
			shadow = RGBWhite.getInstance();
		}

		ShaderContext context = p.clone();
		context.setLight(light);
		context.setIntersection(intersections);
		context.setOtherLights(otherLights);
		context.setOtherSurfaces(otherSurfaces);

		if (light instanceof SurfaceLight) {
			try {
				shade = lightingCalculation(intersections, new RayOrigin(intersections.get(0)),
											surface, otherSurfaces,
											((SurfaceLight) light).getSamples(), p);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (light instanceof PointLight) {
			shade = surface instanceof Shadable ? ((PointLight) light).forShadable((Shadable) surface, intersections.get(0), context) : null;
		} else if (light instanceof DirectionalAmbientLight) {
			DirectionalAmbientLight directionalLight = (DirectionalAmbientLight) light;

			Vector l = (directionalLight.getDirection().divide(
					directionalLight.getDirection().length())).minus();

			context.setLightDirection(StaticProducer.of(l));

			shade = surface instanceof Shadable ? ((Shadable) surface).shade(context) : null;
		} else if (light instanceof AmbientLight) {
			shade = AmbientLight.ambientLightingCalculation(surface, (AmbientLight) light, new RayOrigin(intersections.get(0)));
		} else {
			shade = new RGB(0.0, 0.0, 0.0);
		}
	}

	@Override
	public Iterable<Producer<Ray>> getDependencies() {
		return intersections;
	}

	public Curve<RGB> getSurface() { return surface; }

	/**
	 * Performs intersection and lighting calculations for the specified {@link Ray}, Surfaces,
	 * and {@link Light}s. This method may return null, which should be interpreted as black
	 * (or "nothing").
	 */
	public RGB evaluate(Object args[]) {
		if (shade == null) return new RGB(0.0, 0.0, 0.0);

		return new RGBMultiply(shadow, shade).evaluate(args);
	}

	@Override
	public void compact() {
		shadow.compact();
		shade.compact();

		System.out.println("Compacted LightingEngine");
	}

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of intersection
	 * on that surface using the lighting data from the specified Light objects and returns an RGB object
	 * that represents the color of the point. A list of all other surfaces in the scene must be specified
	 * for reflection/shadowing. This list does not include the specified surface for which the lighting
	 * calculations are to be done.
	 */
	public static Producer<RGB> lightingCalculation(ContinuousField intersection, Producer<Vector> point,
													Curve<RGB> surface, Iterable<Curve<RGB>> otherSurfaces,
											   		Light lights[], ShaderContext p) {
		Producer<RGB> color = null;

		for (int i = 0; i < lights.length; i++) {
			// See RayTracingEngine.seperateLights method

			Light otherLights[] = new Light[lights.length - 1];

			for (int j = 0; j < i; j++) { otherLights[j] = lights[j]; }
			for (int j = i + 1; j < lights.length; j++) { otherLights[j - 1] = lights[j]; }

			Producer<RGB> c = lightingCalculation(intersection, point, surface,
										otherSurfaces, lights[i], otherLights, p);
			if (c != null) {
				if (color == null) {
					color = c;
				} else {
					color = new RGBAdd(color, c);
				}
			}
		}

		return color;
	}

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * intersection on that surface using the lighting data from the specified Light object
	 * and returns an RGB object that represents the color of the point. A list of all other
	 * surfaces in the scene must be specified for reflection/shadowing. This list does not
	 * include the specified surface for which the lighting calculations are to be done.
	 */
	public static Producer<RGB> lightingCalculation(ContinuousField intersection, Producer<Vector> point,
													Curve<RGB> surface,
													Iterable<Curve<RGB>> otherSurfaces, Light light,
													Light otherLights[], ShaderContext p) {
		List<Curve<RGB>> allSurfaces = new ArrayList<>();
		for (Curve<RGB> s : otherSurfaces) allSurfaces.add(s);
		allSurfaces.add(surface);

		if (light instanceof SurfaceLight) {
			Light l[] = ((SurfaceLight) light).getSamples();
			return lightingCalculation(intersection, point,
					surface, otherSurfaces, l, p);
		} else if (light instanceof PointLight) {
			throw new IllegalArgumentException("Migrated elsewhere");
		} else if (light instanceof DirectionalAmbientLight) {
			throw new IllegalArgumentException("Migrated elsewhere");
		} else if (light instanceof AmbientLight) {
			throw new IllegalArgumentException("Migrated elsewhere");
		} else {
			return new RGB(0.0, 0.0, 0.0);
		}
	}

	/**
	 * Refracts the specified Vector object based on the specified normal vector and 2 specified indices of refraction.
	 *
	 * @param vector  A Vector object representing a unit vector in the direction of the incident ray
	 * @param normal  A Vector object representing a unit vector that is normal to the surface refracting the ray
	 * @param ni  A double value representing the index of refraction of the incident medium
	 * @param nr  A double value representing the index of refraction of the refracting medium
	 */
	public static Vector refract(Vector vector, Vector normal, double ni, double nr, boolean v) {
		if (v) System.out.println("Vector = " + vector);

		vector = vector.minus();

		double p = -vector.dotProduct(normal);
		double r = ni / nr;

		if (v) System.out.println("p = " + p + " r = " + r);

		vector = vector.minus();
		if (vector.dotProduct(normal) < 0) {
			if (v) System.out.println("LALA");
			normal = normal.minus();
			p = -p;
		}
		vector = vector.minus();

		double s = Math.sqrt(1.0 - r * r * (1.0 - p * p));

		if (v) System.out.println("s = " + s);

		Vector refracted = vector.multiply(r);

		if (v) System.out.println(refracted);

		//	if (p >= 0.0) {
		refracted.addTo(normal.multiply((p * r) - s));
		//	} else {
		//		refracted.addTo(normal.multiply((p * r) - s));
		//	}

		if (v) System.out.println(refracted);

		// Vector refracted = ((vector.subtract(normal.multiply(p))).multiply(r)).subtract(normal.multiply(s));

//		if (refracted.subtract(vector).length() > 0.001) System.out.println("!!"); TODO

		return refracted.minus();
	}

//	public static double brdf(Vector ld, Vector vd, Vector n, double nv, double nu, double r) {
//		ld = ld.divide(ld.length());
//		vd = vd.divide(vd.length());
//		n = n.divide(n.length());
//
//		Vector h = ld.add(vd);
//		h = h.divide(h.length());
//
//		Vector v = null;
//
//		if (Math.abs(h.getX()) < Math.abs(h.getY()) && Math.abs(h.getX()) < Math.abs(h.getZ()))
//			v = new Vector(0.0, h.getZ(), -h.getY());
//		else if (Math.abs(h.getY()) < Math.abs(h.getZ()))
//			v = new Vector(h.getZ(), 0.0, -h.getX());
//		else
//			v = new Vector(h.getY(), -h.getX(), 0.0);
//
//		v = v.divide(v.length());
//
//		Vector u = v.crossProduct(h);
//
//		double hu = h.dotProduct(u);
//		double hv = h.dotProduct(v);
//		double hn = h.dotProduct(n);
//		double hk = h.dotProduct(ld);
//
//		//System.out.println("hk = " + hk);
//
//		double a = Math.sqrt((nu + 1.0) * (nv + 1.0)) / (8.0 * Math.PI);
//		double b = Math.pow(hn, (nu * hu * hu + nv * hv * hv) / ( 1.0 - hn * hn));
//		b = b / (hk * Math.max(n.dotProduct(ld), n.dotProduct(vd)));
//
//		double value =  a * b;
//
//		//System.out.println("a = " + a);
//		//System.out.println("b = " + b);
//		//System.out.println("BRDF =  " + value);
//
//		return value;
//	}

	/**
	 * Removes the Light at the specified index from the specified Light
	 * array and returns the new array.
	 */
	public static Light[] separateLights(int index, Light allLights[]) {
		Light otherLights[] = new Light[allLights.length - 1];

		for (int i = 0; i < index; i++) { otherLights[i] = allLights[i]; }
		for (int i = index + 1; i < allLights.length; i++) { otherLights[i - 1] = allLights[i]; }

		return otherLights;
	}

	/**
	 * Removes the Surface at the specified index from the specified Surface
	 * array and returns the new array.
	 */
	public static ShadableSurface[] separateSurfaces(int index, ShadableSurface allSurfaces[]) {
		ShadableSurface otherSurfaces[] = new ShadableSurface[allSurfaces.length - 1];

		for (int i = 0; i < index; i++) { otherSurfaces[i] = allSurfaces[i]; }
		for (int i = index + 1; i < allSurfaces.length; i++) { otherSurfaces[i - 1] = allSurfaces[i]; }

		return otherSurfaces;
	}
}
