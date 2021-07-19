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

import com.almostrealism.projection.CameraPositioner;
import com.almostrealism.event.SurfaceAddEvent;
import com.almostrealism.event.SurfaceRemoveEvent;
import com.almostrealism.event.SurfaceEvent;
import com.almostrealism.renderable.RenderableGeometry;
import com.jogamp.opengl.util.texture.Texture;
import org.almostrealism.algebra.Vector;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

import com.almostrealism.renderable.Renderable;
import com.almostrealism.renderable.RenderableSurfaceFactory;
import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventListener;

import java.util.Iterator;

public class SurfaceCanvas extends DefaultGLCanvas implements EventListener {
	public SurfaceCanvas(GLScene s) { super(s, null);}

	public SurfaceCanvas(Scene<ShadableSurface> scene) {
		super(new GLScene(scene), null);
	}

	public SurfaceCanvas(Scene<ShadableSurface> scene, Texture skydome) {
		super(new GLScene(scene), skydome);
	}

	public SurfaceCanvas(Scene<ShadableSurface> scene, ClassLoader scope,
						 String basename, String suffix, boolean mipmapped) {
		super(new GLScene(scene), scope, basename, suffix, mipmapped);
	}

	@Override
	public void eventFired(Event event) {
		// TODO  Handle light events

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

	public void autoPositionCamera() {
		autoPositionCamera(new Vector(0,0,-1));
	}

	public void autoPositionCamera(Vector cameraDirection) {
		CameraPositioner cameraPositioner = new CameraPositioner(getCamera(), getScene(), cameraDirection);
		getCamera().setLocation(cameraPositioner.getLocation());
		getCamera().setViewingDirection(cameraPositioner.getViewingDirection());
		if (gl != null) { gl.setCamera(getCamera()); }
	}
}
