package com.almostrealism.feedgrow.test;

import java.io.FileNotFoundException;

import javax.sound.sampled.LineUnavailableException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.audio.SineWaveCell;
import com.almostrealism.feedgrow.optimization.StableDurationHealthComputation;
import com.almostrealism.receptor.player.ReceptorPlayer;

public class DyadicOrganismTest {
	public static void main(String args[]) throws FileNotFoundException, LineUnavailableException {
		AudioProteinCache cache = new AudioProteinCache();
		
//		BasicDyadicChromosome c = new BasicDyadicChromosome(0.8, 0.85);
//		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(500, c, cache);
		
		BasicDyadicChromosome c = new BasicDyadicChromosome(0.999, 0.99);
		BasicDyadicCellularSystem s = new BasicDyadicCellularSystem(1000, c, cache);
		
		StableDurationHealthComputation h = new StableDurationHealthComputation(cache);
		h.computeHealth(s);
		
		SineWaveCell sine = new SineWaveCell(cache);
		sine.setNoteLength(500);
		sine.setAmplitude(0.5);
		sine.setFreq(200);
		
		sine.setReceptor(s.getCellA());
		ReceptorPlayer p = new ReceptorPlayer(cache);
		s.getCellA().setMeter(p);
		
		for (long l = 0; l < Long.MAX_VALUE; l++) {
			sine.push(0);
			s.tick();
		}
		
		p.finish();
	}
}
