/*
 * Copyright 2016 Michael Murray
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
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import io.almostrealism.code.Setup;
import org.almostrealism.graph.CachedStateCell;
import org.almostrealism.graph.CachedStateCellGroup;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.CellAdapter;
import org.almostrealism.graph.Receptor;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.IdentityFactor;
import io.almostrealism.relation.Producer;

public class SimpleOrgan<T> implements Organ<T> {
	private String name;

	private List<Cell<T>> inputLayer;
	private List<Cell<T>> processingLayer;
	private Chromosome<T> chrom;
	private CachedStateCellGroup<T> cacheGroup;
	
	protected SimpleOrgan() { }
	
	public SimpleOrgan(List<Cell<T>> inputLayer, List<Cell<T>> processingLayer, Chromosome<T> chrom) {
		init(inputLayer, processingLayer, chrom);
	}
	
	protected void init(List<Cell<T>> inputLayer, List<Cell<T>> processingLayer, Chromosome<T> chrom) {
		if (inputLayer != null && inputLayer.size() != processingLayer.size()) {
			throw new IllegalArgumentException("Input and processing layers must have the same number of cells");
		}

		this.inputLayer = inputLayer;
		this.processingLayer = processingLayer;
		this.chrom = chrom;
		this.cacheGroup = new CachedStateCellGroup<>();
		createPairs();
	}

	@Override
	public String getName() { return name; }

	@Override
	public void setName(String name) { this.name = name; }

	private void createPairs() {
		if (chrom == null) {
			System.out.println("WARN: " + getClass().getSimpleName() +
					" has no chromosome and will not have pairs created");
		}

		List<CellPair<T>> pairs = new ArrayList<>();
		for (AtomicInteger i = new AtomicInteger(); i.get() < processingLayer.size(); i.incrementAndGet()) {
			Cell<T> source = Optional.ofNullable(inputLayer).map(l -> l.get(i.get())).orElse(null);
			Cell<T> processing = processingLayer.get(i.get());
			
			if (source instanceof CachedStateCell<?>) {
				this.cacheGroup.add((CachedStateCell<T>) source);
			} else if (source != null) {
				System.out.println("WARN: " + source.getClass().getSimpleName() +
						" is not a CachedStateCell and will not have tick() triggered");
			}

			if (processing instanceof CachedStateCell<?>) {
				this.cacheGroup.add((CachedStateCell<T>) processing);
			} else {
				System.out.println("WARN: " + processing.getClass().getSimpleName() +
						" is not a CachedStateCell and will not have tick() triggered");
			}

			if (source != null) source.setReceptor(processing);

			if (chrom != null) {
				MultiCell<T> m = new MultiCell<>(processingLayer, chrom.valueAt(i.get()));
				m.setName("SimpleOrgan[" + i + "]");
				pairs.add(new CellPair<>(processing, m, null, new IdentityFactor<>()));
			}
		}
	}
	
	public Gene<T> getGene(int index) { return chrom.valueAt(index); }

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
	public Supplier<Runnable> tick() { return this.cacheGroup.tick(); }

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
