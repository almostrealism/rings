package com.almostrealism.replicator.geometry;

import javax.media.opengl.GL2;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.engine.SurfaceGroup;
import com.almostrealism.visualize.primitives.RenderableSurfaceFactory;
import com.almostrealism.visualize.renderable.Renderable;

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
			// TODO  Push matrix
			delegate.display(gl);
			// TODO  Pop matrix
		}
	}
}
