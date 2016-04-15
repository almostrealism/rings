package com.almostrealism.feedgrow.test;

import java.io.FileNotFoundException;

import javax.sound.sampled.LineUnavailableException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.optimization.StableDurationHealthComputation;
import com.almostrealism.receptor.player.ReceptorPlayer;

public class DyadicOrganismTest {
	public static void main(String args[]) throws FileNotFoundException, LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
		BasicDyadicChromosome c = new BasicDyadicChromosome(0.8, 0.85);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(500, c, cache);
		
		StableDurationHealthComputation h = new StableDurationHealthComputation(cache);
//		h.computeHealth(s);
		
		
		ReceptorPlayer p = new ReceptorPlayer(null);
		p.setProteinCache(cache);
		s.getCellA().setMeter(p);
		
		for (long l = 0; l < Long.MAX_VALUE; l++) {
			s.push(0);
		}
		
		p.finish();
	}
}
