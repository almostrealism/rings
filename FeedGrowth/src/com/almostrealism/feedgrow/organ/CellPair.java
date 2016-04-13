package com.almostrealism.feedgrow.organ;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.heredity.Factor;

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
