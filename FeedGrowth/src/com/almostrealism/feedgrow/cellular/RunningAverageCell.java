package com.almostrealism.feedgrow.cellular;

public class RunningAverageCell extends CachedStateCell<Double> {
	private double total;
	private int pushes;
	
	public void push(long index) {
		this.total = total + getProtein(index);
		this.pushes++;
		
		// Update the cached value to the current
		// running average of values received
		setCachedValue(this.total / pushes);
	}
	
	public void tick() {
		this.total = 0;
		this.pushes = 0;
		super.tick();
	}
}
