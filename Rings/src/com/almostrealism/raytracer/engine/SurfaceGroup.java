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

/*
 * Copyright (C) 2004-16  Mike Murray
 * All rights reserved.
 */

package com.almostrealism.raytracer.engine;

import java.util.ArrayList;

import org.almostrealism.space.Intersection;
import org.almostrealism.space.Ray;
import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;

import com.almostrealism.projection.Intersections;
import com.almostrealism.rayshade.ShaderParameters;
import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.raytracer.primitives.Triangle;

/**
 * A {@link SurfaceGroup} object allows {@link ShadableSurface} objects to be grouped together.
 * The properties of the {@link SurfaceGroup} object are applied to each of its children.
 * 
 * @author Mike Murray
 */
public class SurfaceGroup extends AbstractSurface {
	private ArrayList<ShadableSurface> surfaces;

	/**
	 * Constructs a SurfaceGroup object with no Surface objects.
	 */
	public SurfaceGroup() {
		surfaces = new ArrayList<ShadableSurface>();
		setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	 * Constructs a SurfaceGroup object using the Surface objects in the specified array.
	 */
	public SurfaceGroup(ShadableSurface surfaces[]) {
		this();
		this.setSurfaces(surfaces);
	}
	
	/**
	 * Replaces all of the Surface objects of this SurfaceGroup object with those represented by the specified Surface array.
	 */
	public void setSurfaces(ShadableSurface surfaces[]) {
		this.surfaces.clear();
		
		for (int i = 0; i < surfaces.length; i++)
			this.addSurface(surfaces[i]);
	}
	
	/**
	 * Adds the specified Surface object to this SurfaceGroup object and sets its parent
	 * to this SurfaceGroup object (if it is an instance of AbstractSurface).
	 */
	public void addSurface(ShadableSurface surface) {
		if (surface instanceof AbstractSurface)
			((AbstractSurface) surface).setParent(this);
//		else if (surface instanceof AbstractSurfaceUI)
//			((AbstractSurfaceUI)surface).setParent(this);
		
		this.surfaces.add(surface);
	}
	
	/**
	 * Removes the Surface object stored at the specified index from this SurfaceGroup object
	 * and sets the parent of the removed Surface object to null (if it is an instance of AbstractSurface).
	 */
	public void removeSurface(int index) {
		if (this.surfaces.get(index) instanceof AbstractSurface)
			((AbstractSurface) this.surfaces.get(index)).setParent(null);
		
		this.surfaces.remove(index);
	}
	
	/**
	 * Returns the {@link ShadableSurface} objects stored by this {@link SurfaceGroup} object as
	 * a {@link ShadableSurface} array.
	 */
	public ShadableSurface[] getSurfaces() {
		return this.surfaces.toArray(new ShadableSurface[0]);
	}
	
	/**
	 * Returns the {@link ShadableSurface} object stored by this {@link SurfaceGroup} object at
	 * the specified index.
	 */
	public ShadableSurface getSurface(int index) {
		return this.surfaces.get(index);
	}
	
	/** {@link ShadableSurface#shade(ShaderParameters)} */
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
		
		i: for (int i = 0; i < this.surfaces.size(); i++) {
			if (this.surfaces.get(i) instanceof AbstractSurface == false) continue i;
			
			Mesh m = ((AbstractSurface) this.surfaces.get(i)).triangulate();
			
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
		
		for(int i = 0; i < this.surfaces.size(); i++) {
			if (this.surfaces.get(i).intersect(ray) == true)
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
		
		return Intersections.closestIntersection(ray, surfaces);
	}
}
