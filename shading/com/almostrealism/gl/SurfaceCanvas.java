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

package com.almostrealism.gl;

import com.almostrealism.raytracer.SurfaceAddEvent;
import com.almostrealism.raytracer.SurfaceRemoveEvent;
import com.almostrealism.raytracer.event.SurfaceEvent;
import com.almostrealism.renderable.GLDriver;
import com.almostrealism.renderable.RenderableGeometry;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableSurfaceFactory;
import com.jogamp.opengl.GL2;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventListener;

import java.util.Iterator;

public class SurfaceCanvas extends DefaultGLCanvas implements EventListener {
	private Scene<ShadableSurface> scene;
	
	public SurfaceCanvas(Scene<ShadableSurface> scene) {
		this.scene = scene;
	}
	
	@Override
	public PinholeCamera getCamera() { return (PinholeCamera) scene.getCamera(); }

	@Override
	protected void initRenderables(GLDriver gl) {
		renderables.clear();
		
		for (ShadableSurface s : scene) {
			renderables.add(RenderableSurfaceFactory.createRenderableSurface(s));
		}
		
		for (Renderable r : renderables) r.init(gl);
	}

	@Override
	public void eventFired(Event event) {
		// TODO  Handle light events

		System.out.println("SurfaceCanvas.eventFired");

		if (event instanceof SurfaceAddEvent) {
			add(RenderableSurfaceFactory.createRenderableSurface(((SurfaceEvent) event).getTarget()));
		} else if (event instanceof SurfaceRemoveEvent) {
			Iterator<Renderable> itr = renderables.iterator();

			while (itr.hasNext()) {
				Renderable r = itr.next();

				if (r instanceof RenderableGeometry &&
						((RenderableGeometry) r).getGeometry() == ((SurfaceEvent) event).getTarget()) {
					itr.remove();
				}
			}
		}
	}
}
