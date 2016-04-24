package com.almostrealism.receptor.synth;

public interface Source {
	public long next();
	
	public boolean isDone();
}
