package com.almostrealism.feedgrow.systems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.almostrealism.feedgrow.cellular.AdjustmentCell;
import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.organ.SimpleOrgan;

public class AdjustmentLayer<T, R> extends SimpleOrgan<R> {

	public AdjustmentLayer(List<Cell<T>> toAdjust, List<CellAdjustment<T, R>> adjustments, Chromosome<R> chrom) {
		init(createAdjustmentCells(toAdjust, adjustments), chrom);
	}
	
	private List<Cell<R>> createAdjustmentCells(List<Cell<T>> toAdjust, List<CellAdjustment<T, R>> adjustments) {
		ArrayList<Cell<R>> cells = new ArrayList<Cell<R>>();
		Iterator<Cell<T>> itrC = toAdjust.iterator();
		Iterator<CellAdjustment<T, R>> itrA = adjustments.iterator();
		
		while (itrC.hasNext() && itrA.hasNext()) {
			cells.add(new AdjustmentCell<T, R>(itrC.next(), itrA.next()));
		}
		
		return cells;
	}
}
