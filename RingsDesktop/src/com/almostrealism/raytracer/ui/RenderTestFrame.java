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

package com.almostrealism.raytracer.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.almostrealism.space.Scene;
import org.almostrealism.swing.displays.ImageCanvas;
import org.almostrealism.swing.displays.ProgressDisplay;
import org.almostrealism.texture.GraphicsConverter;
import org.almostrealism.texture.RGB;

import com.almostrealism.raytracer.camera.OrthographicCamera;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.RenderParameters;


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
			e.printStackTrace();
		}
		
		RGB rgb[][] = RayTracingEngine.render(this.scene, this.par, this.prog);
		this.image = GraphicsConverter.convertToAWTImage(rgb);
		
		super.remove(this);
		
		try {
			System.out.print("Writing image file: ");
			ImageCanvas.encodeImageFile(rgb, new File(s + ".jpg"), ImageCanvas.JPEGEncoding);
			System.out.println("Done");
		} catch (IOException ioe) {
			ioe.printStackTrace();
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
