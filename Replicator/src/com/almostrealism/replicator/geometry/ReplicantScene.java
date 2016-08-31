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

import org.almostrealism.space.Surface;
import org.almostrealism.space.Vector;
import org.almostrealism.uml.ModelEntity;

import com.almostrealism.lighting.StandardLightingRigs;
import com.almostrealism.projection.ThinLensCamera;
import com.almostrealism.raytracer.Scene;
import com.almostrealism.raytracer.engine.ShadableSurface;
import com.almostrealism.raytracer.engine.SurfaceGroup;

/**
 * {@link ReplicantScene} stores for rendering all the {@link Replicant}s in a
 * {@link SurfaceGroup}. The {@link Scene} hierarchy is flattened when a new
 * {@link ReplicantScene} is construced, making the {@link Scene} into a simple
 * list of {@link Replicant}s. This flattening does not side effect the
 * {@link SurfaceGroup} that is passed to the constructor.
 * 
 * @author  Michael Murray
 */
@ModelEntity
public class ReplicantScene extends Scene<Replicant, ThinLensCamera> {
	
	public ReplicantScene(SurfaceGroup<ShadableSurface> g) {
		addReplicants(this, g);
		
		StandardLightingRigs.addDefaultLights(this);
		
		ThinLensCamera c = new ThinLensCamera();
		c.setLocation(new Vector(0.0, 0.0, 10.0));
		c.setViewDirection(new Vector(0.0, 0.0, -1.0));
		c.setProjectionDimensions(c.getProjectionWidth(), c.getProjectionWidth() * 1.6);
		c.setFocalLength(0.05);
		c.setFocus(10.0);
		c.setLensRadius(0.2);
		setCamera(c);
	}
	
	private static void addReplicants(ReplicantScene scene, SurfaceGroup<ShadableSurface> g) {
		for (Surface s : g) {
			if (s instanceof Replicant) {
				scene.add((Replicant) s);
			} else if (s instanceof SurfaceGroup) {
				addReplicants(scene, (SurfaceGroup) s);
			}
		}
	}
}
