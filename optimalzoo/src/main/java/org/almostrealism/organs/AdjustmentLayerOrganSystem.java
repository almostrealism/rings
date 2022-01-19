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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.almostrealism.code.Setup;
import io.almostrealism.uml.Lifecycle;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Adjustment;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.time.Temporal;

@Deprecated
public class AdjustmentLayerOrganSystem<G, O, A, R> implements LayeredTemporal<O> {
	public static final boolean enableAdjustment = false;

	private final Temporal adjustable;
	private final AdjustmentLayer<A, R> adjust;

	public AdjustmentLayerOrganSystem(Temporal adjustable, CellAdjustmentFactory<G, A> factory,
									  Chromosome<G> adjustChromosome, Chromosome<O> organChromosome) {
		this.adjustable = adjustable;

		Temporal o = adjustable;

		if (o instanceof LayeredTemporal) {
			o = ((LayeredTemporal) o).last();
		}

		List<Cell<O>> c = new ArrayList<>();
		List<Adjustment<A>> l = new ArrayList<>();

		for (int i = 0; i < ((List) adjustable).size(); i++) {
			c.add((Cell) ((List) o).get(i));
			l.add(factory.generateAdjustment(adjustChromosome.valueAt(i)));
		}
		
		this.adjust = new AdjustmentLayer(c, l, organChromosome);
	}

	public Cell<O> getCell(int index) { return ((SimpleOrgan) adjustable).getCell(index); }

	@Override
	public Temporal getOrgan(int index) {
		if (index >= getDepth()) {
			return adjust;
		} else if (adjustable instanceof LayeredTemporal) {
			return ((LayeredTemporal) adjustable).getOrgan(index);
		} else {
			return adjustable;
		}
	}

	public int size() { return ((SimpleOrgan) adjustable).size(); }

	public void setMonitor(Receptor<O> monitor) {
		((SimpleOrgan) this.adjustable).setMonitor(monitor);
	}

	@Override
	public int getDepth() {
		if (adjust instanceof LayeredTemporal) {
			return ((LayeredTemporal) adjust).getDepth() + 1;
		}

		return 1;
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList("AdjustmentLayerOrganSystem Setup");
		setup.add(((Setup) adjustable).setup());
		setup.add(adjust.setup());
		return setup;
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("AdjustmentLayerOrganSystem Tick");
		if (enableAdjustment) tick.add(adjust.tick());
		tick.add(adjustable.tick());
		return tick;
	}

	@Override
	public void reset() {
		LayeredTemporal.super.reset();
		adjust.reset();
		((Lifecycle) adjustable).reset();
	}
}
