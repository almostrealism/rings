/*
 * Copyright (C) 2004-16  Michael Murray
 * 
 * All rights reserved
 */

// TODO  Make this class use fewer method calls for calculation.
// TODO  Add iterative sampling at increasing resolutions.
// TODO  Add support for brightness histograms (randomly sample pixels).

package com.almostrealism.raytracer.engine;

import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.lighting.AmbientLight;
import com.almostrealism.raytracer.lighting.DirectionalAmbientLight;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.lighting.PointLight;
import com.almostrealism.raytracer.lighting.SurfaceLight;
import com.almostrealism.raytracer.shaders.ShaderParameters;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.run.Settings;
import net.sf.j3d.ui.displays.ProgressDisplay;
import net.sf.j3d.ui.event.ProgressMonitor;


/**
 * The RayTracingEngine class provides static methods for rendering scenes.
 * 
 * @author Mike Murray
 */
public class RayTracingEngine {
  /**
   * Controls wether or not shadow casting will be done during rendering.
   * By default set to true.
   */
  public static boolean castShadows = true;
  
  /**
   * Controls wether the color of a point light source will be adjusted based on the
   * intensity of the point light or wether this will be left up to the shader.
   * By default set to true.
   */
  public static boolean premultiplyIntensity = true;
  
  /**
   * This value will be set to true when a render method starts
   * and false when all render methods end.
   */
  public static boolean inProgress = false;
  
  private static int inProgressCount = 0;
  
  /**
   * Controls the method for rendering fog in areas where no object is in view.
   * If set to true, the fog will be mixed in with probability equal to the fog density
   * times the drop off distance. Otherwise, the background color will be the fog color
   * times the fog ratio.
   */
  public static boolean useRouletteFogSamples = false;
  
  public static double dropOffDistance = 10.0;
  
  public static RGB black = new RGB(0.0, 0.0, 0.0);
  
  /** A very small value (0.00000001) that is used in '>=' and '<=' operations to account for computational errors. */
  public static final double e = 0.00000001;

	/**
	 * Computes all intersection and lighting calculations required to produce an image of the specified width and height
	 * that is a rendering of the specified Scene object and returns the image as an array of RGB objects.
	 * The image is anti-aliased using the specified supersampling width (ssWidth) and height (ssHeight).
	 * and progress is reported to the specified ProgressMonitor object.
	 */
	public static RGB[][] render(Scene scene, int width, int height,
								int ssWidth, int ssHeight,
								ProgressMonitor monitor) {
		RenderParameters p = new RenderParameters(0, 0, width, height, width, height, ssWidth, ssHeight);
		return RayTracingEngine.render(scene.getSurfaces(), scene.getCamera(), scene.getLights(), p, monitor);
	}
	
	public static RGB[][] render (Scene scene, int x, int y, int dx, int dy,
								int width, int height, int ssWidth, int ssHeight,
								ProgressMonitor monitor) {
		RenderParameters p = new RenderParameters(x, y, dx, dy, width, height, ssWidth, ssHeight);
		return RayTracingEngine.render(scene.getSurfaces(), scene.getCamera(), scene.getLights(), p, monitor);
	}
	
	/**
	 * Renders the specified scene.
	 * 
	 * @param scene  Scene object to render.
	 * @param p  RenderParamters object to use.
	 * @param prog  ProgressMonitor to use.
	 * @return  Rendered image data.
	 */
	public static RGB[][] render(Scene scene, RenderParameters p, ProgressDisplay prog) {
		return RayTracingEngine.render(scene.getSurfaces(), scene.getCamera(), scene.getLights(),
									p, prog);
	}
	
	/**
	 *
	 * Computes all intersection and lighting calculations required to produce an image of the specified width and height
	 * that is a rendering of the specified set of Surface objects using the data from the specified Camera and Light object.
	 * The image is anti-aliased using the specified supersampling width (ssWidth) and height (ssHeight)
	 * and progress is reported to the specified ProgressMonitor object. The image is returned as an array of RGB objects.
	 * 
	 * @param surfaces  Surface objects in scene.
	 * @param camera  Camera object for scene.
	 * @param lights  Light objects in scene.
	 * @param x  X coordinate of upper left corner of image.
	 * @param y  Y coordinate of upper left corner of image.
	 * @param dx  Width of image.
	 * @param dy  Height of image.
	 * @param width  Width of total image.
	 * @param height  Height of total image.
	 * @param ssWidth  Supersample width.
	 * @param ssHeight  Supersample height.
	 * @param monitor  ProgressMonitor instance to use.
	 * @return  Image data.
	 */
	public static RGB[][] render(Surface surfaces[], Camera camera, Light lights[], RenderParameters p, ProgressMonitor monitor) {
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.println("Entering RayTracingEngine (" + p.width + " X " + p.ssWidth + ", " + p.height + " X " + p.ssHeight + ") : " + surfaces.length + " Surfaces");
			Settings.rayEngineOut.println("Camera: " + camera.toString());
		}
		
		RayTracingEngine.inProgressCount++;
		RayTracingEngine.inProgress = true;
		
		RGB image[][] = new RGB[p.dx][p.dy];
		
		i: for(int i = p.x; i < (p.x + p.dx); i++) {
			j: for(int j = p.y; j < (p.y + p.dy); j++) {
				k: for(int k = 0; k < p.ssWidth; k++)
				l: for(int l = 0; l < p.ssHeight; l++) {
					double r = i + ((double)k / (double)p.ssWidth);
					double q = j + ((double)l / (double)p.ssHeight);
					
					Ray ray = camera.rayAt(r, p.height - q, p.width, p.height);
					RGB color = RayTracingEngine.lightingCalculation(ray, surfaces, lights,
										p.fogColor, p.fogDensity, p.fogRatio, null);
					
					if (color == null) {
						// System.out.println("null");
						color = RayTracingEngine.black;
					}
					
					if (image[i - p.x][j - p.y] == null) {
						if (p.ssWidth > 1 || p.ssHeight > 1) {
							color.divideBy(p.ssWidth * p.ssHeight);
						}
						
						image[i - p.x][j - p.y] = color;
					} else {
						color.divideBy(p.ssWidth * p.ssHeight);
						image[i - p.x][j - p.y].addTo(color);
					}
				}
				
				if (monitor != null) {
					if (((i - p.x) * p.dy + j - p.y) % monitor.getIncrementSize() == 0)
						monitor.increment();
				}
			}
		}
		
		RayTracingEngine.inProgressCount--;
		if (RayTracingEngine.inProgressCount <= 0) RayTracingEngine.inProgress = false;
		
		return image;
	}
	
	public static double calculateAverageBrightness(Scene scene, int width, int height, int itr) {
		return RayTracingEngine.calculateAverageBrightness(scene, 0, 0, width, height,
														width, height, 1, 1, itr);
	}
	
	public static double calculateAverageBrightness(Scene scene, int width, int height,
											int ssWidth, int ssHeight, int itr) {
		return RayTracingEngine.calculateAverageBrightness(scene, 0, 0, width, height,
														width, height, ssWidth, ssHeight,
														itr);
	}
	
	public static double calculateAverageBrightness(Scene scene, int x, int y, int dx, int dy,
											int width, int height, int ssWidth, int ssHeight,
											int itr) {
		int xc = x + dx / 2;
		int yc = y + dy / 2;
		
		if (itr == 0) {
			RGB color = RayTracingEngine.render(scene, xc, yc, 1, 1, width, height,
										ssWidth, ssHeight, null)[0][0];
			return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
		} else {
			double c1 =	RayTracingEngine.calculateAverageBrightness(scene, x, y,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c2 = RayTracingEngine.calculateAverageBrightness(scene, x + dx / 2, y,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c3 = RayTracingEngine.calculateAverageBrightness(scene, x, y + dy / 2,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c4 = RayTracingEngine.calculateAverageBrightness(scene, xc, yc,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			
			return (c1 + c2 + c3 + c4) / 4.0;
		}
	}
	
	/**
	 * Performs intersection and lighting calculations for the specified Ray, Surfaces,
	 * and Lights. This method may return null, which should be interpreted as black
	 * (or "nothing").
	 */
	public static RGB lightingCalculation(Ray r, Surface allSurfaces[], Light allLights[],
										RGB fog, double fd, double fr, ShaderParameters p) {
		Intersection intersect = RayTracingEngine.closestIntersection(r, allSurfaces);
		
		RGB color = new RGB(0.0, 0.0, 0.0);
		
		// TODO  Figure out what this is for.
		Surface surface = null;
		
		if (intersect != null) {
			double intersection = intersect.getClosestIntersection();
			// r = intersect.getRay();
			Vector point = r.pointAt(intersection);
			
			Surface surf = intersect.getSurface();
			Surface otherSurf[] = allSurfaces;
			
			for(int i = 0; i < allSurfaces.length; i++) {
				if (surface == allSurfaces[i]) {
					// See separateSurfaces method.
					
					otherSurf = new Surface[allSurfaces.length - 1];
					
					for (int j = 0; j < i; j++) 
						otherSurf[j] = allSurfaces[j];
					for (int j = i + 1; j < allSurfaces.length; j++)
						otherSurf[j - 1] = allSurfaces[j];
				}
			}
			
//			if (RefractionShader.lastRay != null && !RefractionShader.lastRay.equals(r.getDirection()))
//				System.out.println("1: " + RefractionShader.lastRay + " " + r.getDirection());
			
			for(int i = 0; i < allLights.length; i++) {
				// See RayTracingEngine.seperateLights method
				
				Light otherL[] = new Light[allLights.length - 1];
				
				for (int j = 0; j < i; j++) { otherL[j] = allLights[j]; }
				for (int j = i + 1; j < allLights.length; j++) { otherL[j - 1] = allLights[j]; }
				
//				if (RefractionShader.lastRay != null && !RefractionShader.lastRay.equals(r.getDirection()))
//					System.out.println("2: " + RefractionShader.lastRay + " " + r.getDirection());
				
				RGB c = null;
				
//				if (RefractionShader.lastRay != null && !RefractionShader.lastRay.equals(r.getDirection()))
//					System.out.println("3: " + RefractionShader.lastRay + " " + r.getDirection());
				
				if (RayTracingEngine.castShadows && allLights[i].castShadows &&
						RayTracingEngine.shadowCalculation(point, allSurfaces, allLights[i]))
					return new RGB(0.0, 0.0, 0.0);
				
				if (allLights[i] instanceof SurfaceLight) {
					Light l[] = ((SurfaceLight)allLights[i]).getSamples();
					
					c = RayTracingEngine.lightingCalculation(point, r.getDirection(),
							surf, otherSurf, ((SurfaceLight)allLights[i]).getSamples(), p);
				} else if (allLights[i] instanceof PointLight) {
					Vector direction = point.subtract(((PointLight)allLights[i]).getLocation());
					DirectionalAmbientLight directionalLight =
						new DirectionalAmbientLight(1.0, allLights[i].getColorAt(point), direction);
					
//					if (RefractionShader.lastRay != null &&
//							!RefractionShader.lastRay.equals(r.getDirection()))
//						System.out.println("4:" + RefractionShader.lastRay + " " + r.getDirection());
					
					Vector rayDirection = r.getDirection();
					
					Vector v = (rayDirection.divide(rayDirection.length())).minus();
					Vector l = (directionalLight.getDirection().divide(directionalLight.getDirection().length())).minus();
					
//					if (RefractionShader.lastRay != null &&
//						!RefractionShader.lastRay.equals(r.getDirection()))
//						System.out.println("5: " + RefractionShader.lastRay + " " + rayDirection);
					
					if (p == null) {
						c = surf.shade(new ShaderParameters(point, v, l, directionalLight,
								otherL, otherSurf));
					} else {
						p.setPoint(point);
						p.setViewerDirection(v);
						p.setLightDirection(l);
						p.setLight(directionalLight);
						p.setOtherLights(otherL);
						p.setOtherSurfaces(otherSurf);
						
						c = surf.shade(p);
					}
				} else if (allLights[i] instanceof DirectionalAmbientLight) {
					DirectionalAmbientLight directionalLight = (DirectionalAmbientLight) allLights[i];
					
					Vector rayDirection = r.getDirection();
					
					Vector v = (rayDirection.divide(rayDirection.length())).minus();
					Vector l = (directionalLight.getDirection().divide(
							directionalLight.getDirection().length())).minus();
					
//					if (RefractionShader.lastRay != null &&
//							!RefractionShader.lastRay.equals(r.getDirection()))
//						System.out.println("6: " + RefractionShader.lastRay + " " + rayDirection);
					
					if (p == null) {
						c = surface.shade(new ShaderParameters(point, v, l, directionalLight,
								otherL, otherSurf));
					} else {
						p.setPoint(point);
						p.setViewerDirection(v);
						p.setLightDirection(l);
						p.setLight(directionalLight);
						p.setOtherLights(otherL);
						p.setOtherSurfaces(otherSurf);
						
						c = surf.shade(p);
					}
				} else if (allLights[i] instanceof AmbientLight) {
					c = RayTracingEngine.ambientLightingCalculation(point, r.getDirection(),
							surf, otherSurf, (AmbientLight)allLights[i]);
				} else {
					c = new RGB(0.0, 0.0, 0.0);
				}
				
				if (c != null) color.addTo(c);
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
	public static RGB lightingCalculation(Vector point, Vector rayDirection, Surface surface,
										Surface otherSurfaces[], Light lights[], ShaderParameters p) {
		RGB color = new RGB(0.0, 0.0, 0.0);
		
		for(int i = 0; i < lights.length; i++) {
			// See RayTracingEngine.seperateLights method
			
			Light otherLights[] = new Light[lights.length - 1];
			
			for (int j = 0; j < i; j++) { otherLights[j] = lights[j]; }
			for (int j = i + 1; j < lights.length; j++) { otherLights[j - 1] = lights[j]; }
			
			RGB c = RayTracingEngine.lightingCalculation(point, rayDirection,
														surface, otherSurfaces,
														lights[i], otherLights, p);
			if (c != null) color.addTo(c);
		}
		
		return color;
	}
	
	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * interesection on that surface using the lighting data from the specified Light object
	 * and returns an RGB object that represents the color of the point. A list of all other
	 * surfaces in the scene must be specified for reflection/shadowing. This list does not
	 * include the specified surface for which the lighting calculations are to be done.
	 */
	public static RGB lightingCalculation(Vector point, Vector rayDirection, Surface surface,
										Surface otherSurfaces[], Light light, Light otherLights[],
										ShaderParameters p) {
		Surface allSurfaces[] = new Surface[otherSurfaces.length + 1];
		for (int i = 0; i < otherSurfaces.length; i++) { allSurfaces[i] = otherSurfaces[i]; }
		allSurfaces[allSurfaces.length - 1] = surface;
		
		if (RayTracingEngine.castShadows && light.castShadows &&
				RayTracingEngine.shadowCalculation(point, allSurfaces, light))
			return new RGB(0.0, 0.0, 0.0);
		
		if (light instanceof SurfaceLight) {
			Light l[] = ((SurfaceLight)light).getSamples();
			return RayTracingEngine.lightingCalculation(point, rayDirection, surface,
														otherSurfaces, l, p);
		} else if (light instanceof PointLight) {
			return RayTracingEngine.pointLightingCalculation(point, rayDirection,
															surface, otherSurfaces,
															(PointLight)light, otherLights, p);
		} else if (light instanceof DirectionalAmbientLight) {
			return RayTracingEngine.directionalAmbientLightingCalculation(
															point, rayDirection,
															surface, otherSurfaces,
															(DirectionalAmbientLight)light,
															otherLights, p);
		} else if (light instanceof AmbientLight) {
			return RayTracingEngine.ambientLightingCalculation(point, rayDirection,
																surface, otherSurfaces,
																(AmbientLight)light);
		} else {
			return new RGB(0.0, 0.0, 0.0);
		}
	}
	
	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * interesection on that surface using the lighting data from the specified AmbientLight
	 * object and returns an RGB object that represents the color of the point. A list of all
	 * other surfaces in the scene must be specified for reflection/shadowing. This list does
	 * not include the specified surface for which the lighting calculations are to be done.
	 */
	public static RGB ambientLightingCalculation(Vector point, Vector rayDirection, Surface surface, Surface otherSurfaces[], AmbientLight light) {
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.print(" AmbientLight {");
		}
		
		RGB color = null;
		
		Surface allSurfaces[] = new Surface[otherSurfaces.length + 1];
		for (int i = 0; i < otherSurfaces.length; i++) { allSurfaces[i] = otherSurfaces[i]; }
		allSurfaces[allSurfaces.length - 1] = surface;
		
		color = light.getColor().multiply(light.getIntensity());
		color.multiplyBy(surface.getColorAt(point));
		
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.print(" : Color = " + color.toString() + " }");
		}
		
		return color;
	}
	
	/**
	 * Performs the lighting calculations for the specified surface at the specified point of interesection
	 * on that surface using the lighting data from the specified DirectionalAmbientLight object and returns
	 * an RGB object that represents the color of the point. A list of all other surfaces in the scene must
	 * be specified for reflection/shadowing. This list does not include the specified surface for which
	 * the lighting calculations are to be done.
	 * 
	 * @param point  The intersection point on the surface to be shaded.
	 * @param rayDirection  Direction of the ray that intersected the surface to be shaded.
	 * @param surface  The Surface object to use for shading calculations.
	 * @param otherSurfaces  An array of Surface objects that are also in the scene.
	 * @param light  The DirectionalAmbientLight instance to use for shading calculations.
	 * @param otherLights[]  An array of Light objects that are also in the scene.
	 * @param p  A ShaderParameters object that stores all parameters that are persisted
	 *           during a single set of ray casting events (reflections, refractions, etc.)
	 *           (null accepted).
	 */
	public static RGB directionalAmbientLightingCalculation(Vector point, Vector rayDirection,
														Surface surface, Surface otherSurfaces[],
														DirectionalAmbientLight light, Light otherLights[],
														ShaderParameters p) {
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.print(" DirectionalAmbientLight {");
		}
		
		RGB color = null;
		
		Surface allSurfaces[] = new Surface[otherSurfaces.length + 1];
		for (int i = 0; i < otherSurfaces.length; i++) { allSurfaces[i] = otherSurfaces[i]; }
		allSurfaces[allSurfaces.length - 1] = surface;
		
		Vector v = (rayDirection.divide(rayDirection.length())).minus();
		Vector l = (light.getDirection().divide(light.getDirection().length())).minus();
		
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Vector n = surface.getNormalAt(point);
			
			Settings.rayEngineOut.print(" V = " + v.toString());
			Settings.rayEngineOut.print(" N = " + n.toString());
			Settings.rayEngineOut.print(" L = " + l.toString());
		}
		
		if (p == null) {
			color = surface.shade(new ShaderParameters(point, v, l, light, otherLights, otherSurfaces));
		} else {
			p.setPoint(point);
			p.setViewerDirection(v);
			p.setLightDirection(l);
			p.setLight(light);
			p.setOtherLights(otherLights);
			p.setOtherSurfaces(otherSurfaces);
			
			color = surface.shade(p);
		}
		
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.print(" : Color = " + color + " }");
		}
		
		return color;
	}
	
	/**
	 * Performs the lighting calculations for the specified surface at the specified point of
	 * interesection on that surface using the lighting data from the specified PointLight
	 * object and returns an RGB object that represents the color of the point.
	 * A list of all other surfaces in the scene must be specified for reflection/shadowing.
	 * This list does not include the specified surface for which the lighting calculations
	 * are to be done. If the premultiplyIntensity option is set to true the color of the
	 * point light will be adjusted by the intensity of the light and the intensity will
	 * then be set to 1.0. If the premultiplyIntensity option is set to false, the color will
	 * be left unattenuated and the shaders will be responsible for adjusting the color
	 * based on intensity.
	 */
	public static RGB pointLightingCalculation(Vector point, Vector rayDirection,
											Surface surface, Surface otherSurfaces[],
											PointLight light, Light otherLights[],
											ShaderParameters p) {
		Vector direction = point.subtract(light.getLocation());
		DirectionalAmbientLight dLight = null;
		
		if (RayTracingEngine.premultiplyIntensity) {
			dLight = new DirectionalAmbientLight(1.0, light.getColorAt(point), direction);
		} else {
			double in = light.getIntensity();
			light.setIntensity(1.0);
			dLight = new DirectionalAmbientLight(in, light.getColorAt(point), direction);
			light.setIntensity(in);
		}
		
		return RayTracingEngine.directionalAmbientLightingCalculation(
											point, rayDirection,
											surface, otherSurfaces,
											dLight, otherLights, p);
	}
	
	/**
	  Performs the shadow calculations for the specified surfaces at the specified point using the data from the specified Light object.
	  Returns true if the point has a shadow cast on it.
	*/
	
	public static boolean shadowCalculation(Vector point, Surface surfaces[], Light light) {
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.print(" Shadow {");
		}
		
		double maxDistance = -1.0;
		Vector direction = null;
		
		if (light instanceof PointLight) {
			direction = ((PointLight)light).getLocation().subtract(point);
			direction = direction.divide(direction.length());
			maxDistance = direction.length();
		} else if (light instanceof DirectionalAmbientLight) {
			direction = ((DirectionalAmbientLight)light).getDirection().minus();
		} else {
			if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
				Settings.rayEngineOut.print(" False }");
			}
			
			return false;
		}
		
		Ray shadowRay = new Ray(point, direction);
		
		Intersection closestIntersectedSurface = RayTracingEngine.closestIntersection(shadowRay, surfaces);
		double intersect = 0.0;
		if (closestIntersectedSurface != null)
			intersect = closestIntersectedSurface.getClosestIntersection();
		
		if (closestIntersectedSurface == null || intersect <= RayTracingEngine.e || (maxDistance >= 0.0 && intersect > maxDistance)) {
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
	  Reflects the specified Vector object across the normal vector represented by the second specified Vector object and returns the result.
	*/
	
	public static Vector reflect(Vector vector, Vector normal) {
		vector = vector.minus();
		Vector reflected = vector.subtract(normal.multiply(2 * (vector.dotProduct(normal) / normal.lengthSq())));
		
		return reflected;
	}
	
	/**
	  Refracts the specified Vector object based on the specified normal vector and 2 specified indices of refraction.
	  
	  @param vector  A Vector object representing a unit vector in the direction of the incident ray
	         normal  A Vector object respresenting a unit vector that is normal to the surface refracting the ray
	         ni  A double value representing the index of refraction of the incident medium
	         nr  A double value representing the index of refraction of the refracting medium
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
	 * Returns an Intersection object that represents the closest intersection
	 * (>= RayTracingEngine.e) between a surface in the specified array of Surface
	 * objects and the ray represented by the specified Ray object. If there are
	 * no intersections >= RayTracingEngine.e then null is returned.
	 */
	public static Intersection closestIntersection(Ray ray, Surface surfaces[]) {
		Intersection intersections[] = new Intersection[surfaces.length];
		
		for(int i = 0; i < surfaces.length; i++) {
			intersections[i] = surfaces[i].intersectAt((Ray)ray.clone());
		}
		
		double closestIntersection = -1.0;
		int closestIntersectionIndex = -1;
		
		i: for(int i = 0; i < intersections.length; i++) {
			if (intersections[i] == null)
				continue i;
			
			for(int j = 0; j < intersections[i].getIntersections().length; j++) {
				if (intersections[i].getIntersections()[j] >= RayTracingEngine.e) {
					if (closestIntersectionIndex == -1 || intersections[i].getIntersections()[j] < closestIntersection) {
						closestIntersection = intersections[i].getIntersections()[j];
						closestIntersectionIndex = i;
					}
				}
			}
		}
		
		if (closestIntersectionIndex < 0)
			return null;
		else
			return intersections[closestIntersectionIndex];
	}
	
	/**
	  Returns the value (>= RayTracingEngine.e) of the closest intersection point of the specified Intersection object
	  If there are no positive intersections, -1.0 is returned.
	*/
	
	public static double closestIntersectionAt(Intersection intersect) {
		double intersections[] = intersect.getIntersections();
		
		double closestIntersection = -1.0;
		
		for(int i = 0; i < intersections.length; i++) {
			if (intersections[i] >= RayTracingEngine.e) {
				if (closestIntersection == -1.0 || intersections[i] < closestIntersection) {
					closestIntersection = intersections[i];
				}
			}
		}
		
		return closestIntersection;
	}
	
	/**
	  Removes the Light object at the specified index from the specified Light object array and returns the new array.
	*/
	
	public static Light[] separateLights(int index, Light allLights[]) {
		Light otherLights[] = new Light[allLights.length - 1];
		
		for (int i = 0; i < index; i++) { otherLights[i] = allLights[i]; }
		for (int i = index + 1; i < allLights.length; i++) { otherLights[i - 1] = allLights[i]; }
		
		return otherLights;
	}
	
	/**
	  Removes the Surface object at the specified index from the specified Surface object array and returns the new array.
	*/
	
	public static Surface[] separateSurfaces(int index, Surface allSurfaces[]) {
		Surface otherSurfaces[] = new Surface[allSurfaces.length - 1];
		
		for (int i = 0; i < index; i++) { otherSurfaces[i] = allSurfaces[i]; }
		for (int i = index + 1; i < allSurfaces.length; i++) { otherSurfaces[i - 1] = allSurfaces[i]; }
		
		return otherSurfaces;
	}
	
	/**
	  Removes the specified Surface object from the specified Surface object array and returns the new array.
	  If the specified Surface object is not matched, the whole array is returned.
	*/
	
	public static Surface[] separateSurfaces(Surface surface, Surface allSurfaces[]) {
		for(int i = 0; i < allSurfaces.length; i++) {
			if (surface == allSurfaces[i]) {
				// See separateSurfaces method.
				
				Surface otherSurfaces[] = new Surface[allSurfaces.length - 1];
				
				for (int j = 0; j < i; j++) { otherSurfaces[j] = allSurfaces[j]; }
				for (int j = i + 1; j < allSurfaces.length; j++) { otherSurfaces[j - 1] = allSurfaces[j]; }
				
				return otherSurfaces;
			}
		}
		
		return allSurfaces;
	}
}
