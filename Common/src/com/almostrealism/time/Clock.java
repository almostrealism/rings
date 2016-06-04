package com.almostrealism.time;

/**
 * Any operation that is performed as a sequence of steps can implement {@link Clock}
 * to allow for easy synchronization between groups of operations.
 * 
 * @author  Michael Murray
 */
public interface Clock {
	public void tick();
}
