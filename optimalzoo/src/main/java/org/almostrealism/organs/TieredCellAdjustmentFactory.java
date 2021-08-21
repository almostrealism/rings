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

package org.almostrealism.organs;

import org.almostrealism.graph.Adjustment;
import org.almostrealism.heredity.Gene;

public class TieredCellAdjustmentFactory<G, T> implements CellAdjustmentFactory<G, T> {
	private final CellAdjustmentFactory<G, T> tier;
	private final TieredCellAdjustmentFactory<G, ?> parent;

	public TieredCellAdjustmentFactory(CellAdjustmentFactory<G, T> tier) {
		this(tier, null);
	}
	
	public TieredCellAdjustmentFactory(CellAdjustmentFactory<G, T> tier, CellAdjustmentFactory<G, ?> parent) {
		this(tier, (TieredCellAdjustmentFactory) (parent instanceof TieredCellAdjustmentFactory ? parent : new TieredCellAdjustmentFactory(parent, null)));
	}
	
	public TieredCellAdjustmentFactory(CellAdjustmentFactory<G, T> tier, TieredCellAdjustmentFactory<G, ?> parent) {
		this.tier = tier;
		this.parent = parent;
	}

	@Override
	public Adjustment<T> generateAdjustment(Gene<G> gene) { return this.tier.generateAdjustment(gene); }
	
	public TieredCellAdjustmentFactory<G, ?> getParent() { return parent; }
}
