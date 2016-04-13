package com.almostrealism.feedgrow.organ;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellFactory;
import com.almostrealism.feedgrow.cellular.ProbabilityDensityCellFactory;
import com.almostrealism.feedgrow.delay.DelayCellFactory;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.heredity.Gene;

public class SimpleOrganFactory<T> {
	public static int minDelay = 40;
	public static int maxDelay = 6000;
	
	public static SimpleOrganFactory<Long> defaultFactory;
	
	static {
		CellFactory choices[] = {new DelayCellFactory(minDelay, maxDelay)};
		defaultFactory = new SimpleOrganFactory<Long>(new ProbabilityDensityCellFactory<Long>(choices));
	}
	
	private CellFactory<T> factory;
	
	public SimpleOrganFactory(CellFactory<T> f) { this.factory = f; }
	
	public SimpleOrgan<T> generateOrgan(Chromosome<Double> x, Chromosome<T> y) {
		List<Cell<T>> cells = new ArrayList<Cell<T>>();
		
		// Obtain the first gene of the X chromosome
		// which controls the selection of cell types
		Gene<Double> g = x.getGene(0);
		
		for (int i = 0; i < y.length(); i++) {
			// Generate a new cell using the factor found at
			// the specified index in the X chromosome's gene
			cells.add(factory.generateCell(g.getFactor(i).getResultant(1.0)));
		}
		
		// Return a new organ with the specified cells
		// plus the Y chromosome which controls the
		// scale of expression for each cell
		return new SimpleOrgan<T>(cells, y);
	}
}
