package com.almostrealism.feedgrow.cellular;

public class SummationCell extends CachedStateCell<Long> {
	public void push(long index) {
		long value = getProtein(index);
		
		if (getCachedValue() == null) {
			System.out.println("Caching " + value);
			setCachedValue(value);
		} else {
			System.out.println("Caching " + getCachedValue() + " + " + value);
			setCachedValue(getCachedValue() + value);
		}
	}
}
