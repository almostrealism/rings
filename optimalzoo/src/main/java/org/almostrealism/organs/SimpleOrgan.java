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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.almostrealism.graph.Cell;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.graph.CellPair;
import org.almostrealism.graph.FilteredCell;
import org.almostrealism.graph.MultiCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.IdentityFactor;
import io.almostrealism.relation.Producer;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalList;

public class SimpleOrgan<T> implements Organ<T> {
	private String name;

	private List<Cell<T>> inputLayer;
	private List<Cell<T>> processingLayer;
	private Chromosome<T> transmission;
	private TemporalList temporals;
	
	protected SimpleOrgan() { }
	
	public SimpleOrgan(List<Cell<T>> inputLayer, List<Cell<T>> processingLayer, Chromosome<T> transmission) {
		this(inputLayer, processingLayer, transmission, IdentityFactor.chromosome(transmission.length(), 1));
	}

	public SimpleOrgan(List<Cell<T>> inputLayer, List<Cell<T>> processingLayer, Chromosome<T> transmission, Chromosome<T> filter) {
		init(inputLayer, processingLayer, transmission, i -> new FilteredCell<>(filter.valueAt(i).valueAt(0)));
	}
	
	protected void init(List<Cell<T>> inputLayer, List<Cell<T>> processingLayer, Chromosome<T> transmission, IntFunction<Cell<T>> adapters) {
		if (inputLayer != null && inputLayer.size() != processingLayer.size()) {
			throw new IllegalArgumentException("Input and processing layers must have the same number of cells");
		}

		this.inputLayer = inputLayer;
		this.processingLayer = processingLayer;
		this.transmission = transmission;
		this.temporals = new TemporalList();
		createPairs(adapters);
	}

	@Override
	public String getName() { return name; }

	@Override
	public void setName(String name) { this.name = name; }

	private void createPairs(IntFunction<Cell<T>> adapters) {
		if (transmission == null) {
			System.out.println("WARN: " + getClass().getSimpleName() +
					" has no chromosome and will not have pairs created");
		}

		List<CellPair<T>> pairs = new ArrayList<>();
		for (AtomicInteger i = new AtomicInteger(); i.get() < processingLayer.size(); i.incrementAndGet()) {
			Cell<T> source = Optional.ofNullable(inputLayer).map(l -> l.get(i.get())).orElse(null);
			Cell<T> processing = processingLayer.get(i.get());
			
			if (source instanceof Temporal) {
				this.temporals.add((Temporal) source);
			} else if (source != null) {
				System.out.println("WARN: " + source.getClass().getSimpleName() +
						" is not a Temporal and will not have tick() triggered");
			}

			if (processing instanceof Temporal) {
				this.temporals.add((Temporal) processing);
			} else {
				System.out.println("WARN: " + processing.getClass().getSimpleName() +
						" is not a Temporal and will not have tick() triggered");
			}

			if (source != null) source.setReceptor(processing);

			if (transmission != null) {
				Cell<T> adapter = adapters.apply(i.get());

				if (adapter instanceof Temporal) {
					temporals.add((Temporal) adapter);
				}

				pairs.add(MultiCell.split(processing, adapter, processingLayer, transmission.valueAt(i.get())));
			}
		}
	}
	
	public Gene<T> getGene(int index) { return transmission.valueAt(index); }

	@Override
	public Cell<T> getCell(int index) { return processingLayer.get(index); }
	
	/**
	 * Returns the total number of {@link Cell}s which make up
	 * this {@link SimpleOrgan}.
	 */
	@Override
	public int size() { return this.processingLayer.size(); }
	
	public List<Cell<T>> getProcessingLayer() { return processingLayer; }

	@Override
	public void setMonitor(Receptor<T> monitor) {
		((CellAdapter<T>) processingLayer.get(size() - 1)).setMeter(monitor);
	}

	@Override
	public Supplier<Runnable> setup() {
		List<Runnable> toSetup = new ArrayList<>();
		if (inputLayer != null) inputLayer.stream().map(Cell::setup).map(Supplier::get).forEach(toSetup::add);
		processingLayer.stream().map(Cell::setup).map(Supplier::get).forEach(toSetup::add);
		return () -> () -> toSetup.forEach(Runnable::run);
	}

	@Override
	public Supplier<Runnable> tick() {
		return this.temporals.tick();
	}

	@Override
	public Supplier<Runnable> push(Producer<T> protein) {
		if (inputLayer == null) {
			OperationList push = new OperationList();
			processingLayer.stream().map(c -> c.push(protein)).forEach(push::add);
			return push;
		}

		OperationList push = new OperationList();
		inputLayer.stream().map(c -> c.push(protein)).forEach(push::add);
		return push;
	}

	@Override
	public void reset() {
		Organ.super.reset();
		if (inputLayer != null) inputLayer.forEach(Cell::reset);
		processingLayer.forEach(Cell::reset);
	}
}
