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
 *  Research done under professor Jim Fix Reed College 2005-06.
 *  Supervision is property of the College.
 */

package net.sf.j3d.raytracer.constructives;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;

import net.sf.j3d.raytracer.engine.AbstractSurface;
import net.sf.j3d.raytracer.engine.Surface;
import net.sf.j3d.util.TransformMatrix;
import net.sf.j3d.util.Vector;


/**
 * @author Mike Murray
 */
public class LSystem {
	public static final String STEP = "step";
	public static final String FORWARD = "forward";
	public static final String BACKWARD = "backward";
	public static final String LEFT = "left";
	public static final String RIGHT = "right";
	public static final String PUSH = "push";
	public static final String POP = "pop";
	
	public static interface SurfaceFactory { public AbstractSurface next(AbstractSurface current); }
	public static interface Statement { public String[] evaluate(Object o); }
	
	private Hashtable rules;
	private SurfaceFactory factory;
	private Vector left, right, forward, backward;
	
	public LSystem(Hashtable rules) {
		this.rules = rules;
		this.setAngle(Math.toRadians(30));
	}
	
	public void setAngle(double angle) {
		this.left = new Vector(0.0, 0.0, -angle);
		this.right = new Vector(0.0, 0.0, angle);
		this.forward = new Vector(angle, 0.0, 0.0);
		this.backward = new Vector(-angle, 0.0, 0.0);
	}
	
	public void setLeft(Vector left) { this.left = left; }
	public void setRight(Vector right) { this.right = right; }
	public void setForward(Vector forward) { this.forward = forward; }
	public void setBackward(Vector backward) { this.backward = backward; }
	
	public Vector getLeft() { return this.left; }
	public Vector getRight() { return this.right; }
	public Vector getForward() { return this.forward; }
	public Vector getBackward() { return this.backward; }
	
	public void setSurfaceFactory(SurfaceFactory f) { this.factory = f; }
	public SurfaceFactory getSurfaceFactory() { return this.factory; }
	
	public List generate(List init, int itr) {
		if (itr == 0) return init;
		
		List l = new ArrayList();
		
		for (int i = 0; i < init.size(); i++) {
			Object o = init.get(i);
			
			if (this.rules.containsKey(o)) {
				Object r[];
				
				if (this.rules.get(o) instanceof Statement) {
					r = ((Statement) this.rules.get(o)).evaluate(o);
				} else {
					r = (Object[]) this.rules.get(o);
				}
				
				for (int j = 0; j < r.length; j++) l.add(r[j]);
			} else {
				l.add(o);
			}
		}
		
		return this.generate(l, --itr);
	}
	
	public Surface[] generate(Object data[], Vector d) {
		List s = new ArrayList();
		
		double dl = d.length();
		AbstractSurface base = this.factory.next(null);
		Vector p = base.getLocation();
		Stack pstack = new Stack(), dstack = new Stack();
		
		i: for (int i = 0; i < data.length; i++) {
			if (data[i].equals(LSystem.STEP)) {
				p = p.add(d.multiply(dl / d.length()));
			} else if (data[i].equals(LSystem.FORWARD)) {
				TransformMatrix mx = TransformMatrix.createRotateXMatrix(this.forward.getX());
				TransformMatrix my = TransformMatrix.createRotateYMatrix(this.forward.getY());
				TransformMatrix mz = TransformMatrix.createRotateZMatrix(this.forward.getZ());
				
				d = (Vector) d.clone();
				
				mx.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				my.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				mz.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				
				continue i;
			} else if (data[i].equals(LSystem.BACKWARD)) {
				TransformMatrix mx = TransformMatrix.createRotateXMatrix(this.backward.getX());
				TransformMatrix my = TransformMatrix.createRotateYMatrix(this.backward.getY());
				TransformMatrix mz = TransformMatrix.createRotateZMatrix(this.backward.getZ());
				
				d = (Vector) d.clone();
				
				mx.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				my.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				mz.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				
				continue i;
			} else if (data[i].equals(LSystem.LEFT)) {
				TransformMatrix mx = TransformMatrix.createRotateXMatrix(this.left.getX());
				TransformMatrix my = TransformMatrix.createRotateYMatrix(this.left.getY());
				TransformMatrix mz = TransformMatrix.createRotateZMatrix(this.left.getZ());
				
				d = (Vector) d.clone();
				
				mx.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				my.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				mz.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				
				continue i;
			} else if (data[i].equals(LSystem.RIGHT)) {
				TransformMatrix mx = TransformMatrix.createRotateXMatrix(this.right.getX());
				TransformMatrix my = TransformMatrix.createRotateYMatrix(this.right.getY());
				TransformMatrix mz = TransformMatrix.createRotateZMatrix(this.right.getZ());
				
				d = (Vector) d.clone();
				
				mx.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				my.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				mz.transform(d, TransformMatrix.TRANSFORM_AS_OFFSET);
				
				continue i;
			} else if (data[i].equals(LSystem.PUSH)) {
				pstack.push(p.clone());
				dstack.push(d.clone());
				continue i;
			} else if (data[i].equals(LSystem.POP)) {
				p = (Vector) pstack.pop();
				d = (Vector) dstack.pop();
				continue i;
			} else {
				System.out.println("Encountered non-terminal: " + data[i]);
				continue i;
			}
			
			AbstractSurface next = this.factory.next(base);
			next.setLocation(p);
			
			s.add(next);
			base = next;
		}
		
		return (Surface[]) s.toArray(new Surface[0]);
	}
	
	public static String print(Object data[]) {
		StringBuffer b = new StringBuffer();
		
		for (int i = 0; i < data.length; i++) {
			if (data[i].equals(LSystem.STEP)) {
				b.append("S");
			} else if (data[i].equals(LSystem.FORWARD)) {
				b.append("F");
			} else if (data[i].equals(LSystem.BACKWARD)) {
				b.append("B");
			} else if (data[i].equals(LSystem.LEFT)) {
				b.append("L");
			} else if (data[i].equals(LSystem.RIGHT)) {
				b.append("R");
			} else if (data[i].equals(LSystem.PUSH)) {
				b.append("[");
			} else if (data[i].equals(LSystem.POP)) {
				b.append("]");
			} else {
				b.append(data[i]);
			}
		}
		
		return b.toString();
	}
}
