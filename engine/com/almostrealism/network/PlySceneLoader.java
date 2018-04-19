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

package com.almostrealism.network;

import java.io.IOException;

import io.flowtree.node.Client;
import org.almostrealism.graph.Mesh;
import org.almostrealism.graph.io.PlyResource;
import org.almostrealism.space.Scene;

import com.almostrealism.raytracer.SceneFactory;

/**
 * The {@link PlySceneLoader} loads a PLY model from the distributed database
 * and places it in a default scene.
 * 
 * @author  Michael Murray
 */
public class PlySceneLoader implements SceneLoader {
	public static final double scale = 100.0;
	
	/**
	 * @see SceneLoader#loadScene(java.lang.String)
	 */
	public Scene<Mesh> loadScene(String uri) throws IOException {
		Scene<Mesh> s = new Scene<Mesh>();
		PlyResource r = ((PlyResource) Client.getCurrentClient().getServer().loadResource(uri));
		PlyResource.MeshReader reader = new PlyResource.MeshReader();
		s.add(reader.transcode(r).getMesh());
		s.setLights(SceneFactory.getStandard3PointLightRig(scale));
		return s;
	}
}
