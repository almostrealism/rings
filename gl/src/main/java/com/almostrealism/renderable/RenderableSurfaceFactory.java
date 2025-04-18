/*
 * Copyright 2024 Michael Murray
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

package com.almostrealism.renderable;

import org.almostrealism.io.Console;
import org.almostrealism.space.Mesh;
import org.almostrealism.space.ShadableSurface;

import org.almostrealism.primitives.Sphere;
import org.almostrealism.space.ShadableSurfaceWrapper;
import org.almostrealism.space.SurfaceGroup;

public class RenderableSurfaceFactory {
	public static Renderable createRenderableSurface(ShadableSurface s) {
		if (s instanceof Renderable) {
			return (Renderable) s;
		} else if (s instanceof ShadableSurfaceWrapper) {
			return createRenderableSurface(((ShadableSurfaceWrapper) s).getSurface());
		} else if (s instanceof Sphere) {
			return new RenderableSphere((Sphere) s);
		} else if (s instanceof Mesh) {
			return new RenderableMesh((Mesh) s);
		} else if (s instanceof SurfaceGroup) {
			GLRenderableList l = new GLRenderableList();

			for (ShadableSurface sh : ((SurfaceGroup) s).getSurfaces()) {
				l.getRenderables().add(createRenderableSurface(sh));
			}

			return l;
		} else {
			Console.root().features(RenderableSurfaceFactory.class)
					.warn("Returning null for " + s);
		}
		
		return null;
	}
}
