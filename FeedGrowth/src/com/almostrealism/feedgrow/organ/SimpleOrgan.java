package com.almostrealism.feedgrow.organ;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.almostrealism.feedgrow.cellular.CachedStateCell;
import com.almostrealism.feedgrow.cellular.CachedStateCellGroup;
import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.Gene;
import com.almostrealism.feedgrow.heredity.IdentityFactor;

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
