package com.almostrealism.feedgrow.test;

import java.util.ArrayList;
import java.util.List;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.content.ProteinCache;
import com.almostrealism.feedgrow.delay.BasicDelayCell;
import com.almostrealism.feedgrow.heredity.Chromosome;
import com.almostrealism.feedgrow.organ.SimpleOrgan;

public class BasicDyadicCellularSystem extends SimpleOrgan<Long> {
	private BasicDelayCell cellA, cellB;
	
	public BasicDyadicCellularSystem(int delay, Chromosome<Long> chromosome, ProteinCache<Long> cache) {
		super(createCells(delay), chromosome);
		setProteinCache(cache);
		this.cellA = (BasicDelayCell) getCells().get(0);
		this.cellB = (BasicDelayCell) getCells().get(1);
	}
	
	public BasicDelayCell getCellA() { return cellA; }
	
	public BasicDelayCell getCellB() { return cellB; }
	
	private static List<Cell<Long>> createCells(int delay) {
		List<Cell<Long>> c = new ArrayList<Cell<Long>>();
		c.add(new BasicDelayCell(delay));
		c.add(new BasicDelayCell(delay));
		return c;
	}
}
