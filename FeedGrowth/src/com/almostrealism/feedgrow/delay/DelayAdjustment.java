package com.almostrealism.feedgrow.delay;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.CellAdjustment;

public class DelayAdjustment<T> implements CellAdjustment<T, Double> {
	public void adjust(Cell<T> toAdjust, Double arg) {
		Delay delay = (Delay) toAdjust;
		long frames = (long) (delay.getDelayInFrames() * arg);
		delay.setDelayInFrames(frames);
	}
}
