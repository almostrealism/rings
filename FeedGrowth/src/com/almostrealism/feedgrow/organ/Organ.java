package com.almostrealism.feedgrow.organ;

import com.almostrealism.feedgrow.cellular.Cell;
import com.almostrealism.feedgrow.cellular.Receptor;

public interface Organ<T> extends Receptor<T> {
	public Cell<T> getCell(int index);
	
	public int size();
	
	public void tick();
}
