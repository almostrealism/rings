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

package com.almostrealism.replicator;

import com.almostrealism.gl.GLDriver;
import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableGeometry;
import com.almostrealism.renderable.RenderableSurfaceFactory;
import com.almostrealism.replicator.transform.TransformedSurfaceGroup;
import io.almostrealism.lang.CodePrintWriter;
import org.almostrealism.color.ShadableSurface;
import org.almostrealism.geometry.BasicGeometry;
import org.almostrealism.space.GeometryStack;
import org.almostrealism.space.SurfaceGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
	private final List<Renderable> delegates;
	private Iterable<BasicGeometry> geo;
	private boolean pushed = false;
	
	protected Replicant() { delegates = new ArrayList<>(); }
	
	public Replicant(Iterable<BasicGeometry> n) {
		this();
		setGeometry(n);
	}

	@Override
	public void addSurface(T s) {
		super.addSurface(s);
		this.delegates.add(RenderableSurfaceFactory.createRenderableSurface(s));
	}

	@Override
	public void removeSurface(int index) {
		super.removeSurface(index);
		this.delegates.remove(index);
	}
	
	protected void setGeometry(Iterable<BasicGeometry> n) { this.geo = n; }

	public Iterable<BasicGeometry> geometry() { return geo; }

	@Override
	public Iterator<T> iterator() {
		final Iterator<T> itr = super.iterator();

		return new Iterator<T>() {
			private Iterator<BasicGeometry> gitr;
			private T surface;

			@Override
			public boolean hasNext() {
				return (itr.hasNext() || (gitr != null && gitr.hasNext()));
			}

			@Override
			public T next() {
				if (gitr == null || !gitr.hasNext()) {
					if (surface instanceof GeometryStack) ((GeometryStack) surface).pop();
					pushed = false;
					surface = itr.next();
					gitr = geometry().iterator();
				}

				BasicGeometry g = gitr.next();

				if (surface instanceof GeometryStack) {
					if (pushed) ((GeometryStack) surface).pop();
					((GeometryStack) surface).push(g);
					pushed = true;
				}

				TransformedSurfaceGroup t = new TransformedSurfaceGroup(g);
				t.addSurface(surface);
				return (T) t;
			}
		};
	}
	
	@Override
	public void init(GLDriver gl) {
		for (Renderable delegate : delegates) {
			if (delegate != null) delegate.init(gl);
		}
	}
	
	@Override
	public void display(GLDriver gl) {
		for (BasicGeometry g : geo) {
			gl.pushMatrix();
			RenderableGeometry.applyTransform(gl, g);
			// TODO  Inherit surface color, etc?
			for (Renderable delegate : delegates) {
				if (delegate != null) delegate.display(gl);
			}
			gl.popMatrix();
		}
	}

	public void write(String glMember, String name, CodePrintWriter p) { throw new UnsupportedOperationException("write"); }
}
