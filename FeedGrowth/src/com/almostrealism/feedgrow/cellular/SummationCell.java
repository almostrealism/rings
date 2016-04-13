package com.almostrealism.feedgrow.cellular;

public class SummationCell extends CachedStateCell<Long> {
	public void push(long index) {
		if (getCachedValue() == null) {
			setCachedValue(getProtein(index));
		} else {
			setCachedValue(getCachedValue() + getProtein(index));
		}
	}
}
