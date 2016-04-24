package com.almostrealism.receptor.mixing;

import java.util.ArrayList;
import java.util.Iterator;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.SummationCell;
import com.almostrealism.receptor.synth.Source;
import com.almostrealism.time.Clock;

public class Mixer extends ArrayList<Source> implements Clock {
	private AudioProteinCache cache;
	private SummationCell c;
	
	public Mixer(AudioProteinCache cache, SummationCell receptor) {
		this.cache = cache;
		this.c = receptor;
	}
	
	@Override
	public void tick() {
		for (Source s : this) {
			c.push(cache.addProtein(s.next()));
		}
		
		Iterator<Source> itr = iterator();
		while (itr.hasNext()) if (itr.next().isDone()) itr.remove();
		
		this.c.tick();
	}
}
