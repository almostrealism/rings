package com.almostrealism.raytracer.engine;

import com.almostrealism.lighting.*;
import com.almostrealism.projection.Intersections;
import com.almostrealism.rayshade.Shadable;
import com.almostrealism.rayshade.ShadableIntersection;
import com.almostrealism.rayshade.ShaderParameters;
import com.almostrealism.raytracer.Settings;
import org.almostrealism.algebra.ContinuousField;
import org.almostrealism.algebra.Ray;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Intersectable;
import org.almostrealism.space.Intersection;
import org.almostrealism.util.ParameterizedFactory;

import java.util.*;
import java.util.concurrent.Callable;

public class LightingEngine {
	private ParameterizedFactory<Ray, ContinuousField> fields;

	public LightingEngine(ParameterizedFactory<Ray, ContinuousField> fields) {
		this.fields = fields;
	}

	/**
	 * Performs intersection and lighting calculations for the specified {@link Ray}, Surfaces,
	 * and {@link Light}s. This method may return null, which should be interpreted as black
	 * (or "nothing").
	 */
	public ColorSum lightingCalculation(Ray r, Iterable<? extends Callable<ColorProducer>> allSurfaces, Light allLights[],
											   RGB fog, double fd, double fr, ShaderParameters p) {
		fields.setParameter(Ray.class, r);
		ContinuousField intersect = fields.construct();

		ColorSum color = new ColorSum();

		// TODO  Figure out what this is for.
		ShadableSurface surface = null;

		if (intersect != null) {
			Callable<ColorProducer> surf = intersect instanceof Intersection ? (Callable<ColorProducer>) ((Intersection) intersect).getSurface() : null;
			List<Callable<ColorProducer>> otherSurf = new ArrayList<Callable<ColorProducer>>();

			for (Callable<ColorProducer> s : allSurfaces) {
				if (surface != s) {
					otherSurf.add(s);
				}
			}

			for (int i = 0; i < allLights.length; i++) {
				// See RayTracingEngine.separateLights method

				Light otherL[] = new Light[allLights.length - 1];

				for (int j = 0; j < i; j++) { otherL[j] = allLights[j]; }
				for (int j = i + 1; j < allLights.length; j++) { otherL[j - 1] = allLights[j]; }

				ColorProducer c = null;

				try {
					if (LegacyRayTracingEngine.castShadows && allLights[i].castShadows &&
							shadowCalculation(intersect.get(0).call().getOrigin(),
									Intersections.filterIntersectables(Arrays.asList(allSurfaces)), allLights[i]))
						return new ColorSum();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				if (allLights[i] instanceof SurfaceLight) {
					try {
						c = lightingCalculation(intersect, intersect.get(0).call().getOrigin(), r.getDirection(),
												surf, otherSurf, ((SurfaceLight) allLights[i]).getSamples(), p);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				} else if (allLights[i] instanceof PointLight) {
					try {
						Vector direction = intersect.get(0).call().getOrigin().subtract(((PointLight) allLights[i]).getLocation());
						DirectionalAmbientLight directionalLight =
								new DirectionalAmbientLight(1.0, allLights[i].getColorAt(intersect.get(0).call().getOrigin()).evaluate(null), direction);

						Vector l = (directionalLight.getDirection().divide(directionalLight.getDirection().length())).minus();

						if (p == null) {
							c = surf instanceof Shadable ? ((Shadable) surf).shade(new ShaderParameters(intersect, l, directionalLight, otherL, otherSurf)) : null;
						} else {
							p.setIntersection(intersect);
							p.setLightDirection(l);
							p.setLight(directionalLight);
							p.setOtherLights(otherL);
							p.setOtherSurfaces(otherSurf);

							c = surf instanceof Shadable ? ((Shadable) surf).shade(p) : null;
						}
					} catch (Exception e) {
						e.printStackTrace();;
						return null;
					}
				} else if (allLights[i] instanceof DirectionalAmbientLight) {
					DirectionalAmbientLight directionalLight = (DirectionalAmbientLight) allLights[i];

					Vector l = (directionalLight.getDirection().divide(
							directionalLight.getDirection().length())).minus();

					if (p == null) {
						c = surf instanceof Shadable ? ((Shadable) surf).shade(new ShaderParameters(intersect, l, directionalLight, otherL, otherSurf)) : null;
					} else {
						p.setIntersection(intersect);
						p.setLightDirection(l);
						p.setLight(directionalLight);
						p.setOtherLights(otherL);
						p.setOtherSurfaces(otherSurf);

						c = surf instanceof Shadable ? ((Shadable) surf).shade(p) : null;
					}
				} else if (allLights[i] instanceof AmbientLight) {
					try {
						c = AmbientLight.ambientLightingCalculation(intersect.get(0).call().getOrigin(),
													r.getDirection(), surf, (AmbientLight) allLights[i]);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				} else {
					c = new RGB(0.0, 0.0, 0.0);
				}

				if (c != null) color.add(c);
			}
		}

		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput)
			Settings.rayEngineOut.println();

		return color;

//		Intersection intersect = RayTracingEngine.closestIntersection(ray, surfaces);
//
//		if (intersect == null) {
//			if (fog != null) {
//				double rd = Math.random();
//
//				if (!RayTracingEngine.useRouletteFogSamples ||
//						rd < fd * RayTracingEngine.dropOffDistance)
//					return fog.multiply(fr + fd * rd);
//			}
//
//			return null;
//		} else {
//			double intersection = intersect.getClosestIntersection();
//
//			Surface surface = intersect.getSurface();
//			Surface otherSurfaces[] = surfaces;
//
//			for (int i = 0; i < surfaces.length; i++) {
//				if (surface == surfaces[i]) {
//					// See separateSurfaces method.
//
//					otherSurfaces = new Surface[surfaces.length - 1];
//
//					for (int j = 0; j < i; j++) { otherSurfaces[j] = surfaces[j]; }
//					for (int j = i + 1; j < surfaces.length; j++) { otherSurfaces[j - 1] = surfaces[j]; }
//				}
//			}
//
//			 if (Math.random() < 0.00001 && RefractionShader.lastRay != null)
//			 	System.out.println("lightingCalculation3: " + RefractionShader.lastRay + " " + ray.getDirection());
//
//			RGB rgb = RayTracingEngine.lightingCalculation(ray.pointAt(intersection), ray.getDirection(), intersect.getSurface(), otherSurfaces, lights, p);
//
//			// System.out.println(fog + " " + fd + " " + intersection);
//
//			if (fog != null && Math.random() < fd * intersection) {
//				if (fr > RayTracingEngine.e) {
//					if (fr >= 1.0) {
//						rgb = (RGB) fog.clone();
//					} else if (rgb != null){
//						rgb.multiplyBy(1.0 - fr);
//						rgb.addTo(fog.multiply(fr));
//					} else {
//						rgb = fog.multiply(fr);
//					}
//				}
//			}
//
//			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
//				Settings.rayEngineOut.println();
//			}
//
//			return rgb;
//		}
	}

	/**
	 * Performs the lighting calculations for the specified surface at the specified point of intersection
	 * on that surface using the lighting data from the specified Light objects and returns an RGB object
	 * that represents the color of the point. A list of all other surfaces in the scene must be specified
	 * for reflection/shadowing. This list does not include the specified surface for which the lighting
	 * calculations are to be done.
	 */
	public static ColorSum lightingCalculation(ContinuousField intersection, Vector point, Vector rayDirection,
											   Callable<ColorProducer> surface, Collection<Callable<ColorProducer>> otherSurfaces,
											   Light lights[], ShaderParameters p) {
		ColorSum color = new ColorSum();

		for(int i = 0; i < lights.length; i++) {
			// See RayTracingEngine.seperateLights method

			Light otherLights[] = new Light[lights.length - 1];

			for (int j = 0; j < i; j++) { otherLights[j] = lights[j]; }
			for (int j = i + 1; j < lights.length; j++) { otherLights[j - 1] = lights[j]; }

			ColorProducer c = lightingCalculation(intersection, point, rayDirection, surface,
					otherSurfaces, lights[i], otherLights, p);
			if (c != null) color.add(c);
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
	public static ColorProducer lightingCalculation(ContinuousField intersection, Vector point, Vector rayDirection,
													Callable<ColorProducer> surface,
													Collection<Callable<ColorProducer>> otherSurfaces, Light light,
													Light otherLights[], ShaderParameters p) {
		List<Callable<ColorProducer>> allSurfaces = new ArrayList<Callable<ColorProducer>>();
		for (Callable<ColorProducer> s : otherSurfaces) allSurfaces.add(s);
		allSurfaces.add(surface);

		if (LegacyRayTracingEngine.castShadows && light.castShadows &&
				shadowCalculation(point, Intersections.filterIntersectables(allSurfaces), light))
			return new RGB(0.0, 0.0, 0.0);

		if (light instanceof SurfaceLight) {
			Light l[] = ((SurfaceLight) light).getSamples();
			return lightingCalculation(intersection, point, rayDirection,
					surface, otherSurfaces, l, p);
		} else if (light instanceof PointLight) {
			return PointLight.pointLightingCalculation(intersection, point,
					rayDirection, surface,
					otherSurfaces, (PointLight) light, otherLights, p);
		} else if (light instanceof DirectionalAmbientLight) {
			return DirectionalAmbientLight.directionalAmbientLightingCalculation(
					intersection, point,
					rayDirection, surface,
					otherSurfaces,
					(DirectionalAmbientLight) light, otherLights, p);
		} else if (light instanceof AmbientLight) {
			return AmbientLight.ambientLightingCalculation(point, rayDirection, surface, (AmbientLight) light);
		} else {
			return new RGB(0.0, 0.0, 0.0);
		}
	}

	/**
	 * Performs the shadow calculations for the specified surfaces at the specified point using the data
	 * from the specified Light object. Returns true if the point has a shadow cast on it.
	 */
	public static <T extends Intersection> boolean shadowCalculation(Vector point, Iterator<Intersectable<T>> surfaces, Light light) {
		double maxDistance = -1.0;
		Vector direction = null;

		if (light instanceof PointLight) {
			direction = ((PointLight) light).getLocation().subtract(point);
			direction = direction.divide(direction.length());
			maxDistance = direction.length();
		} else if (light instanceof DirectionalAmbientLight) {
			direction = ((DirectionalAmbientLight) light).getDirection().minus();
		} else {
			return false;
		}

		Ray shadowRay = new Ray(point, direction);

		T closestIntersectedSurface = Intersections.closestIntersection(shadowRay, surfaces);
		double intersect = 0.0;
		if (closestIntersectedSurface != null)
			intersect = closestIntersectedSurface.getClosestIntersection();

		if (closestIntersectedSurface == null || intersect <= Intersection.e || (maxDistance >= 0.0 && intersect > maxDistance)) {
			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
				Settings.rayEngineOut.print(" False }");
			}

			return false;
		} else {
			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
				Settings.rayEngineOut.print(" True }");
			}

			return true;
		}
	}

	/**
	 * Reflects the specified {@link Vector} across the normal vector represented by the
	 * second specified {@link Vector} and returns the result.
	 */
	public static Vector reflect(Vector vector, Vector normal) {
		vector = vector.minus();
		return vector.subtract(normal.multiply(2 * (vector.dotProduct(normal) / normal.lengthSq())));
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

		if (refracted.subtract(vector).length() > 0.001) System.out.println("!!");

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
