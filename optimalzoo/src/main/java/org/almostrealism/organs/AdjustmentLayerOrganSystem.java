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

import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Adjustment;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.Chromosome;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.OperationList;

public class AdjustmentLayerOrganSystem<G, O, A, R> implements OrganSystem<O> {
	public static final boolean enableAdjustment = false;

	private String name;

	private final Organ<O> adjustable;
	private final AdjustmentLayer<A, R> adjust;

	public AdjustmentLayerOrganSystem(Organ<O> adjustable, CellAdjustmentFactory<G, A> factory,
									  Chromosome<G> adjustChromosome, Chromosome<O> organChromosome) {
		this.adjustable = adjustable;

		Organ<O> o = adjustable;

		if (o instanceof OrganSystem) {
			o = ((OrganSystem) o).last();
		}

		List<Cell<O>> c = new ArrayList<>();
		List<Adjustment<A>> l = new ArrayList<>();

		for (int i = 0; i < adjustable.size(); i++) {
			c.add(o.getCell(i));
			l.add(factory.generateAdjustment(adjustChromosome.valueAt(i)));
		}
		
		this.adjust = new AdjustmentLayer(c, l, organChromosome);
	}

	@Override
	public String getName() { return name; }

	@Override
	public void setName(String name) { this.name = name; }

	@Override
	public Cell<O> getCell(int index) { return adjustable.getCell(index); }

	@Override
	public Organ<O> getOrgan(int index) {
		if (index >= getDepth()) {
			return (Organ<O>) adjust;
		} else if (adjustable instanceof OrganSystem) {
			return ((OrganSystem) adjustable).getOrgan(index);
		} else {
			return adjustable;
		}
	}

	@Override
	public int size() { return adjustable.size(); }

	@Override
	public void setMonitor(Receptor<O> monitor) {
		this.adjustable.setMonitor(monitor);
	}

	@Override
	public int getDepth() {
		if (adjust instanceof OrganSystem) {
			return ((OrganSystem) adjust).getDepth() + 1;
		}

		return 1;
	}

	@Override
	public Supplier<Runnable> setup() {
		Runnable r = adjustable.setup().get();
		Runnable a = adjust.setup().get();
		return () -> () -> { r.run(); a.run(); };
	}

	@Override
	public Supplier<Runnable> push(Producer<O> protein) {
		OperationList push = new OperationList();
		if (enableAdjustment) push.add(adjust.push(null));
		push.add(adjustable.push(protein));
		return push;
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList();
		if (enableAdjustment) tick.add(adjust.tick());
		tick.add(adjustable.tick());
		return tick;
	}

	@Override
	public void reset() {
		OrganSystem.super.reset();
		adjust.reset();
		adjustable.reset();
	}
}
