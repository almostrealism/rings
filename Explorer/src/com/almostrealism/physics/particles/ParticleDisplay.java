/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.physics.particles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

public class ParticleDisplay extends JPanel {
	private static final int NUM_PARTICLES = 3000;
	private static final int PWIDTH = 512;
	private static final int PHEIGHT = 512;
	private static final int BOUNDSIZE = 500;
	private static final Point3d USERPOSN = new Point3d(0, 20, 20);
	
	private SimpleUniverse univ;
	private BranchGroup scene;
	private BoundingSphere bounds;

	public static void main(String args[]) {
		int tot = NUM_PARTICLES;

		if (args.length > 0) {
			try 
			{ tot = Integer.parseInt( args[0] ); }
			catch(NumberFormatException e)
			{ System.out.println("Illegal number of particles"); }

			if (tot < 0) {
				System.out.println("Number of particles must be positive");
				tot = NUM_PARTICLES;
			}
		}

		JFrame f = new JFrame("Particles");
		f.getContentPane().add(new ParticleDisplay(tot));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(512, 512);
		f.setVisible(true);
//		f.setState(Frame.MAXIMIZED_BOTH);
	}

	public ParticleDisplay(int tot) {
		super.setLayout(new BorderLayout());
		super.setOpaque(false);
		super.setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

		GraphicsConfiguration config =
			SimpleUniverse.getPreferredConfiguration();
		Canvas3D canvas3D = new Canvas3D(config);
		super.add("Center", canvas3D);
		canvas3D.setFocusable(true);
		canvas3D.requestFocus();
		
		this.univ = new SimpleUniverse(canvas3D);

		createSceneGraph(tot);
		initUserPosition();
		orbitControls(canvas3D);

		univ.addBranchGraph(this.scene);
	}
	
	private void createSceneGraph(int tot) {
		this.scene = new BranchGroup();
		this.bounds = new BoundingSphere(new Point3d(0, 0, 0), BOUNDSIZE);   
		
		lightScene();
//		addBackground();
//		this.scene.addChild(new CheckerFloor().getBG());
		
		ParticleSystem particles = new ParticleSystem(tot, 20);
		
		TransformGroup pos = new TransformGroup();
		Transform3D trans = new Transform3D();
		trans.setTranslation(new Vector3d(0.0f, 0.0f, 1.0f));
		pos.setTransform(trans);
		pos.addChild(particles); 
		this.scene.addChild(pos);
		
		Behavior behavior = particles.getParticleControl();
		behavior.setSchedulingBounds(bounds);
		this.scene.addChild(behavior);
		
		this.scene.compile();
	}
	
	private void lightScene() {
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		
		AmbientLight ambientLightNode = new AmbientLight(white);
		ambientLightNode.setInfluencingBounds(bounds);
		this.scene.addChild(ambientLightNode);
		
		Vector3f light1Direction  = new Vector3f(-1.0f, -1.0f, -1.0f);
		Vector3f light2Direction  = new Vector3f(1.0f, -1.0f, 1.0f);

		DirectionalLight light1 = new DirectionalLight(white, light1Direction);
		light1.setInfluencingBounds(bounds);
		this.scene.addChild(light1);

		DirectionalLight light2 = new DirectionalLight(white, light2Direction);
		light2.setInfluencingBounds(bounds);
		this.scene.addChild(light2);
	}
	
	private void addBackground() {
		Background back = new Background();
		back.setApplicationBounds( bounds );
		back.setColor(0.17f, 0.65f, 0.92f);
		this.scene.addChild(back);
	}
	
	private void orbitControls(Canvas3D c) {
		OrbitBehavior orbit = new OrbitBehavior(c, OrbitBehavior.REVERSE_ALL);
		orbit.setSchedulingBounds(bounds);
		
		ViewingPlatform vp = this.univ.getViewingPlatform();
		vp.setViewPlatformBehavior(orbit);
	}
	
	private void initUserPosition() {
		ViewingPlatform vp = this.univ.getViewingPlatform();
		TransformGroup steerTG = vp.getViewPlatformTransform();

		Transform3D t3d = new Transform3D();
		steerTG.getTransform(t3d);
		
		t3d.lookAt(USERPOSN, new Point3d(0, 10, 10), new Vector3d(0, 1, 0));
		t3d.invert();

		steerTG.setTransform(t3d);
	}
}