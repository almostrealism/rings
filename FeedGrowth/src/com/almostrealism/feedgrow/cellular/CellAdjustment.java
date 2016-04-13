package com.almostrealism.feedgrow.cellular;

public interface CellAdjustment<T, R> {
	public void adjust(Cell<T> toAdjust, R arg);
}
