package com.almostrealism.feedgrow.systems;

import com.almostrealism.feedgrow.organ.Organ;

public interface OrganSystem<T> extends Organ<T> {
	public Organ<T> getOrgan(int index);
	
	public int size();
}
