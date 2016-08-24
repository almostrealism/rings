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

import org.almostrealism.cells.CellAdjustment;

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
