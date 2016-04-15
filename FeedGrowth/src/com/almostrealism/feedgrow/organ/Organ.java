package com.almostrealism.feedgrow.organ;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.time.Clock;

public interface Organ<T> extends Receptor<T>, Clock {
	public Cell<T> getCell(int index);
	
	public int size();
}
