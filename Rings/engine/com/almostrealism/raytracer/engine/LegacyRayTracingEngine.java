/*
 * Copyright 2017 Michael Murray
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

// TODO  Make this class use fewer method calls for calculation.
// TODO  Add iterative sampling at increasing resolutions.
// TODO  Add support for brightness histograms (randomly sample pixels).

package com.almostrealism.raytracer.engine;

import java.util.Collection;

import org.almostrealism.color.RGB;
import org.almostrealism.space.Ray;
import org.almostrealism.swing.ProgressMonitor;
import org.almostrealism.swing.displays.ProgressDisplay;

import com.almostrealism.lighting.Light;
import com.almostrealism.projection.Camera;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.Settings;

/**
 * The {@link LegacyRayTracingEngine} class provides static methods for rendering scenes.
 * 
 * @author Mike Murray
 */
public class LegacyRayTracingEngine {
  /**
   * Controls whether or not shadow casting will be done during rendering.
   * By default set to true.
   */
  public static boolean castShadows = true;
  
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
		return LegacyRayTracingEngine.render(scene, scene.getCamera(), scene.getLights(), p, monitor);
	}
	
	public static RGB[][] render (Scene scene, int x, int y, int dx, int dy,
								int width, int height, int ssWidth, int ssHeight,
								ProgressMonitor monitor) {
		RenderParameters p = new RenderParameters(x, y, dx, dy, width, height, ssWidth, ssHeight);
		return LegacyRayTracingEngine.render(scene, scene.getCamera(), scene.getLights(), p, monitor);
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
		return LegacyRayTracingEngine.render(scene, scene.getCamera(), scene.getLights(), p, prog);
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
	public static RGB[][] render(Collection<ShadableSurface> surfaces, Camera camera, Light lights[], RenderParameters p, ProgressMonitor monitor) {
		if (Settings.produceOutput && Settings.produceRayTracingEngineOutput) {
			Settings.rayEngineOut.println("Entering RayTracingEngine (" + p.width + " X " + p.ssWidth + ", " + p.height + " X " + p.ssHeight + ") : " + surfaces.size() + " Surfaces");
			Settings.rayEngineOut.println("Camera: " + camera.toString());
		}
		
		LegacyRayTracingEngine.inProgressCount++;
		LegacyRayTracingEngine.inProgress = true;
		
		RGB image[][] = new RGB[p.dx][p.dy];
		
		for (int i = p.x; i < (p.x + p.dx); i++) {
			for (int j = p.y; j < (p.y + p.dy); j++) {
				for (int k = 0; k < p.ssWidth; k++)
				for (int l = 0; l < p.ssHeight; l++) {
					double r = i + ((double)k / (double)p.ssWidth);
					double q = j + ((double)l / (double)p.ssHeight);
					
					Ray ray = camera.rayAt(r, p.height - q, p.width, p.height);
					RGB color = RayIntersectionEngine.lightingCalculation(ray, surfaces, lights,
										p.fogColor, p.fogDensity, p.fogRatio, null).evaluate(null);
					
					if (color == null) {
						// System.out.println("null");
						color = RayTracedScene.black;
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
		
		LegacyRayTracingEngine.inProgressCount--;
		if (LegacyRayTracingEngine.inProgressCount <= 0) LegacyRayTracingEngine.inProgress = false;
		
		return image;
	}
	
	public static double calculateAverageBrightness(Scene scene, int width, int height, int itr) {
		return LegacyRayTracingEngine.calculateAverageBrightness(scene, 0, 0, width, height,
														width, height, 1, 1, itr);
	}
	
	public static double calculateAverageBrightness(Scene scene, int width, int height,
											int ssWidth, int ssHeight, int itr) {
		return LegacyRayTracingEngine.calculateAverageBrightness(scene, 0, 0, width, height,
														width, height, ssWidth, ssHeight,
														itr);
	}
	
	public static double calculateAverageBrightness(Scene scene, int x, int y, int dx, int dy,
											int width, int height, int ssWidth, int ssHeight,
											int itr) {
		int xc = x + dx / 2;
		int yc = y + dy / 2;
		
		if (itr == 0) {
			RGB color = LegacyRayTracingEngine.render(scene, xc, yc, 1, 1, width, height,
										ssWidth, ssHeight, null)[0][0];
			return (color.getRed() + color.getGreen() + color.getBlue()) / 3.0;
		} else {
			double c1 =	LegacyRayTracingEngine.calculateAverageBrightness(scene, x, y,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c2 = LegacyRayTracingEngine.calculateAverageBrightness(scene, x + dx / 2, y,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c3 = LegacyRayTracingEngine.calculateAverageBrightness(scene, x, y + dy / 2,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			double c4 = LegacyRayTracingEngine.calculateAverageBrightness(scene, xc, yc,
						dx / 2, dy / 2,
						width, height,
						ssWidth, ssHeight,
						itr - 1);
			
			return (c1 + c2 + c3 + c4) / 4.0;
		}
	}
}
