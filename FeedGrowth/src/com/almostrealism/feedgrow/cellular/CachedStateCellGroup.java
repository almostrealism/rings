package com.almostrealism.feedgrow.cellular;

import java.util.ArrayList;
import java.util.Iterator;

public class CachedStateCellGroup<T> extends ArrayList<CachedStateCell<T>> {
	public void tick() {
		Iterator<CachedStateCell<T>> itr = iterator();
		while (itr.hasNext()) itr.next().tick();
	}
}
