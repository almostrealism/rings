package com.almostrealism.feedgrow.cellular;

/**
 * TODO  Why does this extend CachedStateCell? It does not use the cached value.
 */
public class AdjustmentCell<T, R> extends CachedStateCell<R> {
	private Cell<T> cell;
	private CellAdjustment<T, R> adjust;
	
	public AdjustmentCell(Cell<T> cell, CellAdjustment<T, R> adjustment) {
		this.cell = cell;
		this.adjust = adjustment;
	}
	
	public void push(long i) {
		adjust.adjust(cell, getProtein(i));
//		super.push(i);
	}
}
