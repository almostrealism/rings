/*
 * Copyright 2023 Michael Murray
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

package com.almostrealism.geometry;

import com.almostrealism.light.PlanarLight;
import com.almostrealism.physics.AbsorberHashSet;
import com.almostrealism.physics.DefaultPhotonField;
import com.almostrealism.physics.SpecularAbsorber;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.Ops;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.physics.Clock;
import org.almostrealism.physics.PhotonField;
import org.almostrealism.primitives.AbsorptionPlane;
import org.almostrealism.primitives.Pinhole;
import org.almostrealism.primitives.Plane;
import org.almostrealism.space.Volume;

import javax.swing.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 * @author Samuel Tepper
 */
public class Box extends HashSet implements Volume<Object>, CodeFeatures {
	public static double verbose = Math.pow(10.0, -3.0);
	
	private Vector[] coords;
	private double width, height, depth, wallThickness;
	private double[] orientation, normal;
	
	public static void main(String[] args) {
		double scale = 50.0;
		
		PlanarLight l = new PlanarLight();
		l.setWidth(scale * 2.0);
		l.setHeight(scale * 2.0);
		l.setSurfaceNormal(new Vector(0.0, 0.0, -1.0));
		l.setOrientation(new Vector(0.0, 1.0, 0.0));
		l.setLightPropagation(false);
		l.setPower(PlanarLight.wattsToEvMsec * 0.01);
		
		//TEST THESE PARAMETERS
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(scale/300.0);
		plane.setWidth(600);
		plane.setHeight(600);
		plane.setThickness(0.05);
		plane.setSurfaceNormal(Ops.o().vector(0.0, 0.0, -1.0));
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});

		//The focal length of the Cornell Box's lense is .035
		//The distance between the Pinhole and the AbsorbtionPlane is the focal length
		//The diameter of the pinhole is given by the equation
		// d = 1.9(sqrt(f*l)) where f is the focal length and l is the avergage wavelength
		Pinhole pinhole = new Pinhole();
		pinhole.setRadius(scale / 1.5);
		pinhole.setThickness(scale / 100.0);
		pinhole.setSurfaceNormal(Ops.o().vector(0.0, 0.0, -1.0));
		pinhole.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		Box box1 = new Box();
		box1.setWidth(scale);
		box1.setHeight(scale);
		box1.setDepth(scale);
		box1.setOrientation(new double[] {0.0, 0.0, 1.0});
		box1.setSurfaceNormal(new double[] {0.0, -1.0, 0.0});
		box1.setWallThickness(scale/100.0);
		box1.makeWalls(true);
		
		SpecularAbsorber spec = new SpecularAbsorber();
		spec.setColorRange(450.0, 50.0);
		
		AbsorberHashSet a = new AbsorberHashSet();
		a.setBound(scale * 10.0);
		a.addAbsorber(spec, ZeroVector.getInstance());
		a.addAbsorber(plane, Ops.o().vector(0.0, 0.0, scale * 2.2));
		// a.addAbsorber(pinhole, new double[] {0.0, 0.0, scale * 2.0});
		a.addAbsorber(l, Ops.o().vector(0.0, 0.0, scale * 1.95));
		
		PhotonField f = new DefaultPhotonField();
		f.setAbsorber(a);
		
		Clock c = new Clock();
		c.addPhotonField(f);
		a.setClock(c);
		
		JFrame frame = new JFrame("Box Test");
		frame.getContentPane().add(plane.getDisplay());
		frame.setSize(150, 150);
		frame.setVisible(true);
		
		long start = System.currentTimeMillis();
		
		while (true) {
			c.tick().get().run();
			
			if (Math.random() < Box.verbose) {
				int rate = (int) ((System.currentTimeMillis() - start) /
									(60 * 60000 * c.getTime()));
				
				System.out.println("[" + c.getTime() + "]: " + rate +
									" hours per microsecond.");
				
				try {
					plane.saveImage("box-sim.ppm");
				} catch (IOException ioe) {
					System.out.println("BlackBody: Could not write image (" +
										ioe.getMessage() + ")");
				}
			}
		}
		
	}
	
	public void setWidth(double x){
		this.width = x;
	}
	
	public void setHeight(double x){
		this.height = x;
	}
	
	public void setDepth(double x){
		this.depth = x;
	}
	public void setOrientation(double[] x){
		//Sets the orientation for the bottom of the box
		this.orientation = x;
	}
	
	public void setSurfaceNormal(double[] x){
		//Sets the normal for the bottom of the box.
		this.normal = x;
	}
	public void setWallThickness(double x){
		this.wallThickness = x;
	}
	
	public void makeWalls(boolean complex){
		
		// super.setBound(Math.max(Math.max(this.height, this.width), this.depth));
		
		if (complex = true){
			Plane bottom = new Plane();
			// SpecularAbsorber BT = new SpecularAbsorber();
			bottom.setWidth(this.width);
			bottom.setHeight(this.depth);
			bottom.setOrientation(this.orientation);
			bottom.setSurfaceNormal(value(new Vector(this.normal)));
			bottom.setThickness(this.wallThickness);
			// BT.setVolume(bottom);
			// BT.setColorRange(this.startColor, this.range);
			// super.addAbsorber(BT,new double[] {0.0, -.5*this.height, 0.0});
			super.add(bottom);

			Plane top = new Plane();
			// SpecularAbsorber TP = new SpecularAbsorber();
			top.setWidth(this.width);
			top.setHeight(this.depth);
			top.setOrientation(this.orientation);
			top.setSurfaceNormal(value(new Vector(this.normal).minus()));
			top.setThickness(this.wallThickness);
			// TP.setVolume(top);
			// TP.setColorRange(this.startColor, this.range);
			// super.addAbsorber(TP, new double[] {0.0, .5*this.height, 0.0});
			super.add(top);

			Plane side1 = new Plane();
			// SpecularAbsorber S1 = new SpecularAbsorber();
			side1.setWidth(this.depth);
			side1.setHeight(this.height);
			Vector bottomNormal = (Vector) bottom.getSurfaceNormal().get().evaluate();
			side1.setSurfaceNormal(value(new Vector(bottom.getOrientation()).crossProduct(bottomNormal)));
			side1.setOrientation(bottomNormal.crossProduct(new Vector(bottom.getAcross())).toArray());
			side1.setThickness(this.wallThickness);
			// S1.setVolume(side1);
			// S1.setColorRange(this.startColor, this.range);
			// super.addAbsorber(S1, new double[] {-.5*this.width, 0.0, 0.0});
			super.add(side1);
			
			Plane side2 = new Plane();
//			SpecularAbsorber S2 = new SpecularAbsorber();
			side2.setWidth(this.width);
			side2.setHeight(this.height);
			side2.setSurfaceNormal(value(new Vector(bottom.getOrientation()).minus()));
			side2.setOrientation(bottomNormal.minus().toArray());
			side2.setThickness(this.wallThickness);
//			S2.setVolume(side2);
//			S2.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S2, new double[] {0.0, 0.0, -.5*this.depth});
			super.add(side2);

			Plane side3 = new Plane();
//			SpecularAbsorber S3 = new SpecularAbsorber();
			side3.setWidth(this.depth);
			side3.setHeight(this.height);
			side3.setSurfaceNormal(value(((Vector) side1.getSurfaceNormal().get().evaluate()).minus()));
			side3.setOrientation(side1.getOrientation());
			side3.setThickness(this.wallThickness);
//			S3.setVolume(side3);
//			S3.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S3, new double[] {.5*this.width, 0.0, 0.0});
			super.add(side3);
			
			Plane side4 = new Plane();
//			SpecularAbsorber S4 = new SpecularAbsorber();
			side4.setWidth(this.width);
			side4.setHeight(this.height);
			side4.setSurfaceNormal(value(((Vector) side2.getSurfaceNormal().get().evaluate()).minus()));
			side4.setOrientation(side2.getOrientation());
			side4.setThickness(this.wallThickness);
//			S4.setVolume(side4);
//			S4.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S4, new double[] {0.0, 0.0, .5*this.depth});
			super.add(side4);
		}
//		else {
//			Plane TopBottom = new Plane();
//			SpecularAbsorber TB = new SpecularAbsorber();
//			TopBottom.setWidth(this.width);
//			TopBottom.setHeight(this.depth);
//			TopBottom.setSurfaceNormal(this.normal);
//			TopBottom.setOrientation(this.orientation);
//			TopBottom.setThickness(this.height);
//			TB.setVolume(TopBottom);
//			TB.setColorRange(this.startColor, this.range);
//			super.addAbsorber(TB, new double[] {this.width/2.0, this.height/2.0, this.depth/2.0});
//			
//			Plane S13 = new Plane();
//			SpecularAbsorber S1 = new SpecularAbsorber();
//			S13.setWidth(this.wallThickness);
//			S13.setHeight(this.height);
//			S13.setThickness(this.width);
//			S13.setSurfaceNormal(VectorMath.cross(this.normal, this.orientation));
//			S13.setOrientation(VectorMath.multiply(this.normal, -1.0));
//			S1.setVolume(S13);
//			S1.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S1, new double[] {this.width/2.0, this.height/2.0, this.depth/2.0});
//			
//			Plane S24 = new Plane();
//			SpecularAbsorber S2 = new SpecularAbsorber();
//			S24.setWidth(this.width);
//			S24.setHeight(this.height);
//			S24.setThickness(this.depth);
//			S24.setOrientation(VectorMath.multiply(this.normal, -1.0));
//			S24.setSurfaceNormal(VectorMath.multiply(this.orientation, -1.0));
//			S2.setVolume(S24);
//			S2.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S2, new double[] {this.width/2.0, this.height/2.0, this.depth/2.0});
//		}
		
		this.coords = new Vector[6];
		
		this.coords[0] = new Vector(0.0, -0.5 * this.height, 0.0);
		this.coords[1] = new Vector(0.0, 0.5 * this.height, 0.0);
		this.coords[2] = new Vector(-0.5 * this.width, 0.0, 0.0);
		this.coords[3] = new Vector(0.0, 0.0, -0.5 * this.depth);
		this.coords[4] = new Vector(0.5 * this.width, 0.0, 0.0);
		this.coords[5] = new Vector(0.0, 0.0, .5 * this.depth);
	}
	
	
	public double[] getOrientation() { return this.orientation; }
	public double getWidth() { return this.width; }
	public double getHeight() { return this.height; }
	public double getDepth() {return this.depth; }

	@Override
	public Producer getValueAt(Producer point) {
		return null;
	}

	@Override
	public Producer<PackedCollection> getNormalAt(Producer<PackedCollection> x) {
		return () -> args -> {
			Iterator it = iterator();
			Plane lowest = (Plane) it.next();
			double d = Double.MAX_VALUE;

			//The plane which produces the smallest dot product between the plane's normal
			//and the vector v between the point and the center of the box is the closest plane,
			//so just take the normal from that plane.

			int tot = 0;

			w:
			while (it.hasNext()) {
				Plane current = (Plane) it.next();
				Vector xVec = new Vector(x.get().evaluate(args), 0);
				if (!current.inside(v(xVec))) continue w;
				Vector n = new Vector(current.getNormalAt(x).get().evaluate(args), 0);
				double cd = Math.abs(xVec.dotProduct(n));

				tot++;

				if (cd < d) {
					lowest = current;
					d = cd;
				}

			}

			if (Math.random() < Box.verbose) {
				System.out.println("Box: Selected " + lowest + " from " + tot + " planes.");
				System.out.println("Box: Normal is " + lowest.getNormalAt(x).get().evaluate(args));
			}

			return lowest.getNormalAt(x).get().evaluate(args);
		};
	}

	@Override
	public boolean inside(Producer<PackedCollection> x) {
		int i = 0;

		Iterator itr = this.iterator();

		while (itr.hasNext()){
			Vector p = new Vector(x.get().evaluate(), 0).subtract(coords[i]);
			if (((Plane) itr.next()).inside(v(p))) return true;
			i++;
		}

		return false;
	}

	@Override
	public double intersect(Vector p, Vector d) {
		Iterator itr = this.iterator();
		
		double l = Double.MAX_VALUE - 1.0;
		int i = 0;
		
		while (itr.hasNext()){
			double xl = ((Plane) itr.next()).intersect(p.subtract(coords[i]), d);
			if (xl < l) l = xl;
			i++;
		}
		
		return l;
	}

	@Override
	public double[] getSpatialCoords(double[] uv) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] getSurfaceCoords(Producer<PackedCollection> xyz) {
		// TODO Auto-generated method stub
		return null;
	}
}
