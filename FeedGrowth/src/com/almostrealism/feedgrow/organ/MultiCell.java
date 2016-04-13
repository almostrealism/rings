package com.almostrealism.feedgrow.organ;

import java.util.Iterator;
import java.util.List;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdapter;
import com.almostrealism.feedgrow.heredity.Factor;
import com.almostrealism.feedgrow.heredity.Gene;

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
