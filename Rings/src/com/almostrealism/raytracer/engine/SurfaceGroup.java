/*
 * Copyright (C) 2004-05  Mike Murray
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

package com.almostrealism.raytracer.engine;

import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.raytracer.primitives.Triangle;
import com.almostrealism.raytracer.shaders.ShaderParameters;

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

/**
 * A {@link SurfaceGroup} object allows {@link Surface} objects to be grouped together.
 * The properties of the {@link SurfaceGroup} object are applied to each of its children.
 * 
 * @author Mike Murray
 */
public class SurfaceGroup extends AbstractSurface {
  private Surface surfaces[];

	/**
	 * Constructs a SurfaceGroup object with no Surface objects.
	 */
	public SurfaceGroup() {
		super.setColor(new RGB(1.0, 1.0, 1.0));
		this.setSurfaces(new Surface[0]);
	}
	
	/**
	 * Constructs a SurfaceGroup object using the Surface objects in the specified array.
	 */
	public SurfaceGroup(Surface surfaces[]) {
		super.setColor(new RGB(1.0, 1.0, 1.0));
		this.setSurfaces(surfaces);
	}
	
	/**
	 * Replaces all of the Surface objects of this SurfaceGroup object with those represented by the specified Surface array.
	 */
	public void setSurfaces(Surface surfaces[]) {
		this.surfaces = new Surface[0];
		
		for (int i = 0; i < surfaces.length; i++)
			this.addSurface(surfaces[i]);
	}
	
	/**
	 * Adds the specified Surface object to this SurfaceGroup object and sets its parent
	 * to this SurfaceGroup object (if it is an instance of AbstractSurface).
	 */
	public void addSurface(Surface surface) {
		Surface newSurfaces[] = new Surface[this.surfaces.length + 1];
		for (int i = 0; i < this.surfaces.length; i++) newSurfaces[i] = this.surfaces[i];
		newSurfaces[newSurfaces.length - 1] = surface;
		
		if (surface instanceof AbstractSurface)
			((AbstractSurface)surface).setParent(this);
//		else if (surface instanceof AbstractSurfaceUI)
//			((AbstractSurfaceUI)surface).setParent(this);
		
		this.surfaces = newSurfaces;
	}
	
	/**
	 * Removes the Surface object stored at the specified index from this SurfaceGroup object
	 * and sets the parent of the removed Surface object to null (if it is an instance of AbstractSurface).
	 */
	public void removeSurface(int index) {
		if (this.surfaces[index] instanceof AbstractSurface)
			((AbstractSurface)this.surfaces[index]).setParent(null);
		
		Surface newSurfaces[] = new Surface[this.surfaces.length - 1];
		for (int i = 0; i < index; i++) newSurfaces[i] = this.surfaces[i];
		for (int i = index + 1; i < this.surfaces.length - (index + 1); i++) newSurfaces[i] = this.surfaces[i];
		
		this.setSurfaces(newSurfaces);
	}
	
	/**
	 * Returns the Surface objects stored by this SurfaceGroup object as a Surface array.
	 */
	public Surface[] getSurfaces() {
		return this.surfaces;
	}
	
	/**
	 * Returns the Surface object stored by this SurfaceGroup object at the specified index.
	 */
	public Surface getSurface(int index) {
		return this.surfaces[index];
	}
	
	/**
	 * @see com.almostrealism.raytracer.engine.Surface#shade(com.almostrealism.raytracer.engine.ShaderParameters)
	 */
	public RGB shade(ShaderParameters p) {
		RGB color = null;
		
		if (super.getShaderSet() != null)
			color = super.getShaderSet().shade(p);
		else
			color = new RGB(0.0, 0.0, 0.0);
		
		if (super.getParent() != null)
			color.addTo(super.getParent().shade(p));
		
		return color;
	}
	
	/**
	 * Returns null.
	 */
	public Vector getNormalAt(Vector point) { return null; }
	
	public Mesh triangulate() {
		Mesh mesh = super.triangulate();
		
		i: for (int i = 0; i < this.surfaces.length; i++) {
			if (this.surfaces[i] instanceof AbstractSurface == false) continue i;
			
			Mesh m = ((AbstractSurface)this.surfaces[i]).triangulate();
			
			Vector v[] = m.getVectors();
			Triangle t[] = m.getTriangles();
			
			int index[] = new int[v.length];
			for (int j = 0; j < index.length; j++) {
				index[j] = mesh.addVector(m.getTransform(true).transformAsLocation(v[j]));
			}
			
			for (int j = 0; j < t.length; j++) {
				Vector tv[] = t[j].getVertices();
				
				int v0 = index[m.indexOf(tv[0])];
				int v1 = index[m.indexOf(tv[1])];
				int v2 = index[m.indexOf(tv[2])];
				
				mesh.addTriangle(v0, v1, v2);
			}
		}
		
		return mesh;
	}
	
	/**
	 * Returns true if the ray represented by the specified Ray object intersects any of the surfaces
	 * represented by this SurfaceGroup object.
	 */
	public boolean intersect(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		for(int i = 0; i < this.surfaces.length; i++) {
			if (this.surfaces[i].intersect(ray) == true)
				return true;
		}
		
		return false;
	}
	
	/**
	 * Returns an Intersection object that represents the ray-surface intersections
	 * for the AbstractSurface object which is intersected closest to the origin of
	 * the ray (>= 0). If there is no intersection >= 0 along the ray, null is returned.
	 */
	public Intersection intersectAt(Ray ray) {
		ray.transform(this.getTransform(true).getInverse());
		
		return RayTracingEngine.closestIntersection(ray, this.surfaces);
	}
}
