/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.audio;

import io.almostrealism.code.ProducerComputation;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.DefaultWaveCellData;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CellList extends ArrayList<Cell<PackedCollection<?>>> implements Cells {
	private List<CellList> parents;
	private List<Receptor<PackedCollection<?>>> roots;
	private List<Setup> setups;
	private TemporalList requirements;
	private List<Runnable> finals;

	public CellList(CellList... parents) {
		this(Arrays.asList(parents));
	}

	public CellList(List<CellList> parents) {
		this.parents = parents;
		this.roots = new ArrayList<>();
		this.setups = new ArrayList<>();
		this.requirements = new TemporalList();
		this.finals = new ArrayList<>();
	}

	public CellList addSetup(Setup setup) { this.setups.add(setup); return this; }

	public void addRoot(Cell<PackedCollection<?>> c) {
		roots.add(c);
		add(c);
	}

	public CellList addRequirement(Temporal t) {
		requirements.add(t);
		return this;
	}

	public <T extends Temporal> CellList addRequirements(T... t) {
		requirements.addAll(Arrays.asList(t));
		return this;
	}

	public CellList map(IntFunction<Cell<PackedCollection<?>>> dest) {
		return map(this, dest);
	}

	public CellList and(CellList cells) {
		return cells(this, cells);
	}

	public CellList[] branch(IntFunction<Cell<PackedCollection<?>>>... dest) {
		return branch(this, dest);
	}

	public CellList poly(IntFunction<ProducerComputation<PackedCollection<?>>> decision) {
		CellList l = poly(1, () -> null, decision,
				stream().map(c -> (Function<DefaultWaveCellData, CollectionTemporalCellAdapter>) data -> (CollectionTemporalCellAdapter) c).toArray(Function[]::new));
		// TODO  By dropping the parent, we may be losing necessary dependencies
		// TODO  However, if it is included, operations will be invoked multiple times
		// TODO  Since the new polymorphic cells delegate to the operations of the
		// TODO  original cells in this current CellList
		// l.setParent(this);
		return l;
	}

	public CellList gr(double duration, int segments, IntUnaryOperator choices) {
		return gr(this, duration, segments, choices);
	}

	public CellList grid(double duration, int segments, IntToDoubleFunction choices) {
		return grid(this, duration, segments, choices);
	}

	public CellList grid(double duration, int segments, IntFunction<Producer<PackedCollection<?>>> choices) {
		return grid(this, duration, segments, choices);
	}

	public CellList f(IntFunction<Factor<PackedCollection<?>>> filter) {
		return f(this, filter);
	}

	public CellList d(IntFunction<Producer<Scalar>> delay) { return d(this, delay); }

	public CellList d(IntFunction<Producer<Scalar>> delay, IntFunction<Producer<Scalar>> scale) { return d(this, delay, scale); }

	public CellList m(IntFunction<Cell<PackedCollection<?>>> adapter) {
		return m(this, adapter);
	}

	public CellList m(IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(this, adapter, transmission);
	}

	public CellList m(List<Cell<PackedCollection<?>>> adapter, List<Cell<PackedCollection<?>>> destinations) {
		return m(this, adapter, destinations);
	}

	public CellList m(List<Cell<PackedCollection<?>>> adapter, List<Cell<PackedCollection<?>>> destinations, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(this, adapter, destinations, transmission);
	}

	public CellList mself(List<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return mself(this, adapter, transmission);
	}

	public CellList m(IntFunction<Cell<PackedCollection<?>>> adapter, List<Cell<PackedCollection<?>>> destinations) {
		return m(this, adapter, destinations);
	}

	public CellList m(IntFunction<Cell<PackedCollection<?>>> adapter, List<Cell<PackedCollection<?>>> destinations, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(this, adapter, destinations, transmission);
	}

	public CellList mself(IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return mself(this, adapter, transmission);
	}

	public CellList mself(IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Gene<PackedCollection<?>>> transmission, IntFunction<Cell<PackedCollection<?>>> passthrough) {
		return mself(this, adapter, transmission, passthrough);
	}

	public CellList m(IntFunction<Cell<PackedCollection<?>>> adapter, IntFunction<Cell<PackedCollection<?>>> destinations, IntFunction<Gene<PackedCollection<?>>> transmission) {
		return m(this, adapter, destinations, transmission);
	}

	public CellList sum() { return sum(this); }

	public Supplier<Runnable> export(PackedCollection<PackedCollection<?>> destinations) {
		return export(this, destinations);
	}

	public CellList mixdown(double seconds) { return mixdown(this, seconds); }

	public CellList csv(IntFunction<File> f) {
		return csv(this, f);
	}

	public CellList o(IntFunction<File> f) {
		return o(this, f);
	}

	public CellList om(IntFunction<File> f) {
		return om(this, f);
	}

	public List<CellList> getParents() { return parents; }

	public TemporalList getRequirements() { return requirements; }

	public List<Runnable> getFinals() { return finals; }

	public Collection<Cell<PackedCollection<?>>> getAll() {
		List<Cell<PackedCollection<?>>> all = new ArrayList<>();
		parents.stream().map(CellList::getAll).flatMap(Collection::stream).forEach(c -> append(all, c));
		forEach(c -> append(all, c));

		return all;
	}

	public TemporalList getAllTemporals() {
		TemporalList all = new TemporalList();
		parents.stream().map(CellList::getAllTemporals).flatMap(Collection::stream).forEach(c -> append(all, c));

		stream().map(c -> c instanceof Temporal ? (Temporal) c : null)
				.filter(Objects::nonNull).forEach(t -> append(all, t));

		requirements.forEach(c -> append(all, c));

		return all;
	}

	public List<Setup> getAllSetup() {
		List<Setup> all = new ArrayList<>();
		parents.stream().map(CellList::getAllSetup).flatMap(Collection::stream).forEach(c -> append(all, c));

		stream().map(c -> c instanceof Setup ? (Setup) c : null)
				.filter(Objects::nonNull).forEach(t -> append(all, t));

		requirements.stream().map(c -> c instanceof Setup ? (Setup) c : null)
				.filter(Objects::nonNull).forEach(c -> append(all, c));

		setups.stream().forEach(s -> append(all, s));

		return all;
	}

	public Collection<Receptor<PackedCollection<?>>> getAllRoots() {
		List<Receptor<PackedCollection<?>>> all = new ArrayList<>();
		parents.stream().map(CellList::getAllRoots).flatMap(Collection::stream).forEach(c -> append(all, c));
		roots.forEach(c -> append(all, c));

		return all;
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList("CellList Setup");
		getAllSetup().stream().map(Setup::setup).forEach(setup::add);
		return setup;
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("CellList Tick");
		getAllRoots().stream().map(r -> r.push(c(0.0))).forEach(tick::add);
		tick.add(getAllTemporals().tick());
		return tick;
	}

	@Override
	public void reset() {
		finals.forEach(Runnable::run);
		parents.forEach(CellList::reset);
		forEach(Cell::reset);
		requirements.reset();
	}

	public static Collector<Cell<PackedCollection<?>>, ?, CellList> collector() {
		return Collectors.toCollection(CellList::new);
	}
}
