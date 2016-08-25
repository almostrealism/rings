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

package com.almostrealism.feedgrow.systems;

import org.almostrealism.cells.AdjustmentCell;
import org.almostrealism.cells.Cell;
import org.almostrealism.cells.CellAdjustment;
import org.almostrealism.cells.Receptor;
import org.almostrealism.protein.ProteinCache;

public class AdjustmentCellAdapter<T> extends AdjustmentCell<T, Double> {
	private Cell<Double> adjuster;
	private double factor;
	
	public AdjustmentCellAdapter(Cell<T> adjustable, Cell<Double> adjuster, CellAdjustment<T, Double> adjustment) {
		super(adjustable, adjustment);
		this.adjuster = adjuster;
		
		// This receptor will allow the values from the adjuster
		// to be used as the value for the adjustment parameter
		// when this cell adapter is pushed
		this.adjuster.setReceptor(new Receptor<Double>() {
			private ProteinCache<Double> cache;
			
			public void setProteinCache(ProteinCache<Double> p) { this.cache = p; }
			
			public void push(long proteinIndex) {
				AdjustmentCellAdapter.this.factor = cache.getProtein(proteinIndex);
			}
		});
	}
	
	public void push(long proteinIndex) {
		// By pushing the adjuster, a new value
		// is set for the factor
		this.adjuster.push(proteinIndex);
		
		// Now this new factor can be inserted
		// into the cache, and used for the
		// adjustment parameter
		long index = addProtein(factor);
		
		// The super class method will take care
		// of performing the actual adjustment
		// using this new factor
		super.push(index);
	}
}
