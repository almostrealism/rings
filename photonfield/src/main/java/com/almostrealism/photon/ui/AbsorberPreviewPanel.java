/*
 * Copyright 2018 Michael Murray
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

package com.almostrealism.photon.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import com.almostrealism.absorption.PinholeCameraAbsorber;
import com.almostrealism.primitives.Plane;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.color.ProbabilityDistribution;
import org.almostrealism.physics.Absorber;
import org.almostrealism.physics.PhysicalConstants;
import org.almostrealism.space.Volume;
import org.almostrealism.swing.displays.ProgressDisplay;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.physics.Clock;

import com.almostrealism.physics.AbsorberHashSet;
import com.almostrealism.physics.DefaultPhotonField;
import com.almostrealism.physics.SpecularAbsorber;
import com.almostrealism.physics.VolumeAbsorber;
import com.almostrealism.geometry.Sphere;
import com.almostrealism.light.LightBulb;
import com.almostrealism.light.PlanarLight;
import org.almostrealism.CodeFeatures;

/**
 * @author  Michael Murray
 */
public class AbsorberPreviewPanel extends JPanel
								implements Runnable,
								ProbabilityDistribution.Sampler,
								PhysicalConstants, CodeFeatures {
	private Clock clock;
	
	private AbsorberHashSet set;
	private Absorber absorber;
	private LightBulb light;
	
	private ProgressDisplay progPanel;
	private ImageCanvas canvas;
	
	private double specEnd = H * C / 0.380;
	private double specStart = H * C / 0.780;
	private int bufDim = 25;
	private double bufScale = 0.1;
	
	private Thread updateThread;
	private boolean stopUpdate;
	
	public AbsorberPreviewPanel() {
		this.clock = new Clock();
		this.set = new AbsorberHashSet();
		
		this.initFloor();
		this.initAbsorber();
		this.initLight();
		this.initCanvas();
	}
	
	public AbsorberPreviewPanel(LightBulb l) {
		this.clock = new Clock();
		this.set = new AbsorberHashSet();
		
		this.initFloor();
		this.initAbsorber();
		
		this.light = l;
		this.set.addAbsorber(l, vector(0.0, 15.0, 0.0));
		
		this.initCanvas();
	}
	
	public AbsorberPreviewPanel(Absorber a) {
		this.clock = new Clock();
		this.set = new AbsorberHashSet();
		
		this.initFloor();
		this.initLight();
		
		this.absorber = a;
		if (this.absorber instanceof SpecularAbsorber &&
				((SpecularAbsorber) this.absorber).getSpectra() == null)
			((SpecularAbsorber) this.absorber).setSpectra(new ProbabilityDistribution(this));
		
		this.set.addAbsorber(a, ZeroVector.getInstance());
		this.set.setColorBufferDimensions(this.bufDim, this.bufDim, this.bufScale * 10);
		
		this.initCanvas();
	}
	
	public void startUpdate() {
		if (this.updateThread != null) {
			this.stopUpdate = true;
			while (this.updateThread != null);
		}
		
		this.updateThread = new Thread(this);
		this.updateThread.start();
	}
	
	protected void initCanvas() {
		int w = 150, h = 150;
		
		PinholeCameraAbsorber camera =
			new PinholeCameraAbsorber(1.0 / 0.3, 2.5,
									new Vector(0.0, 0.0, -1.0),
									new Vector(0.0, 1.0, 0.0));
		camera.setWidth(w);
		camera.setHeight(h);
		camera.setPixelSize(0.1);
		
		this.set.addAbsorber(camera, vector(0.0, 0.0, 10.0));
		
		this.canvas = new ImageCanvas(w, h);
		this.progPanel = new ProgressDisplay(w * h / 1000, w * h, false);
		
		super.setLayout(new BorderLayout());
		super.add(this.canvas, BorderLayout.CENTER);
		super.add(this.progPanel, BorderLayout.SOUTH);
		
		DefaultPhotonField f = new DefaultPhotonField();
		f.setAbsorber(this.set);
		this.clock.addPhotonField(f);
	}
	
	protected void initAbsorber() {
		Volume v = new Sphere(5.0);
		this.absorber = new SpecularAbsorber();
		((VolumeAbsorber)this.absorber).setVolume(v);
		((SpecularAbsorber)this.absorber).setSpectra(new ProbabilityDistribution(this));
		
		this.set.addAbsorber(this.absorber, ZeroVector.getInstance());
		this.set.setColorBufferDimensions(this.bufDim, this.bufDim, this.bufScale * 10.0);
	}
	
	protected void initLight() {
		this.light = new PlanarLight();
		this.light.setPower(PhysicalConstants.wattsToEvMsec * 0.5);
		this.light.setSpectra(new ProbabilityDistribution(this));
		((PlanarLight) light).setLightPropagation(true);
		((PlanarLight) light).setWidth(5.0);
		((PlanarLight) light).setHeight(5.0);
		((PlanarLight) light).setSurfaceNormal(new Vector(0.0, -1.0, 0.0));
		((PlanarLight) light).setOrientation(new Vector(0.0, 0.0, 1.0));
		
		this.set.addAbsorber(light, vector(0.0, 15.0, 0.0));
	}
	
	protected void initFloor() {
		Plane p = new Plane();
		p.setSurfaceNormal(vector(0.0, 1.0, 0.0));
		p.setOrientation(new double[] {0.0, 0.0, 1.0});
		p.setWidth(20.0);
		p.setHeight(20.0);
		
		double t = this.clock.getTickDistance() * 1.5;
		p.setThickness(t);
		System.out.println("AbsorberPreviewPanel: Thickness = " + t);
		
		SpecularAbsorber a = new SpecularAbsorber();
		a.setSpectra(new ProbabilityDistribution(this));
		a.setVolume(p);
		
		this.set.addAbsorber(a, vector(0.0, -5.0, 0.0));
		this.set.setColorBufferDimensions(this.bufDim, this.bufDim, this.bufScale);
	}

	@Override
	public void run() {
		this.set.clearColorBuffers();
		Clock c = this.set.getClock();
		
		while (!this.stopUpdate) {
			c.tick().get().run();
			this.progPanel.increment();
			
			if (c.getTicks() % 1000 == 0) {
				this.progPanel.setProgressBarColor(Color.red);
				this.progPanel.reset();
				
//				AbsorberSetRayTracer tracer = this.set.getRayTracer();
//				tracer.setDisplay(this.progPanel);
//
//				RGB rgb[][] = tracer.generateImage(2, 2);
//				this.canvas.setImageData(rgb);
				
				this.progPanel.setProgressBarColor(Color.blue);
				this.progPanel.reset();
			}
		}
		
		this.set.getClock().setTime(0.0);
		
		this.updateThread = null;
		this.stopUpdate = false;
	}

	public double getProbability(double x) { return 1.0; }

	public double getSample(double r) {
		return this.specStart + Math.random() * (this.specEnd - this.specStart);
	}
}
