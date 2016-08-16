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

import org.almostrealism.space.Ray;
import org.almostrealism.space.Vector;
import org.almostrealism.space.VectorMath;
import org.almostrealism.texture.RGB;

import com.almostrealism.photon.Absorber;
import com.almostrealism.photon.AbsorptionPlane;
import com.almostrealism.photon.Clock;
import com.almostrealism.photon.geometry.Pinhole;
import com.almostrealism.photon.util.Colorable;
import com.almostrealism.photon.util.PriorityQueue;
import com.almostrealism.raytracer.camera.Camera;

public class PinholeCameraAbsorber implements Absorber, Camera {
	private Clock clock;
	
	private Pinhole pinhole;
	private AbsorptionPlane plane;
	private double planePos[];
	
	private Colorable colorable;
	private Vector location;
	
	public PinholeCameraAbsorber() { }
	
	public PinholeCameraAbsorber(double fNum, double focalLength,
								double norm[], double orient[]) {
		this.plane = new AbsorptionPlane();
		this.plane.setSurfaceNormal(VectorMath.clone(norm));
		this.plane.setOrientation(VectorMath.clone(orient));
		
		this.pinhole = new Pinhole();
		this.pinhole.setRadius(focalLength / (2.0 * fNum));
		this.pinhole.setSurfaceNormal(VectorMath.clone(norm));
		this.pinhole.setOrientation(orient);
		
		this.planePos = VectorMath.multiply(norm, -focalLength, true);
	}
	
	public PinholeCameraAbsorber(Pinhole pinhole, AbsorptionPlane plane, double focalLength) {
		this.init(pinhole, plane, focalLength);
	}
	
	public void init(Pinhole pinhole, AbsorptionPlane plane, double focalLength) {
		this.pinhole = pinhole;
		this.plane = plane;
		
		double norm[] = pinhole.getSurfaceNormal();
		this.planePos = VectorMath.multiply(norm, -focalLength, true);
	}
	
	public void setWidth(int w) { this.plane.setWidth(w); }
	public void setHeight(int h) { this.plane.setHeight(h); }
	public int getWidth() { return (int) this.plane.getWidth(); }
	public int getHeight() { return (int) this.plane.getHeight(); }
	public void setPixelSize(double p) { this.plane.setPixelSize(p); }
	public double getPixelSize() { return this.plane.getPixelSize(); }
	
	public AbsorptionPlane getAbsorptionPlane() { return this.plane; }
	
	public void setLocation(Vector p) { this.location = p; }
	public Vector getLocation() { return this.location; }
	
	public void setColorable(Colorable c) { this.colorable = c; }
	
	public Ray rayAt(double i, double j, int screenWidth, int screenHeight) {
		double u = i / (screenWidth);
		double v = (j / screenHeight);
		// v = 1.0 - v;
		
		if (this.colorable != null) {
			int tot = 6;
			RGB c = new RGB(0.0, 0.0, 0.0);
			
			if (this.plane.imageAvailable()) {
				RGB im[][] = this.plane.getImage();
				int a = (int) (u * im.length);
				if (a == im.length) a = im.length -1;
				int b = (int) (v * im[a].length);
				
				int x0 = a - 4;
				int y0 = b - 4;
				int x1 = a + 4;
				int y1 = b + 4;
				
				PriorityQueue q = new PriorityQueue();
				
				i: for (int ai = x0; ai < x1; ai++) {
					if (ai < 0 || ai >= im.length) continue i;
					
					j: for (int aj = y0; aj < y1; aj++) {
						if (aj < 0 || aj >= im[ai].length) continue j;
						if (c.equals(im[ai][aj])) continue j;
						
						double d = (ai - a) * (ai - a) + (aj - b) * (aj - b);
						if (d == 0)
							d = 1.0;
						else
							d = 1 / Math.sqrt(d);
						
						if (q.peek() < d || q.size() < tot) q.put(im[ai][aj], d);
						if (q.size() > tot) q.next();
					}
				}
				
				while (q.size() > 0) {
					double p = q.peek();
					RGB cl = (RGB) q.next();
					c.addTo(cl.multiply(p / tot));
				}
				
				// c = im[a][b];
			}
			
			this.colorable.setColor(c.getRed(), c.getGreen(), c.getBlue());
		}
		
		double x[] = this.plane.getSpatialCoords(new double[] {u, v});
		VectorMath.addTo(x, this.planePos);
		double d[] = VectorMath.multiply(x, -1.0 / VectorMath.length(x), true);
		
		Vector vx = new Vector(x[0], x[1], x[2]);
		Vector vd = new Vector(d[0], d[1], d[2]);
		vx.addTo(this.location);
		
		return new Ray(vx, vd);
	}

	public void setClock(Clock c) { this.clock = c; }
	public Clock getClock() { return this.clock; }
	
	public boolean absorb(double x[], double p[], double energy) {
		if (this.pinhole.absorb(x, p, energy))
			return true;
		else if (this.plane.absorb(VectorMath.subtract(x, this.planePos), p, energy))
			return true;
		else
			return false;
	}

	public double[] emit() { return null; }
	public double getEmitEnergy() { return 0.0; }
	public double[] getEmitPosition() { return null; }
	public double getNextEmit() { return Integer.MAX_VALUE; }
}
