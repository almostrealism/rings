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

import java.util.Iterator;
import java.util.List;

import org.almostrealism.cells.Cell;
import org.almostrealism.cells.CellAdapter;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;

public class MultiCell<T> extends CellAdapter<T> {
	public static int pushWait = 0;
	
	private List<Cell<T>> cells;
	private Gene<T> gene;
	
	public MultiCell(List<Cell<T>> cells, Gene<T> gene) {
		this.cells = cells;
		this.gene = gene;
	}
	
	public void push(long index) {
		if (pushWait != 0) {
			try {
				Thread.sleep(pushWait);
			} catch (InterruptedException e) { }
		}
		
		Iterator<Cell<T>> itr = cells.iterator();
		
		i:for (int i = 0; itr.hasNext(); i++) {
			Factor<T> factor = gene.getFactor(i);
			if (factor == null) {
				itr.next(); continue i;
			}
			
			long l = addProtein(factor.getResultant(getProtein(index)));
			itr.next().push(l);
		}
	}
}
