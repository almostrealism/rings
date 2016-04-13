package com.almostrealism.feedgrow.cellular;

public class FloatingPointSummationCell extends CachedStateCell<Double> {
	public void push(long index) {
		if (getCachedValue() == null) {
			setCachedValue(getProtein(index));
		} else {
			setCachedValue(getCachedValue() + getProtein(index));
		}
	}
}
