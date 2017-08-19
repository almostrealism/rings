package org.almostrealism.color;

import org.almostrealism.util.Producer;

public class RealizableImage implements Producer<ColorProducer[][]> {
	private ColorProducer data[][];
	
	public RealizableImage(ColorProducer data[][]) {
		this.data = data;
	}
	
	@Override
	public ColorProducer[][] evaluate(Object[] args) {
		return data;
	}
}
