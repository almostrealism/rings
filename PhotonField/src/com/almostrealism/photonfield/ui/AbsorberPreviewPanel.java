/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.ui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;

import com.almostrealism.photonfield.Absorber;
import com.almostrealism.photonfield.AbsorberHashSet;
import com.almostrealism.photonfield.Clock;
import com.almostrealism.photonfield.DefaultPhotonField;
import com.almostrealism.photonfield.SpecularAbsorber;
import com.almostrealism.photonfield.Volume;
import com.almostrealism.photonfield.VolumeAbsorber;
import com.almostrealism.photonfield.geometry.Plane;
import com.almostrealism.photonfield.geometry.Sphere;
import com.almostrealism.photonfield.light.LightBulb;
import com.almostrealism.photonfield.light.PlanarLight;
import com.almostrealism.photonfield.raytracer.AbsorberSetRayTracer;
import com.almostrealism.photonfield.raytracer.PinholeCameraAbsorber;
import com.almostrealism.photonfield.util.PhysicalConstants;
import com.almostrealism.photonfield.util.ProbabilityDistribution;

import net.sf.j3d.ui.displays.ImageCanvas;
import net.sf.j3d.ui.displays.ProgressDisplay;
import net.sf.j3d.util.graphics.RGB;

/**
 * @author  Mike Murray
 */
public class AbsorberPreviewPanel extends JPanel
								implements Runnable,
								ProbabilityDistribution.Sampler,
								PhysicalConstants {
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
		this.set.addAbsorber(l, new double[] {0.0, 15.0, 0.0});
		
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
		
		this.set.addAbsorber(a, new double[3]);
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
									new double[] {0.0, 0.0, -1.0},
									new double[] {0.0, 1.0, 0.0});
		camera.setWidth(w);
		camera.setHeight(h);
		camera.setPixelSize(0.1);
		
		this.set.addAbsorber(camera, new double[] {0.0, 0.0, 10.0});
		
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
		
		this.set.addAbsorber(this.absorber, new double[3]);
		this.set.setColorBufferDimensions(this.bufDim, this.bufDim, this.bufScale * 10.0);
	}
	
	protected void initLight() {
		this.light = new PlanarLight();
		this.light.setPower(PhysicalConstants.wattsToEvMsec * 0.5);
		this.light.setSpectra(new ProbabilityDistribution(this));
		((PlanarLight)this.light).setLightPropagation(true);
		((PlanarLight)this.light).setWidth(5.0);
		((PlanarLight)this.light).setHeight(5.0);
		((PlanarLight)this.light).setSurfaceNormal(new double[] {0.0, -1.0, 0.0});
		((PlanarLight)this.light).setOrientation(new double[] {0.0, 0.0, 1.0});
		
		this.set.addAbsorber(this.light, new double[] {0.0, 15.0, 0.0});
	}
	
	protected void initFloor() {
		Plane p = new Plane();
		p.setSurfaceNormal(new double[] {0.0, 1.0, 0.0});
		p.setOrientation(new double[] {0.0, 0.0, 1.0});
		p.setWidth(20.0);
		p.setHeight(20.0);
		
		double t = this.clock.getTickDistance() * 1.5;
		p.setThickness(t);
		System.out.println("AbsorberPreviewPanel: Thickness = " + t);
		
		SpecularAbsorber a = new SpecularAbsorber();
		a.setSpectra(new ProbabilityDistribution(this));
		((VolumeAbsorber)a).setVolume(p);
		
		this.set.addAbsorber(a, new double[] {0.0, -5.0, 0.0});
		this.set.setColorBufferDimensions(this.bufDim, this.bufDim, this.bufScale);
	}
	
	public void run() {
		this.set.clearColorBuffers();
		Clock c = this.set.getClock();
		
		while (!this.stopUpdate) {
			c.tick();
			this.progPanel.increment();
			
			if (c.getTicks() % 1000 == 0) {
				this.progPanel.setProgressBarColor(Color.red);
				this.progPanel.reset();
				
				AbsorberSetRayTracer tracer = this.set.getRayTracer();
				tracer.setDisplay(this.progPanel);
				
				RGB rgb[][] = tracer.generateImage(2, 2);
				this.canvas.setImageData(rgb);
				
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