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

package com.almostrealism.feedgrow.organ;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.almostrealism.cells.CachedStateCell;
import org.almostrealism.cells.CachedStateCellGroup;
import org.almostrealism.cells.Cell;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;
import org.almostrealism.heredity.IdentityFactor;
import org.almostrealism.protein.ProteinCache;

public class SimpleOrgan<T> implements Organ<T> {
	private List<Cell<T>> cells;
	private Chromosome<T> chrom;
	private List<CellPair<T>> pairs;
	private CachedStateCellGroup<T> cacheGroup;
	
	protected SimpleOrgan() { }
	
	public SimpleOrgan(List<Cell<T>> cells, Chromosome<T> chrom) {
		init(cells, chrom);
	}
	
	protected void init(List<Cell<T>> cells, Chromosome<T> chrom) {
		this.cells = cells;
		this.chrom = chrom;
		this.cacheGroup = new CachedStateCellGroup<T>();
		createPairs();
	}
	
	private void createPairs() {
		this.pairs = new ArrayList<CellPair<T>>();
		
		Iterator<Cell<T>> itr = cells.iterator();
		
		for (int i = 0; itr.hasNext(); i++) {
			Cell<T> c = itr.next();
			
			if (c instanceof CachedStateCell<?>) {
				this.cacheGroup.add((CachedStateCell<T>) c);
			}
			
			MultiCell<T> m = new MultiCell<T>(cells, chrom.getGene(i));
			m.setName("SimpleOrgan[" + i + "]");
			
			CellPair<T> p = new CellPair<T>(c, m);
			p.setReceptorFactorA(null);
			p.setReceptorFactorB(new IdentityFactor<T>());
			this.pairs.add(p);
		}
	}
	
	public Gene<T> getGene(int index) { return chrom.getGene(index); }
	
	public void setProteinCache(ProteinCache<T> cache) {
		Iterator<CellPair<T>> itr = pairs.iterator();
		while (itr.hasNext()) itr.next().setProteinCache(cache);
	}
	
	public Cell<T> firstCell() { return cells.get(0); }
	
	public Cell<T> lastCell() { return cells.get(cells.size() - 1); }
	
	public Cell<T> getCell(int index) { return this.cells.get(index); }
	
	/**
	 * Returns the total number of {@link Cell}s which make up
	 * this {@link SimpleOrgan}.
	 */
	public int size() { return this.cells.size(); }
	
	protected List<Cell<T>> getCells() { return cells; }
	
	public void tick() { this.cacheGroup.tick(); }
	
	public void push(long proteinIndex) {
		firstCell().push(proteinIndex);
	}
}
