/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.audio.optimize;

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.filter.ExponentialAdjustment;
import org.almostrealism.audio.filter.PeriodicAdjustment;
import org.almostrealism.graph.Adjustment;
import org.almostrealism.heredity.Gene;
import org.almostrealism.organs.CellAdjustmentFactory;
import org.almostrealism.CodeFeatures;

import java.util.function.BiFunction;

public class DefaultCellAdjustmentFactory implements CellAdjustmentFactory<Scalar, Scalar>, CodeFeatures {
	private final Type type;
	public DefaultCellAdjustmentFactory(Type type) {
		this.type = type;
	}

	@Override
	public Adjustment<Scalar> generateAdjustment(Gene<Scalar> gene) {
		BiFunction<Integer, Double, Double> g = (i, v) -> gene.valueAt(i).getResultant(v(v.doubleValue())).get().evaluate().getValue();

		if (type == Type.PERIODIC) {
//			double min = Math.min(1.0, g.apply(1, 1.0));
//			double max = min + g.apply(2, 1.0 - min);
//			return new PeriodicAdjustment(g.apply(0, 1.0), new Pair(min, max));

			Producer<Scalar> min = gene.valueAt(1).getResultant(v(1.0)); // TODO  Missing Math.min(1.0, X)
			Producer<Scalar> max = scalarAdd(min, gene.valueAt(2).getResultant(scalarSubtract(v(1.0), min)));
			return new PeriodicAdjustment(gene.valueAt(0).getResultant(v(1.0)), pair(min, max));
		} else if (type == Type.EXPONENTIAL) {
//			double min = Math.min(1.0, g.apply(2, 1.0));
//			double max = min + g.apply(3, 1.0 - min);
			double min = 0.0;
			double max = 1.0;
			return new ExponentialAdjustment(new Pair(g.apply(0, 1.0), g.apply(1, 1.0)), pair(min, max));
		} else {
			throw new IllegalArgumentException();
		}
	}

	public enum Type {
		PERIODIC, EXPONENTIAL
	}
}
