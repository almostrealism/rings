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

package org.almostrealism.space;

import com.almostrealism.projection.Camera;
import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.lighting.Light;

/**
 * A Scene object represents a scene in 3d. It stores a Camera object, an array of Light objects,
 * and an array of Surface objects.
 */
public class Scene implements Cloneable {
  private Camera camera;
  
  private Light lights[];
  private Surface surfaces[];

	/**
	 * Constructs a Scene object with a default Camera object and no Light or Surface objects.
	 */
	public Scene() {
		this.setCamera(new PinholeCamera());
		
		this.setLights(new Light[0]);
		this.setSurfaces(new Surface[0]);
	}
	
	/**
	 * Constructs a Scene object with a default Camera object, no Light objects,
	 * and the surfaces represented by the specified Surface array.
	 */
	public Scene(Surface surfaces[]) {
		this.setCamera(new PinholeCamera());
		
		this.setLights(new Light[0]);
		this.setSurfaces(surfaces);
	}
	
	/**
	 * Constructs a Scene object with the specified Camera object, Light array, and Surface array.
	 */
	public Scene(Camera camera, Light lights[], Surface surfaces[]) {
		this.setCamera(camera);
		
		this.setLights(lights);
		this.setSurfaces(surfaces);
	}
	
	/**
	 * Sets the camera of this Scene object to the camera represented by the specified Camera object.
	 */
	public void setCamera(Camera camera) { this.camera = camera; }
	
	/**
	 * Replaces all of the lights of this Scene object with those represented by the specified Light array.
	 */
	public void setLights(Light lights[]) { this.lights = lights; }
	
	/**
	 * Adds the specified Light object to this Scene object.
	 */
	public void addLight(Light light) {
		Light newLights[] = new Light[this.lights.length + 1];
		
		System.arraycopy(this.lights, 0, newLights, 0, this.lights.length);
		newLights[newLights.length - 1] = light;
		
		this.setLights(newLights);
	}
	
	/**
	 * Removes the Light object stored at the specified index from this Scene object.
	 */
	public void removeLight(int index) {
		Light newLights[] = new Light[this.lights.length - 1];
		
		System.arraycopy(this.lights, 0, newLights, 0, index);
		if (index != this.lights.length - 1) {
			System.arraycopy(this.lights, index + 1, newLights, index, this.lights.length - (index + 1));
		}
		
		this.setLights(newLights);
	}
	
	/**
	 * Replaces all of the Surface objects of this Scene object with those represented
	 * by the specified Surface array.
	 */
	public void setSurfaces(Surface surfaces[]) { this.surfaces = surfaces; }
	
	/**
	 * Adds the specified Surface object to this Scene object.
	 */
	public void addSurface(Surface surface) {
		Surface newSurfaces[] = new Surface[this.surfaces.length + 1];
		
		System.arraycopy(this.surfaces, 0, newSurfaces, 0, this.surfaces.length);
		newSurfaces[newSurfaces.length - 1] = surface;
		
		this.setSurfaces(newSurfaces);
	}
	
	/**
	 * Removes the Surface object stored at the specified index from this Scene object.
	 */
	public void removeSurface(int index) {
		Surface newSurfaces[] = new Surface[this.surfaces.length - 1];
		
		System.arraycopy(this.surfaces, 0, newSurfaces, 0, index);
		if (index != this.surfaces.length - 1) {
			System.arraycopy(this.surfaces, index + 1, newSurfaces, index, this.surfaces.length - (index + 1));
		}
		
		this.setSurfaces(newSurfaces);
	}
	
	/**
	 * Returns the Camera object stored by this Scene object.
	 */
	public Camera getCamera() { return this.camera; }
	
	/**
	 * Returns the Light objects stored by this Scene object as a Light array.
	 */
	public Light[] getLights() { return this.lights; }
	
	/**
	 * Returns the Surface object stored by this Scene object at the specified index.
	 */
	public Light getLight(int index) { return this.lights[index]; }
	
	/**
	 * Returns the Surface objects stored by this Scene object as a Surface array.
	 */
	public Surface[] getSurfaces() { return this.surfaces; }
	
	/**
	 * Returns the Surface object stored by this Scene object at the specified index.
	 */
	public Surface getSurface(int index) { return this.surfaces[index]; }
	
	/**
	 * @return  A Scene object that stores the same Camera, Lights, and Surfaces as this Scene object.
	 */
	public Object clone() { return new Scene(this.camera, this.lights, this.surfaces); }
}
