/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.raytrace;

import com.almostrealism.lighting.AmbientLight;
import com.almostrealism.lighting.DirectionalAmbientLight;
import com.almostrealism.lighting.PointLight;
import com.almostrealism.lighting.SurfaceLight;
import io.almostrealism.code.ArgumentMap;
import io.almostrealism.code.ScopeInputManager;
import io.almostrealism.code.ScopeLifecycle;
import org.almostrealism.geometry.ContinuousField;
import org.almostrealism.geometry.Intersectable;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.Light;
import org.almostrealism.color.RGB;
import org.almostrealism.color.computations.ColorProduct;
import org.almostrealism.color.computations.RGBAdd;
import org.almostrealism.color.computations.RGBBlack;
import org.almostrealism.color.computations.RGBWhite;
import org.almostrealism.color.Shadable;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Curve;
import org.almostrealism.geometry.Ray;
import org.almostrealism.geometry.computations.RayOrigin;
import org.almostrealism.graph.PathElement;
import org.almostrealism.hardware.AcceleratedComputationEvaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.geometry.ShadableIntersection;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.geometry.DimensionAware;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.ProducerWithRank;
import static org.almostrealism.util.Ops.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO  T must extend ShadableIntersection so that distance can be used as the rank
public class LightingEngine<T extends ContinuousField> extends AcceleratedComputationEvaluable<RGB> implements ProducerWithRank<RGB, Scalar>, PathElement<Ray, RGB>, DimensionAware, CodeFeatures {
	public static boolean enableShadows = false;

	private T intersections;
	private Curve<RGB> surface;
	private Producer<Scalar> distance;

	public LightingEngine(T intersections,
						  Curve<RGB> surface,
						  Collection<Curve<RGB>> otherSurfaces,
						  Light light, Iterable<Light> otherLights, ShaderContext p) {
		super(new ColorProduct(shadowAndShade(intersections, surface, otherSurfaces, light, otherLights, p)));
		this.intersections = intersections;
		this.surface = surface;
		this.distance = ((ShadableIntersection) intersections).getDistance();
	}

	protected static Supplier[] shadowAndShade(ContinuousField intersections,
											   Curve<RGB> surface,
											   Collection<Curve<RGB>> otherSurfaces,
											   Light light, Iterable<Light> otherLights, ShaderContext p) {
		Supplier<Evaluable<? extends RGB>> shadow, shade;

		List<Intersectable> allSurfaces = new ArrayList<>();
		if (surface instanceof Intersectable) allSurfaces.add((Intersectable) surface);

		if (enableShadows && light.castShadows) {
			shadow = new ShadowMask(light, allSurfaces, new RayOrigin(intersections.get(0)).get());
		} else {
			shadow = RGBWhite.getInstance();
		}

		ShaderContext context = p.clone();
		context.setLight(light);
		context.setIntersection(intersections);
		context.setOtherLights(otherLights);
		context.setOtherSurfaces(otherSurfaces);

		if (light instanceof SurfaceLight) {
			shade = lightingCalculation(intersections, new RayOrigin(intersections.get(0)).get(),
										surface, otherSurfaces,
										((SurfaceLight) light).getSamples(), p);
		} else if (light instanceof PointLight) {
			shade = surface instanceof Shadable ? ((PointLight) light).forShadable((Shadable) surface, intersections.get(0), context) : null;
		} else if (light instanceof DirectionalAmbientLight) {
			DirectionalAmbientLight directionalLight = (DirectionalAmbientLight) light;

			Vector l = (directionalLight.getDirection().divide(
					directionalLight.getDirection().length())).minus();

			context.setLightDirection(ops().v(l));

			shade = surface instanceof Shadable ? ((Shadable) surface).shade(context) : null;
		} else if (light instanceof AmbientLight) {
			shade = AmbientLight.ambientLightingCalculation(surface, (AmbientLight) light,
						new RayOrigin(intersections.get(0)));
		} else {
			shade = RGBBlack.getInstance();
		}

		return new Supplier[] { shadow, shade };
	}

	@Override
	public void setDimensions(int width, int height, int ssw, int ssh) {
		if (intersections instanceof DimensionAware) {
			((DimensionAware) intersections).setDimensions(width, height, ssw, ssh);
		}
	}

	@Override
	public Iterable<Producer<Ray>> getDependencies() { return intersections; }

	public Curve<RGB> getSurface() { return surface; }

	@Override
	public Producer<RGB> getProducer() { return this; }

	@Override
	public Producer<Scalar> getRank() { return distance; }

	@Override
	public Evaluable<RGB> get() { return this; }

	@Override
	public void prepareArguments(ArgumentMap map) {
		super.prepareArguments(map);
		ScopeLifecycle.prepareArguments(Stream.of(getRank()), map);
	}

	@Override
	public void prepareScope(ScopeInputManager manager) {
		super.prepareScope(manager);
		if (getArgumentVariables() != null) return;

		ScopeLifecycle.prepareScope(Stream.of(getRank()), manager);
	}

	@Override
	public void compact() {
		super.compact();
		getRank().compact();

		System.out.println("Compacted LightingEngine");
	}

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of intersection
	 * on that surface using the lighting data from the specified Light objects and returns an RGB object
	 * that represents the color of the point. A list of all other surfaces in the scene must be specified
	 * for reflection/shadowing. This list does not include the specified surface for which the lighting
	 * calculations are to be done.
	 */
	public static Supplier<Evaluable<? extends RGB>> lightingCalculation(ContinuousField intersection, Evaluable<Vector> point,
																		 Curve<RGB> surface, Iterable<Curve<RGB>> otherSurfaces,
																		 Light lights[], ShaderContext p) {
		Supplier<Evaluable<? extends RGB>> color = null;

		for (int i = 0; i < lights.length; i++) {
			// See RayTracingEngine.seperateLights method

			Light otherLights[] = new Light[lights.length - 1];

			for (int j = 0; j < i; j++) { otherLights[j] = lights[j]; }
			for (int j = i + 1; j < lights.length; j++) { otherLights[j - 1] = lights[j]; }

			Supplier<Evaluable<? extends RGB>> c = lightingCalculation(intersection, point, surface,
										otherSurfaces, lights[i], otherLights, p);
			if (c != null) {
				if (color == null) {
					color = c;
				} else {
					Supplier<Evaluable<? extends RGB>> fc = color;
					color = () -> new RGBAdd(fc, c);
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
	@Deprecated
	public static Supplier<Evaluable<? extends RGB>> lightingCalculation(ContinuousField intersection, Evaluable<Vector> point,
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
			return RGBBlack.getInstance();
		}
	}

	/**
	 * Refracts the specified Vector object based on the specified normal vector and 2 specified indices of refraction.
	 *
	 * @param vector  A Vector object representing a unit vector in the direction of the incident ray
	 * @param normal  A Vector object representing a unit vector that is normal to the surface refracting the ray
	 * @param ni  A double value representing the index of refraction of the incident medium
	 * @param nr  A double value representing the index of refraction of the refracting medium
	 *
	 * @deprecated
	 */
	@Deprecated
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
}