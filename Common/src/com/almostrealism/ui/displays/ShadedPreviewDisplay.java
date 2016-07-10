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

/*
 * Copyright (C) 2005  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.ui.displays;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.camera.PinholeCamera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.lighting.DirectionalAmbientLight;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.primitives.Sphere;
import com.almostrealism.raytracer.shaders.ShaderParameters;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;


/**
 * @author Mike Murray
 */
public class ShadedPreviewDisplay extends JPanel implements Runnable {
	private class SampleSurface extends Sphere {
		private Surface surface = new Sphere();
		
		public void setSurface(Surface s) {
			this.surface = s;
			if (this.surface == null) this.surface = new Sphere();
		}
		
		public boolean getShadeFront() { return this.surface.getShadeFront(); }
		public boolean getShadeBack() { return this.surface.getShadeBack(); }
		public RGB getColorAt(Vector p) { return this.surface.getColorAt(p); }
		public RGB shade(ShaderParameters sp) { return this.surface.shade(sp); }
	}
	
	private Camera camera;
	private Light lights[];
	private SampleSurface sample;
	private Image image;
	private int w, h;
	
	public ShadedPreviewDisplay() {
		this.sample = new SampleSurface();
		
		this.lights = new Light[] {
			new DirectionalAmbientLight(0.8, new RGB(1.0, 1.0, 1.0), new Vector(0.75, -1.0, -0.75))
		};
		
		this.camera = new PinholeCamera(new Vector(0.0, 0.0, 2.0),
										new Vector(0.0, 0.0, -1.0),
										new Vector(0.0, 1.0, 0.0),
										0.5, 1.0, 1.0);
		
		this.w = 30;
		this.h = 30;
		
		super.setPreferredSize(new Dimension(this.w + 20, this.h + 20));
		
		super.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				ShadedPreviewDisplay.this.refresh();
			}
		});
	}
	
	public void setSurface(Surface s) { this.sample.setSurface(s); }
	
	public void refresh() {
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		if (this.sample != null) {
			RenderParameters p = new RenderParameters(0, 0, w, h, w, h, 1, 1);
			
			RGB rgb[][] = RayTracingEngine.render(new Surface[] { this.sample },
												this.camera, this.lights,
												p, null);
			this.image = GraphicsConverter.convertToAWTImage(rgb);
		} else {
			this.image = null;
		}
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					ShadedPreviewDisplay.this.repaint();
				}
			});
		} catch (InterruptedException ie) {
		} catch (InvocationTargetException ite) {
			ite.printStackTrace();
		}
	}
	
	public void paint(Graphics g) {
		if (this.image != null)
			g.drawImage(this.image, 0, 0, this);
		else
			super.paint(g);
	}
}
