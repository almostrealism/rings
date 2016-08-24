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
import java.util.List;

import org.almostrealism.cells.Cell;
import org.almostrealism.cells.CellFactory;
import org.almostrealism.cells.ProbabilityDensityCellFactory;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Gene;

import com.almostrealism.feedgrow.delay.DelayCellFactory;

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
