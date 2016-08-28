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

package com.almostrealism.replicator.geometry;

import java.util.List;

import org.almostrealism.space.BasicGeometry;

import com.jogamp.opengl.GL2;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableGeometry;
import com.almostrealism.renderable.RenderableSurfaceFactory;

/**
 * A {@link Replicant} combines a set of {@link BasicGeometry}s with the
 * {@link ShadableSurface}s in the {@link SurfaceGroup}. The resulting
 * {@link SurfaceGroup} functions as a collection of {@link ShadableSurface}s
 * for each {@link BasicGeometry}, with the transformations of the
 * {@link BasicGeometry} applied. {@link Replicant} also handles the
 * creation of an Open GL render delegate for each surface using
 * {@link RenderableSurfaceFactory}.
 * 
 * @author  Michael Murray
 */
public class Replicant<T extends ShadableSurface> extends SurfaceGroup<T> implements Renderable {
	private List<Renderable> delegates;
	private Iterable<BasicGeometry> geo;
	
	protected Replicant() { }
	
	public Replicant(Iterable<BasicGeometry> n) {
		setGeometry(n);
	}
	
	public void addSurface(T s) {
		super.addSurface(s);
		this.delegates.add(RenderableSurfaceFactory.createRenderableSurface(s));
	}
	
	public void removeSurface(int index) {
		super.removeSurface(index);
		this.delegates.remove(index);
	}
	
	protected void setGeometry(Iterable<BasicGeometry> n) { this.geo = n; }
	
	@Override
	public void init(GL2 gl) {
		for (Renderable delegate : delegates) {
			if (delegate != null) delegate.init(gl);
		}
	}
	
	@Override
	public void display(GL2 gl) {
		for (BasicGeometry g : geo) {
			gl.glPushMatrix();
			RenderableGeometry.applyTransform(gl, g);
			// TODO  Inherit surface color, etc?
			for (Renderable delegate : delegates) {
				if (delegate != null) delegate.display(gl);
			}
			gl.glPopMatrix();
		}
	}
}
