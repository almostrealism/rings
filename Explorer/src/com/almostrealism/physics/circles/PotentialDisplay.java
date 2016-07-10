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

package com.almostrealism.physics.circles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

public class PotentialDisplay extends Component implements Runnable, IterationListener {
	private int pixels[];
	private ParticleSet particles;
	
	private MemoryImageSource source;
	private Image image;
	private int w, h;
	private static int blank = 255 << 24;
	private static int scale = (int) ParticleSetDisplay.scale;
	
	private int fps = 100;
	private boolean stop;
	
	public PotentialDisplay(int w, int h, ParticleSet particles) {
		this.particles = particles;
		this.particles.addIterationListener(this);
		
		this.pixels = new int[w * h];
		this.w = w;
		this.h = h;
		
		for (int i = 0; i < this.pixels.length; i++) this.pixels[i] = this.blank;
		this.source = new MemoryImageSource(w, h, pixels, 0, w);
		this.source.setAnimated(true);
		this.image = super.createImage(this.source);
	}
	
	public void setFPS(int fps) { this.fps = fps; }
	public int getFPS() { return this.fps; }
	public void stop() { this.stop = true; }
	
	public void run() {
//		while (!stop) {
//			try {
//				Thread.sleep(1000 / this.fps);
//			} catch (InterruptedException ie) { }
//			
//			this.source.newPixels();
//			this.repaint();
//		}
	}
	
	public void iterationComplete() {
		for (int i = 0; i < this.w; i++) {
			for (int j = 0; j < this.h; j++) {
				int f[] = this.particles.force(i * scale, j * scale, 0);
				double x = f[0] * f[0];
				double y = f[1] * f[1];
				double z = f[2] * f[2];
				int r = Math.min(255, (int) ((x + y + z) / 10));
				
				this.pixels[j * this.h + i] = 255 << 24 | r << 16 | r << 8 | r;
			}
		}
		
		if (this.source != null) {
			this.source.newPixels();
			this.repaint();
		}
	}
	
	public void paint(Graphics g) {
		g.drawImage(this.image, 0, 0, this.w, this.h, this);
	}
	
	public Dimension getPreferredSize() { return new Dimension(this.w, this.h); }
}
