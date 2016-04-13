package com.almostrealism.feedgrow.delay;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellFactory;

public class DelayCellFactory implements CellFactory<Long> {
	private int min, delta;
	
	public DelayCellFactory(int minDelay, int maxDelay) {
		this.min = minDelay;
		this.delta = maxDelay - minDelay;
	}
	
	public Cell<Long> generateCell(double arg) {
		return new BasicDelayCell((int) (min + arg * delta));
	}
}
