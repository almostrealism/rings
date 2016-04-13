package com.almostrealism.feedgrow.population;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.almostrealism.feedgrow.breeding.Genome;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.organ.Organ;
import com.almostrealism.feedgrow.organ.SimpleOrganFactory;
import com.almostrealism.feedgrow.systems.AdjustmentLayerOrganSystem;
import com.almostrealism.feedgrow.systems.TieredCellAdjustmentFactory;

public class LayeredOrganPopulation<T> implements Population<T> {
	private List<Genome> pop;
	private List<Organ<T>> organs;
	private double arg;
	
	public LayeredOrganPopulation(List<Genome> population) {
		this(population, 1.0);
	}
	
	public LayeredOrganPopulation(List<Genome> population, double arg) {
		this.pop = population;
		this.organs = new ArrayList<Organ<T>>();
		this.arg = arg;
	}
	
	public void init(SimpleOrganFactory organFactory, TieredCellAdjustmentFactory<T, ?> adjustmentFactory) {
		/* First initialize the sub population */
		
		Population<?> subpop;
		
		if (adjustmentFactory.getParent() != null) {
			// Construct the encapsulated layered organ population
			subpop = new LayeredOrganPopulation(truncatedGenomes());
			((LayeredOrganPopulation) subpop).init(organFactory, adjustmentFactory.getParent());
		} else {
			// Construct the encapsulated simple organ population
			subpop = new SimpleOrganPopulation(truncatedGenomes());
			((SimpleOrganPopulation) subpop).init(organFactory);
		}
		
		
		/* Next initialize the organs for this layer */
		
		Iterator<Genome> itr = pop.iterator();
		
		for (int i = 0; itr.hasNext(); i++) {
			AdjustmentLayerOrganSystem<T, ?> system =
					new AdjustmentLayerOrganSystem(subpop.getOrgan(i), adjustmentFactory,
													itr.next().getLastChromosome(), arg);
			organs.add(system);
		}
	}
	
	public void setProteinCache(ProteinCache<T> cache) {
		Iterator<Organ<T>> itr = organs.iterator();
		
		while (itr.hasNext()) {
			itr.next().setProteinCache(cache);
		}
	}
	
	private List<Genome> truncatedGenomes() {
		Iterator<Genome> itr = pop.iterator();
		List<Genome> trunk = new ArrayList<Genome>();
		while (itr.hasNext()) trunk.add(itr.next().getHeadSubset());
		return trunk;
	}
	
	public Organ<T> getOrgan(int index) { return organs.get(index); }
	
	public int size() { return organs.size(); }
}
