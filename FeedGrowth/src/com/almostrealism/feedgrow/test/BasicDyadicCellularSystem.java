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

package com.almostrealism.feedgrow.test;

import java.util.ArrayList;
import java.util.List;

import org.almostrealism.cells.Cell;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.organs.SimpleOrgan;
import org.almostrealism.protein.ProteinCache;

import com.almostrealism.feedgrow.delay.BasicDelayCell;

public class BasicDyadicCellularSystem extends SimpleOrgan<Long> {
	private BasicDelayCell cellA, cellB;
	
	/**
	 * 
	 * @param delay  Delay in milliseconds
	 * @param chromosome
	 * @param cache
	 */
	public BasicDyadicCellularSystem(int delay, Chromosome<Long> chromosome, ProteinCache<Long> cache) {
		super(createCells(delay), chromosome);
		setProteinCache(cache);
		this.cellA = (BasicDelayCell) getCells().get(0);
		this.cellB = (BasicDelayCell) getCells().get(1);
		
		this.cellA.setName("Cell A");
		this.cellB.setName("Cell B");
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
