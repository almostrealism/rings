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

package net.sf.j3d.ui.displays;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.io.FileEncoder;
import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.engine.Scene;

import net.sf.j3d.util.graphics.GraphicsConverter;
import net.sf.j3d.util.graphics.RGB;


/**
 * @author Mike Murray
 */
public class RenderTestFrame extends JPanel {
  private Scene scene;
  private RenderParameters par;
  private int w, h, ssd;
  private ProgressDisplay prog;
  private Image image;
  
  private JFrame frame;
  	
  	/**
  	 * Constructs a RenderTestFrame for the specified Scene. The height of the image
  	 * produced is calculated based on the dimensions of the camera that is used by
  	 * the scene.
  	 * 
  	 * @param s  Scene to render.
  	 * @param w  Width of image to produce.
  	 * @param ssd  Dimension for super sampling. Total super samples will be this value squared.
  	 */
	public RenderTestFrame(Scene s, int w, int ssd) {
		this.scene = s;
		
		OrthographicCamera c = (OrthographicCamera)this.scene.getCamera();
		
		this.w = w;
		this.ssd = ssd;
		this.h = (int)(c.getProjectionHeight() * (w / c.getProjectionWidth()));
		
		this.par = new RenderParameters(0, 0, this.w, this.h, this.w, this.h, this.ssd, this.ssd);
		
		this.prog = new ProgressDisplay(this. w * this.h / 100, this.w * this.h);
		super.add(this.prog);
		
		this.frame = new JFrame("Render Test");
		this.frame.getContentPane().add(this);
		this.frame.setSize(this.w, this.h + 20);
	}
	
	public RenderParameters getRenderParameters() { return this.par; }
	
	public void render(String s) {
		try {
			this.frame.setVisible(true);
		} catch (Exception e) {
			System.out.println(e);
		}
		
		RGB rgb[][] = RayTracingEngine.render(this.scene, this.par, this.prog);
		this.image = GraphicsConverter.convertToAWTImage(rgb);
		
		super.remove(this);
		
		try {
			System.out.print("Writing image file: ");
			FileEncoder.encodeImageFile(rgb, new File(s + ".jpg"), FileEncoder.JPEGEncoding);
			System.out.println("Done");
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		
		super.repaint();
	}
	
	public void render() { this.render("test"); }
	
	public Image getImage() { return this.image; }
	
	public void paint(Graphics g) {
		if (this.image == null) {
			super.paint(g);
		} else {
			g.setColor(super.getBackground());
			g.fillRect(0, 0, super.getWidth(), super.getHeight());
			
			g.drawImage(this.image, 0, 0, this);
		}
	}
}
