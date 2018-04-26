/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.gl;

import com.almostrealism.projection.OrthographicCamera;
import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableSurfaceFactory;
import io.almostrealism.code.CodePrintWriter;
import org.almostrealism.algebra.Camera;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO  It may be unnecessary to have this class and {@link com.almostrealism.renderable.RenderableList}
 */
public class GLScene extends ArrayList<Renderable> implements Renderable {
	public static final boolean verbose = false;

	private Scene<ShadableSurface> scene;
	private List<ShadableSurface> added;

	public GLScene(Scene<ShadableSurface> s) {
		this.scene = s;
		this.added = new ArrayList<>();
	}

	public Scene<ShadableSurface> getScene() { return scene; }

	public int getFPS() { return 80; } // TODO  Should be assignable

	public OrthographicCamera getCamera() { return (OrthographicCamera) getScene().getCamera(); }

	@Override
	public void init(GLDriver gl) {
		s: for (ShadableSurface s : this.scene) {
			if (this.added.contains(s)) continue s;
			add(RenderableSurfaceFactory.createRenderableSurface(s));
			added.add(s);
		}

		List<Renderable> rs = new ArrayList<>();
		rs.addAll(this);

		for (Renderable r : rs) r.init(gl);
	}

	@Override
	public void display(GLDriver gl) {
		List<Renderable> rs = new ArrayList<>();
		rs.addAll(this);

		r: for (Renderable r : rs) {
			if (r == null) continue r;
			if (verbose) System.out.println("Rendering " + r);
			gl.pushCamera();
			gl.pushLighting();
			gl.setLighting(new GLLightingConfiguration(getScene().getLights()));
			gl.setCamera(getCamera());
			r.display(gl);
			gl.popLighting();
			gl.popCamera();
			if (verbose) System.out.println("Done rendering " + r);
		}
	}

	@Override
	public void write(String glMember, String name, CodePrintWriter p) {
		int index = 0;
		for (Renderable r : this) { r.write(glMember, name + (index++), p); }
	}
}
