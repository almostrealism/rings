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

import javax.media.opengl.GL2;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableGeometry;
import com.almostrealism.renderable.RenderableSurfaceFactory;

/**
 * A {@link Replicant} combines a set of {@link BasicGeometry}s
 * with a {@link Surface}. The resulting {@link SurfaceGroup} is
 * a collection of {@link Surface}s for each {@link BasicGeometry},
 * with the transformations of the {@link BasicGeometry} applied.
 * 
 * @author  Michael Murray
 */
public class Replicant extends SurfaceGroup implements Renderable {
	private Surface surface;
	private Renderable delegate;
	private Iterable<BasicGeometry> geo;
	
	protected Replicant() { }
	
	public Replicant(Iterable<BasicGeometry> n) {
		setGeometry(n);
	}
	
	public void setSurface(Surface s) {
		this.surface = s;
		this.delegate = RenderableSurfaceFactory.createRenderableSurface(s);
	}
	
	protected void setGeometry(Iterable<BasicGeometry> n) {
		this.geo = n;
	}
	
	@Override
	public void init(GL2 gl) { if (delegate != null) delegate.init(gl); }
	
	@Override
	public void display(GL2 gl) {
		for (BasicGeometry g : geo) {
			gl.glPushMatrix();
			RenderableGeometry.applyTransform(gl, g);
			// TODO  Inherit surface color, etc?
			delegate.display(gl);
			gl.glPopMatrix();
		}
	}
}
