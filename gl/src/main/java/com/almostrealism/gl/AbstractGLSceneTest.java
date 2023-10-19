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

import com.almostrealism.lighting.PointLight;
import org.almostrealism.primitives.Sphere;
import com.almostrealism.projection.PinholeCamera;
import org.almostrealism.algebra.Vector;
import org.almostrealism.algebra.ZeroVector;
import org.almostrealism.color.RGB;
import org.almostrealism.space.Scene;
import org.almostrealism.space.ShadableSurface;

public class AbstractGLSceneTest {
	public Scene createTestScene() {
		Scene<ShadableSurface> scene = new Scene<>();
		scene.setCamera(new PinholeCamera());
		scene.add(new Sphere(ZeroVector.getEvaluable().evaluate(), Math.pow(10, 5), new RGB(0.0, 0.0, 1.0)).triangulate());
		scene.getLights().add(new PointLight(Vector.yAxis().multiply((Math.pow(10, 6)))));
		return scene;
	}
}
