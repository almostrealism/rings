/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.raytracer;

import javax.swing.JPanel;

import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.util.graphics.RGB;

import net.sf.j3d.ui.displays.ProgressDisplay;

public class AbsorberSetRayTracer {
	private Camera camera;
	private Surface surfaces[];
	private Light lights[];
	private int w, h;
	
	private ProgressDisplay display;
	
	public AbsorberSetRayTracer(Camera camera, Surface surfaces[], Light lights[],
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
