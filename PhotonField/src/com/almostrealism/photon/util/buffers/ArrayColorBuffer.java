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

package com.almostrealism.photon.util.buffers;

import java.io.IOException;

import org.almostrealism.color.AverageColor;
import org.almostrealism.color.RGB;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.network.PhotonFieldSceneLoader;
import com.almostrealism.raytracer.io.FileDecoder;

public class ArrayColorBuffer implements ColorBuffer {
	public Absorber absorber;
	public double position[];
	boolean first;
	
	public int colorDepth = 48;
	private RGB front[][], back[][];
	private double m = 1.0, k = 1.0;
	private boolean invertV = false, direct = false;
	
	public void setColorBufferSize(int w, int h, double m) {
		this.front = new RGB[w][h];
		this.back = new RGB[w][h];
		this.m = m;
	}
	
	public int[] getColorBufferDimensions() {
		if (this.front == null)
			return new int[2];
		else
			return new int[] {this.front.length, this.front[0].length};
	}
	
	public void setScale(double m) { this.m = m; }
	public double getScale() { return this.m; }
	
	public void clear() {
		this.front = new RGB[this.front.length][this.front[0].length];
		this.back = new RGB[this.back.length][this.back[0].length];
	}
	
	public void store(PhotonFieldSceneLoader loader, String name) throws IOException {
//		TODO  Need to store images
//		ImageCanvas.writeImage(this.front, loader.getOutputStream("-" + name + "-front.rgb"),
//								ImageCanvas.RGBListEncoding);
//		ImageCanvas.writeImage(this.back, loader.getOutputStream("-" + name + "-back.rgb"),
//								ImageCanvas.RGBListEncoding);
	}
	
	public void load(PhotonFieldSceneLoader loader, String name) throws IOException {
		FileDecoder.readRGBList(loader.getInputStream("-" + name + "-front.rgb"), this.front);
		FileDecoder.readRGBList(loader.getInputStream("-" + name + "-back.rgb"), this.back);
	}
	
	public RGB getColorAt(double u, double v, boolean front) {
		if (front && this.front == null) return null;
		if (!front && this.back == null) return null;
		if (direct && this.k == 0.0) return null;
		
		if (direct && this.invertV) v = 1.0 - v;
		
		RGB rgb[][] = null;
		
		if (front)
			rgb = this.front;
		else
			rgb = this.back;
		
		int x = (int) (u * rgb.length);
		if (x >= rgb.length) x = rgb.length - 1;
		int y = (int) (v * rgb[x].length);
		if (y >= rgb[x].length) y = rgb[x].length - 1;
		
		double l1 = (1.0 / rgb.length);
		double l2 = (1.0 / rgb[x].length);
		
		double pu = u - (x + 0.5) * l1;
		double pv = v - (y + 0.5) * l2;
		
		AverageColor c = new AverageColor();
		c.setInvert(true);
		double a, b, b2;
		
		b = l2 - pv;
		b2 = b * b;
		
		if (x > 0 && y > 0) {
			a = l1 + pu;
			c.addColor(a * a + b2, rgb[x - 1][y - 1]);
		}
		
		if (y > 0) {
			a = pu;
			c.addColor(a * a + b2, rgb[x][y - 1]);
		}
		
		if (x < rgb.length - 1 && y > 0) {
			a = l1 - pu;
			c.addColor(a * a + b2, rgb[x + 1][y - 1]);
		}
		
		b = pv;
		b2 = b * b;
		
		if (x > 0) {
			a = l1 + pu;
			c.addColor(a * a + b2, rgb[x - 1][y]);
		}
		
		a = pu;
		c.addColor(a * a + b2, rgb[x][y]);
		
		if (x < rgb.length - 1) {
			a = l1 - pu;
			c.addColor(a * a + b2, rgb[x + 1][y]);
		}
		
		b = l2 + pv;
		b2 = b * b;
		
		if (x > 0 && y < rgb[x].length - 1) {
			a = l1 + u;
			c.addColor(a * a + b2, rgb[x - 1][y + 1]);
		}
		
		if (y < rgb.length - 1) {
			a = u;
			c.addColor(a * a + b2, rgb[x][y + 1]);
		}
		
		if (x < rgb.length - 1 && y < rgb[x].length - 1) {
			a = l1 - u;
			c.addColor(a * a + b2, rgb[x + 1][y + 1]);
		}
		
		if (this.first) {

//			TODO  Need to store images
			/*
			try {
				String f = this.absorber + "-front.ppm";
				System.out.println("AbsorberHashSet: Writing Surface Color Map to " + f);
				ImageCanvas.encodeImageFile(this.front, new File(f),
											ImageCanvas.PPMEncoding);
				f = this.absorber + "-back.ppm";
				System.out.println("AbsorberHashSet: Writing Surface Color Map to " + f);
				ImageCanvas.encodeImageFile(this.back, new File(f),
											ImageCanvas.PPMEncoding);
			} catch (IOException e) {
				System.out.println("AbsorberHashSet: " + e.getMessage());
									
			}
			*/
			
			this.first = false;
		}
		
		if (direct && this.k != 1.0)
			return c.evaluate(null).multiply(this.k);
		else
			return c.evaluate(null);
	}
	
	public void addColor(double u, double v, boolean front, RGB c) {
		if (front && this.front == null) return;
		if (!front && this.back == null) return;
		
		RGB rgb[][] = null;
		
		if (front)
			rgb = this.front;
		else
			rgb = this.back;
		
		if (u >= 1.0 || v >= 1.0 || u < 0.0 || v < 0.0) {
			System.out.println("AbsorberHashSet: Surface coords from " +
								this.absorber + " " + u + ", " + v);
			return;
		}
		
		int x = (int) (u * rgb.length);
		int y = (int) (v * rgb[x].length);
		
		c.multiplyBy(this.m);
		
		if (rgb[x][y] == null)
			rgb[x][y] = c;
		else
			rgb[x][y].addTo(c);
	}
}