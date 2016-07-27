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

package com.almostrealism.raytracer.shaders;

import java.util.Arrays;
import java.util.Hashtable;

import org.almostrealism.util.Vector;
import org.almostrealism.util.graphics.RGB;

import com.almostrealism.raytracer.Settings;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.lighting.Light;


/**
 * A ShaderParameters object stores parameters needed by most Shader implementations.
 * 
 * @author Mike Murray
 */
public class ShaderParameters extends Hashtable {
  private Vector point;
  private Vector viewerDirection;
  private Vector lightDirection;
  private Light light;
  private Light otherLights[];
  private Surface surface;
  private Surface otherSurfaces[];
  
  public RGB fogColor;
  public double fogRatio, fogDensity;
  
  private int refCount;
  private int exit, enter;

	/**
	 * Constructs a new ShaderParameters object using the specified arguments.
	 * 
	 * @param point  Vector object representing the point to be shaded.
	 * @param viewerDirection  Vector object representing the direction toward the viewer (should be unit length).
	 * @param lightDirection  Vector object representing the direction toward the light (should be unit length).
	 * @param light  Light object representing the light.
	 * @param otherLights  Array of Light objects representing other lights in the scene.
	 * @param surface  Surface object to be shaded.
	 * @param otherSurfaces  Array of other Surface objects in the scene.
	 */
	public ShaderParameters(Vector point, Vector viewerDirection, Vector lightDirection,
			Light light, Light otherLights[], Surface surface, Surface otherSurfaces[]) {
		this.point = point;
		this.viewerDirection = viewerDirection;
		this.lightDirection = lightDirection;
		this.light = light;
		this.otherLights = otherLights;
		this.surface = surface;
		this.otherSurfaces = otherSurfaces;
		
		this.refCount = 0;
	}
	
	public ShaderParameters(Vector point, Vector viewerDirection, Vector lightDirection,
			Light light, Light otherLights[], Surface otherSurfaces[]) {
		this(point, viewerDirection, lightDirection, light, otherLights, null, otherSurfaces);
	}
	
	/**
	 * Sets the point to be shaded to the specified Vector object.
	 * 
	 * @param p  Vector object to use.
	 */
	public void setPoint(Vector p) { this.point = p; }
	
	/**
	 * @return  A Vector object representing the point to be shaded.
	 */
	public Vector getPoint() { return (Vector) this.point.clone(); }
	
	/**
	 * Sets the direction toward the viewer to the specified Vector object.
	 * 
	 * @param v  Vector object to use.
	 */
	public void setViewerDirection(Vector v) { this.viewerDirection = v; }
	
	/**
	 * @return  A Vector object representing the direction toward the viewer
	 *          (this can be expected to be unit length).
	 */
	public Vector getViewerDirection() { return this.viewerDirection; }
	
	/**
	 * Sets the direction toward the light to the specified Vector object.
	 * 
	 * @param l  Vector object to use.
	 */
	public void setLightDirection(Vector l) { this.lightDirection = l; }
	
	/**
	 * @return  A Vector object representing the direction toward the light (this can be expected to be unit length).
	 */
	public Vector getLightDirection() { return this.lightDirection; }
	
	/**
	 * Sets the Light to the specified Light object.
	 * 
	 * @param l  Light object to use.
	 */
	public void setLight(Light l) { this.light = l; }
	
	/**
	 * @return  A Light object representing the light.
	 */
	public Light getLight() { return this.light; }
	
	/**
	 * Sets the other Lights to those stored in the specified array.
	 * 
	 * @param l  Array of Light objects to use.
	 */
	public void setOtherLights(Light l[]) { this.otherLights = l; }
	
	/**
	 * @return  An array of Light objects representing the other lights in the scene.
	 */
	public Light[] getOtherLights() { return this.otherLights; }
	
	/**
	 * @param surface  The new Surface object.
	 */
	public void setSurface(Surface surface) { this.surface = surface; }
	
	/**
	 * @return  The Surface object to be shaded.
	 */
	public Surface getSurface() { return this.surface; }
	
	/**
	 * Sets the other Surfaces to those stored in the specified array.
	 * 
	 * @param s  Array of Surface objects to use.
	 */
	public void setOtherSurfaces(Surface s[]) { this.otherSurfaces = s; }
	
	/**
	 * @return  An array of other Surface objects in the scene.
	 */
	public Surface[] getOtherSurfaces() { return this.otherSurfaces; }
	
	public Light[] getAllLights() {
		Light l[] = new Light[this.otherLights.length + 1];
		for (int i = 0; i < this.otherLights.length; i++) l[i] = this.otherLights[i];
		l[l.length - 1] = this.light;
		return l;
	}
	
	/**
	 * @return  The number of reflections (or other types of direction change) undergone.
	 */
	public int getReflectionCount() { return this.refCount; }
	
	/**
	 * @return  The number of surface enterances undergone.
	 */
	public int getEnteranceCount() { return this.enter; }
	
	/**
	 * @return  The number of surface exits undergone.
	 */
	public int getExitCount() { return this.exit; }
	
	/**
	 * Adds one to the reflection count stored by this ShaderParameters object.
	 */
	public void addReflection() { this.refCount++; }
	
	/**
	 * Adds one to the reflection count and the enterance count.
	 */
	public void addEnterance() { this.enter++; this.refCount++; }
	
	/**
	 * Adds one to the reflection count and the exit count.
	 * This method may result in a random warning if exit count
	 * is greater than enterance count.
	 */
	public void addExit() {
		this.exit++; this.refCount++;
		
		if (this.exit > this.enter && Math.random() < Settings.randomWarningThreshold)
			System.out.println(Settings.randomWarningSymbol +
					"ShaderParameters: Exit count exceedes entrance count.");
	}
	
	public String toString() {
		return this.point + ", " + this.viewerDirection + ", " + this.lightDirection + ", " +
				this.light + ", " + Arrays.toString(this.otherLights) + ", " + this.surface;
	}
}
