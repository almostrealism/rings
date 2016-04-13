package com.almostrealism.feedgrow.systems;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.organ.Organ;

public class AdjustmentLayerOrganSystem<T, R> implements OrganSystem<T> {
	private ProteinCache<T> cache;
	private Organ<T> adjustable;
	private AdjustmentLayer<T, R> adjust;
	
	public AdjustmentLayerOrganSystem(Organ<T> adjustable, CellAdjustmentFactory<T, R> factory, Chromosome<R> chrom) {
		this(adjustable, factory, chrom, 1.0);
	}
	
	public AdjustmentLayerOrganSystem(Organ<T> adjustable, CellAdjustmentFactory<T, R> factory, Chromosome<R> chrom, double arg) {
		this.adjustable = adjustable;
		
		List<Cell<T>> c = new ArrayList<Cell<T>>();
		List<CellAdjustment<T, R>> l = new ArrayList<CellAdjustment<T, R>>();
		
		for (int i = 0; i < adjustable.size(); i++) {
			c.add(adjustable.getCell(i));
			l.add(factory.generateAdjustment(arg));
		}
		
		this.adjust = new AdjustmentLayer<T, R>(c, l, chrom);
	}
	
	public void tick() {
		adjust.tick();
		adjustable.tick();
	}
	
	public void setAdjustmentLayerProteinCache(ProteinCache<R> p) {
		this.adjust.setProteinCache(p);
	}
	
	public void setProteinCache(ProteinCache<T> p) {
		this.cache = p;
		this.adjustable.setProteinCache(p);
	}
	
	public ProteinCache<T> getProteinCache() { return this.cache; }
	
	public Cell<T> getCell(int index) { return adjustable.getCell(index); }
	
	public Organ<T> getOrgan(int index) { return adjustable; }
	
	public int size() { return adjustable.size(); }
	
	public void push(long proteinIndex) {
		adjust.getCell(0).push(0);
		adjustable.getCell(0).push(proteinIndex);
	}
}
