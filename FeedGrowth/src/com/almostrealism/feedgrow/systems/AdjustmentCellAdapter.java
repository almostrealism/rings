package com.almostrealism.feedgrow.systems;

import com.almostrealism.feedgrow.cellular.AdjustmentCell;
import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;

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
