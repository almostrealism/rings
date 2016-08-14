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

/*
 * Copyright (C) 2007  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.raytracer.loaders;

import java.io.IOException;

import com.almostrealism.flow.db.Client;
import com.almostrealism.raytracer.SceneFactory;
import com.almostrealism.raytracer.engine.Scene;
import com.almostrealism.raytracer.io.FileDecoder;
import com.almostrealism.raytracer.network.SceneLoader;

/**
 * The PlySceneLoader loads a PLY model from the distributed database
 * and places it in a default scene.
 * 
 * @author  Mike Murray
 */
public class GtsSceneLoader implements SceneLoader {
	public static final double scale = 100.0;
	
	// TODO  Add use of exception listener.
	/**
	 * @see com.almostrealism.raytracer.SceneLoader#loadScene(java.lang.String)
	 */
	public Scene loadScene(String uri) throws IOException {
		Scene scene = FileDecoder.decodeScene(Client.getCurrentClient().getServer().loadResource(uri),
												FileDecoder.GTSEncoding, null);
		scene.setLights(SceneFactory.getStandard3PointLightRig(scale));
		return scene;
	}
}