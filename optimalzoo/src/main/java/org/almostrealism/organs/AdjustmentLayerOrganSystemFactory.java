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

package org.almostrealism.organs;

import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Genome;
import org.almostrealism.time.Temporal;

import java.util.List;

public class AdjustmentLayerOrganSystemFactory<G, O, A, R> implements GeneticTemporalFactory<G, O, AdjustmentLayerOrganSystem<G, O, A, R>> {
	private final TieredCellAdjustmentFactory<G, A> adjustmentFactory;
	private final GeneticTemporalFactory<G, R, Temporal> parentLayer;

	public AdjustmentLayerOrganSystemFactory(TieredCellAdjustmentFactory<G, A> adjustmentFactory, GeneticTemporalFactory simpleFactory) {
		this.adjustmentFactory = adjustmentFactory;

		if (adjustmentFactory.getParent() == null) {
			parentLayer = simpleFactory;
		} else {
			parentLayer = new AdjustmentLayerOrganSystemFactory(adjustmentFactory.getParent(), simpleFactory);
		}
	}

	@Override
	public AdjustmentLayerOrganSystem<G, O, A, R> generateOrgan(Genome<G> genome, List<? extends Receptor<O>> measures, Receptor<O> output) {
		Temporal suborg = parentLayer.generateOrgan(genome.getHeadSubset(), (List) measures, (Receptor) output); //  TODO Looks like O must equal R?
		return new AdjustmentLayerOrganSystem(suborg, adjustmentFactory,
											genome.getLastChromosome(), null);
	}
}
