package com.almostrealism.feedgrow.systems;

import com.almostrealism.feedgrow.cellular.CellAdjustment;

public interface CellAdjustmentFactory<T, R> {
	public CellAdjustment<T, R> generateAdjustment(double arg);
}
