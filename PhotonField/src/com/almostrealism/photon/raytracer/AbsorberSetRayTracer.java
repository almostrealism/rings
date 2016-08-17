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

package com.almostrealism.photon.raytracer;

import javax.swing.JPanel;

import org.almostrealism.space.Scene;
import org.almostrealism.swing.displays.ProgressDisplay;
import org.almostrealism.texture.RGB;

import com.almostrealism.projection.Camera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.lighting.Light;

public class AbsorberSetRayTracer {
	private Camera camera;
	private ShadableSurface surfaces[];
	private Light lights[];
	private int w, h;
	
	private ProgressDisplay display;
	
	public AbsorberSetRayTracer(Camera camera, ShadableSurface surfaces[], Light lights[],
								int w, int h) {
		this.camera = camera;
		this.surfaces = surfaces;
		this.lights = lights;
		this.w = w;
		this.h = h;
	}
	
	public void setWidth(int w) { this.w = w; }
	public void setHeight(int h) { this.h = h; }
	public int getWidth() { return this.w; }
	public int getHeight() { return this.h; }
	
	public Scene getScene() {
		return new Scene(this.camera, this.lights, this.surfaces);
	}
	
	public void setDisplay(ProgressDisplay p) { this.display = p; }
	
	public JPanel getDisplay() {
		if (this.display == null)
			this.display = new ProgressDisplay(this.w * this.h / 100, this.w * this.h);
		
		return this.display;
	}
	
	public RGB[][] generateImage(int ssw, int ssh) {
		Scene s = this.getScene();
		return RayTracingEngine.render(s, 0, 0, this.w, this.h,
										this.w, this.h, ssw, ssh, this.display);
	}
}
