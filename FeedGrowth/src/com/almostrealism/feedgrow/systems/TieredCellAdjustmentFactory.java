package com.almostrealism.feedgrow.systems;

import com.almostrealism.feedgrow.cellular.CellAdjustment;

public class TieredCellAdjustmentFactory<T, R> implements CellAdjustmentFactory<T, R> {
	private CellAdjustmentFactory<T, R> tier;
	private TieredCellAdjustmentFactory<?, ?> parent;
	
	public TieredCellAdjustmentFactory(CellAdjustmentFactory<T, R> tier, CellAdjustmentFactory<?, ?> parent) {
		this(tier, (TieredCellAdjustmentFactory) (parent instanceof TieredCellAdjustmentFactory ? parent : new TieredCellAdjustmentFactory(parent, null)));
	}
	
	public TieredCellAdjustmentFactory(CellAdjustmentFactory<T, R> tier, TieredCellAdjustmentFactory<?, ?> parent) {
		this.tier = tier;
		this.parent = parent;
	}
	
	public CellAdjustment<T, R> generateAdjustment(double arg) { return this.tier.generateAdjustment(arg); }
	
	public TieredCellAdjustmentFactory<?, ?> getParent() { return parent; }
}
