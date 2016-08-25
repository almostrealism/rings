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

package org.almostrealism.organs;

import org.almostrealism.cells.Cell;
import org.almostrealism.cells.Receptor;
import org.almostrealism.heredity.Factor;
import org.almostrealism.protein.ProteinCache;

public class CellPair<T> implements Receptor<T> {
	private ProteinCache<T> cache;
	
	private Cell<T> cellA, cellB;
	private Factor<T> factorA, factorB;
	
	public CellPair(Cell<T> cellA, Cell<T> cellB) {
		this.cellA = cellA;
		this.cellB = cellB;
		
		this.cellA.setReceptor(new Receptor<T>() {
			public void setProteinCache(ProteinCache<T> p) { }
			public void push(long proteinIndex) { CellPair.this.push(proteinIndex, false, true); }
		});
		
		this.cellB.setReceptor(new Receptor<T>() {
			public void setProteinCache(ProteinCache<T> p) { }
			public void push(long proteinIndex) { CellPair.this.push(proteinIndex, true, false); }
		});
	}
	
	public void setReceptorFactorA(Factor<T> r) { this.factorA = r; }
	
	public void setReceptorFactorB(Factor<T> r) { this.factorB = r; }
	
	public void setProteinCache(ProteinCache<T> p) {
		this.cache = p;
		this.cellA.setProteinCache(p);
		this.cellB.setProteinCache(p);
	}
	
	public void push(long proteinIndex) {
		push(proteinIndex, true, true);
	}
	
	private void push(long proteinIndex, boolean toA, boolean toB) {
		T v = this.cache.getProtein(proteinIndex);
		
		if (toA && factorA != null) {
			long a = this.cache.addProtein(factorA.getResultant(v));
			this.cellA.push(a);
		}
		
		if (toB && factorB != null) {
			long b = this.cache.addProtein(factorB.getResultant(v));
			this.cellB.push(b);
		}
	}
}
