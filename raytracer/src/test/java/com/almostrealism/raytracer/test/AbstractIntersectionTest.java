/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.raytracer.test;

import com.almostrealism.primitives.SphereIntersectAt;
import com.almostrealism.projection.PinholeCameraRayAt;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Vector;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.CodeFeatures;

public class AbstractIntersectionTest implements HardwareFeatures, CodeFeatures {
	protected final int width = 400, height = 400;

	protected SphereIntersectAt combined() {
		Vector viewDirection = new Vector(0.0, 0.0,  -1.0);
		Vector upDirection = new Vector(0.0, 1.0, 0.0);

		Vector w = (viewDirection.divide(viewDirection.length())).minus();

		Vector u = upDirection.crossProduct(w);
		u.divideBy(u.length());

		Vector v = w.crossProduct(u);

		return
				new SphereIntersectAt(new PinholeCameraRayAt((Producer) v(Pair.class, 0),
						pair(width, height),
						new Vector(0.0, 0.0, 5.0),
						new Pair(1.0, 1.0),
						0.0, 1.0,
						u, v, w));
	}
}
