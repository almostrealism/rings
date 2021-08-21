/*
 * Copyright 2020 Michael Murray
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

package org.almostrealism.organs;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.graph.Adjustable;
import org.almostrealism.graph.AdjustmentCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Adjustment;
import io.almostrealism.relation.Producer;
import org.almostrealism.util.CodeFeatures;
import org.almostrealism.hardware.OperationList;

import java.util.function.Supplier;

public class AdjustmentCellAdapter<T> extends AdjustmentCell<T, Scalar> implements CodeFeatures {
	private Cell<Scalar> adjuster;
	private Scalar factor;
	
	public AdjustmentCellAdapter(Adjustable<T> adjustable, Cell<Scalar> adjuster, Adjustment<T> adjustment) {
		super(adjustable, adjustment);
		this.adjuster = adjuster;
		
		// This receptor will allow the values from the adjuster
		// to be used as the value for the adjustment parameter
		// when this cell adapter is pushed
		this.adjuster.setReceptor(protein -> () -> () -> {
			Scalar v = protein.get().evaluate();
			AdjustmentCellAdapter.this.factor.setMem(new double[] {
					v.getValue(), v.getCertainty()
			});
		});
	}

	@Override
	public Supplier<Runnable> push(Producer<Scalar> protein) {
		OperationList r = new OperationList();

		// By pushing the adjuster, a new value
		// is set for the factor
		r.add(this.adjuster.push(protein));
		
		// The super class method will take care
		// of performing the actual adjustment
		// using this new factor
		r.add(super.push(p(factor)));

		return r;
	}
}
