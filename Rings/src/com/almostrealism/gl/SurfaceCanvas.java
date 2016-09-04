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

import com.almostrealism.projection.PinholeCamera;
import com.almostrealism.raytracer.Scene;

public class SurfaceCanvas extends DefaultGLCanvas {
	private Scene<?, ? extends PinholeCamera> scene;
	
	public SurfaceCanvas(Scene<?, ? extends PinholeCamera> scene) {
		this.scene = scene;
	}
	
	@Override
	public PinholeCamera getCamera() { return scene.getCamera(); }
}
