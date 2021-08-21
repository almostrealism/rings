/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.replicator;

import org.almostrealism.algebra.Vector;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.geometry.BasicGeometry;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.util.CodeFeatures;

public class DefaultReplicantFactory<S extends ShadableSurface> implements ReplicantFactory<Double, S>, CodeFeatures {
	@Override
	public Replicant<S> generateReplicant(S surface, Chromosome<Double> c) {
		DefaultReplicant<S> r = new DefaultReplicant<>(surface);

		for (int i = 0; i < c.length(); i++) {
			Gene<Double> g = c.valueAt(i);

			// TODO Extract to factory that produces geometry from a gene?
			BasicGeometry geo = new BasicGeometry();
			geo.setLocation(new Vector(g.valueAt(0).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(1).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(2).getResultant(p(1.0)).get().evaluate()));

			geo.setRotationCoefficients(g.valueAt(3).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(4).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(5).getResultant(p(1.0)).get().evaluate());

			geo.setScaleCoefficients(g.valueAt(6).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(7).getResultant(p(1.0)).get().evaluate(),
										g.valueAt(8).getResultant(p(1.0)).get().evaluate());

			r.put(String.valueOf(i), geo);
		}

		return r;
	}
}
