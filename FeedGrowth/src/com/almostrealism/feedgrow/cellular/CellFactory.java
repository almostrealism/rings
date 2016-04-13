package com.almostrealism.feedgrow.cellular;

public interface CellFactory<T> {
	public Cell<T> generateCell(double arg);
}
