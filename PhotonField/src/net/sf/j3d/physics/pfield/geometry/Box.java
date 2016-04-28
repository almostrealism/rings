/* * Copyright (C) 2006  Almost Realism Software Group
*
*  All rights reserved.
*  This document may not be reused without
*  express written permission from Mike Murray.
*  
*  @author Samuel Tepper
**/


package net.sf.j3d.physics.pfield.geometry;

import net.sf.j3d.physics.pfield.AbsorberHashSet;
import net.sf.j3d.physics.pfield.AbsorptionPlane;
import net.sf.j3d.physics.pfield.Clock;
import net.sf.j3d.physics.pfield.DefaultPhotonField;
import net.sf.j3d.physics.pfield.PhotonField;
import net.sf.j3d.physics.pfield.Volume;
import net.sf.j3d.physics.pfield.light.PlanarLight;
import net.sf.j3d.physics.pfield.util.VectorMath;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JFrame;

import net.sf.j3d.physics.pfield.SpecularAbsorber;

public class Box extends HashSet implements Volume {
	public static double verbose = Math.pow(10.0, -3.0);
	
	private double coords[][];
	private double width, height, depth, wallThickness;
	private double[] orientation, normal;
	
	public static void main(String[] args){
		
		double scale = 50.0;
		
		PlanarLight l = new PlanarLight();
		l.setWidth(scale * 2.0);
		l.setHeight(scale * 2.0);
		l.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		l.setOrientation(new double[] {0.0, 1.0, 0.0});
		l.setLightPropagation(false);
		l.setPower(PlanarLight.wattsToEvMsec * 0.01);
		
		//TEST THESE PARAMETERS
		AbsorptionPlane plane = new AbsorptionPlane();
		plane.setPixelSize(scale/300.0);
		plane.setWidth(600);
		plane.setHeight(600);
		plane.setThickness(0.05);
		plane.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
		plane.setOrientation(new double[] {0.0, 1.0, 0.0});
		
		//The focal length of the Cornell Box's lense is .035
		//The distance between the Pinhole and the AbsorbtionPlane is the focal length
		//The diameter of the pinhole is given by the equation
		// d = 1.9(sqrt(f*l)) where f is the focal length and l is the avergage wavelength
		Pinhole pinhole = new Pinhole();
		pinhole.setRadius(scale/1.5);
		pinhole.setThickness(scale / 100.0);
		pinhole.setSurfaceNormal(new double[] {0.0, 0.0, -1.0});
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
		a.addAbsorber(spec, new double[] {0.0, 0.0, 0.0});
		a.addAbsorber(plane, new double[] {0.0, 0.0, scale * 2.2});
		// a.addAbsorber(pinhole, new double[] {0.0, 0.0, scale * 2.0});
		a.addAbsorber(l, new double[] {0.0, 0.0, scale * 1.95});
		
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
			c.tick();
			
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
	public void setOrientation(double x[]){
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
			bottom.setSurfaceNormal(this.normal);
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
			top.setSurfaceNormal(VectorMath.multiply(this.normal, -1.0));
			top.setThickness(this.wallThickness);
			// TP.setVolume(top);
			// TP.setColorRange(this.startColor, this.range);
			// super.addAbsorber(TP, new double[] {0.0, .5*this.height, 0.0});
			super.add(top);

			Plane side1 = new Plane();
			// SpecularAbsorber S1 = new SpecularAbsorber();
			side1.setWidth(this.depth);
			side1.setHeight(this.height);
			side1.setSurfaceNormal(VectorMath.cross(bottom.getOrientation(), bottom.getSurfaceNormal()));
			side1.setOrientation(VectorMath.cross(bottom.getSurfaceNormal(), bottom.getAcross()));
			side1.setThickness(this.wallThickness);
			// S1.setVolume(side1);
			// S1.setColorRange(this.startColor, this.range);
			// super.addAbsorber(S1, new double[] {-.5*this.width, 0.0, 0.0});
			super.add(side1);
			
			Plane side2 = new Plane();
//			SpecularAbsorber S2 = new SpecularAbsorber();
			side2.setWidth(this.width);
			side2.setHeight(this.height);
			side2.setSurfaceNormal(VectorMath.multiply(bottom.getOrientation(), -1.0));
			side2.setOrientation(VectorMath.multiply(bottom.getSurfaceNormal(), -1.0));
			side2.setThickness(this.wallThickness);
//			S2.setVolume(side2);
//			S2.setColorRange(this.startColor, this.range);
//			super.addAbsorber(S2, new double[] {0.0, 0.0, -.5*this.depth});
			super.add(side2);

			Plane side3 = new Plane();
//			SpecularAbsorber S3 = new SpecularAbsorber();
			side3.setWidth(this.depth);
			side3.setHeight(this.height);
			side3.setSurfaceNormal(VectorMath.multiply(side1.getSurfaceNormal(), -1.0));
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
			side4.setSurfaceNormal(VectorMath.multiply(side2.getSurfaceNormal(), -1.0));
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
		
		this.coords = new double[6][0];
		
		this.coords[0] = new double[] {0.0, -0.5 * this.height, 0.0};
		this.coords[1] = new double[] {0.0, 0.5*this.height, 0.0};
		this.coords[2] = new double[] {-0.5 * this.width, 0.0, 0.0};
		this.coords[3] = new double[] {0.0, 0.0, -0.5 * this.depth};
		this.coords[4] = new double[] {0.5 * this.width, 0.0, 0.0};
		this.coords[5] = new double[] {0.0, 0.0, .5*this.depth};
	}
	
	
	public double[] getOrientation(){ return this.orientation; }
	public double getWidth(){ return this.width; }
	public double getHeight(){ return this.height; }
	public double getDepth() {return this.depth; }
	
	public double[] getNormal(double x[]){
		Iterator it = this.iterator();
		Plane lowest = (Plane) it.next();
		double d = Double.MAX_VALUE;
		
		//The plane which produces the smallest dot product between the plane's normal
		//and the vector v between the point and the center of the box is the closest plane,
		//so just take the normal from that plane.
		
		int tot = 0;
		
		w: while (it.hasNext()){
			Plane current = (Plane) it.next();
			if (!current.inside(x)) continue w;
			double n[] = current.getNormal(x);
			double cd = Math.abs(VectorMath.dot(x, n));
			
			tot++;
			
			if (cd < d){
				lowest = current;
				d = cd;
			}

		}
		
		if (Math.random() < Box.verbose) {
			System.out.println("Box: Selected " + lowest + " from " + tot + " planes.");
			System.out.println("Box: Normal is " + lowest.getNormal(x));
		}
		
		return lowest.getNormal(x);
	}
	
	public boolean inside(double x[]){
		int i = 0;
		
		Iterator itr = this.iterator();
		
		while (itr.hasNext()){
			double p[] = VectorMath.subtract(x, this.coords[i]);
			if (((Plane)itr.next()).inside(p)) return true;
			i++;
		}
		
		return false;
	}
	
	public double intersect(double p[], double d[]) {
		Iterator itr = this.iterator();
		
		double l = Double.MAX_VALUE - 1.0;
		int i = 0;
		
		while (itr.hasNext()){
			double x[] = VectorMath.subtract(p, this.coords[i]);
			double xl = ((Plane)itr.next()).intersect(x, d);
			if (xl < l) l = xl;
			i++;
		}
		
		return l;
	}

	public double[] getSpatialCoords(double[] uv) {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] getSurfaceCoords(double[] xyz) {
		// TODO Auto-generated method stub
		return null;
	}

}
