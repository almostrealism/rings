package com.almostrealism.feedgrow.test;

import java.io.FileNotFoundException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.optimization.StableDurationHealthComputation;

public class DyadicOrganismTest {
	public static void main(String args[]) throws FileNotFoundException {
		AudioProteinCache cache = new AudioProteinCache();
		
		BasicDyadicChromosome c = new BasicDyadicChromosome(0.8, 0.85);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(500, c, cache);
		
		StableDurationHealthComputation h = new StableDurationHealthComputation(cache);
		h.computeHealth(s);
	}
}
