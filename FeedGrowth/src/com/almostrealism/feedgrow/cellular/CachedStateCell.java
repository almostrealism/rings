package com.almostrealism.feedgrow.cellular;

import com.almostrealism.feedgrow.heredity.Factor;
import com.almostrealism.time.Clock;

public class CachedStateCell<T> extends FilteredCell<T> implements Factor<T>, Clock {
	public static boolean enableWarning = true;
	
	private T cachedValue;
	private T outValue;
	
	public CachedStateCell() {
		super(null);
		setFilter(this);
	}
	
	public void setCachedValue(T v) { this.cachedValue = v; }
	
	protected T getCachedValue() { return cachedValue; }
	
	public T getResultant(T value) { return outValue; }
	
	public void push(long index) {
		if (cachedValue == null) {
			cachedValue = getProtein(index);
			System.out.println("Caching " + cachedValue);
		} else if (enableWarning) {
			System.out.println("Warning: Cached cell is pushed when full");
		}
	}
	
	public void tick() { outValue = cachedValue; cachedValue = null; super.push(0); }
}
