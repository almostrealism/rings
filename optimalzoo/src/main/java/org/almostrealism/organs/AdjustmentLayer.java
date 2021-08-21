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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.almostrealism.graph.Adjustable;
import org.almostrealism.graph.AdjustmentCell;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.Adjustment;
import org.almostrealism.heredity.Chromosome;

public class AdjustmentLayer<T, R> extends SimpleOrgan<R> {

	public AdjustmentLayer(List<Adjustable<T>> toAdjust, List<Adjustment<T>> adjustments, Chromosome<R> chrom) {
		init(null, createAdjustmentCells(toAdjust, adjustments), chrom);
	}
	
	private List<Cell<R>> createAdjustmentCells(List<Adjustable<T>> toAdjust, List<Adjustment<T>> adjustments) {
		ArrayList<Cell<R>> cells = new ArrayList<Cell<R>>();
		Iterator<Adjustable<T>> itrC = toAdjust.iterator();
		Iterator<Adjustment<T>> itrA = adjustments.iterator();
		
		while (itrC.hasNext() && itrA.hasNext()) {
			cells.add(new AdjustmentCell(itrC.next(), itrA.next()));
		}
		
		return cells;
	}
}
