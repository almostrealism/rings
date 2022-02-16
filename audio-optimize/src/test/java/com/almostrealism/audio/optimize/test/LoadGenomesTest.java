package com.almostrealism.audio.optimize.test;

import com.almostrealism.audio.optimize.AudioPopulationOptimizer;
import com.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LoadGenomesTest implements TestFeatures {
	@Test
	public void loadGenomes() throws FileNotFoundException {
		DefaultAudioGenome genome = new DefaultAudioGenome(6, 3);
		genome.assignTo(AudioPopulationOptimizer.read(new FileInputStream("Population.xml")).get(20));
		genome.setup().get().run();
		for (int i = 0; i < 6; i++) {
			System.out.println("Generator " + i + ": " +
					genome.valueAt(DefaultAudioGenome.GENERATORS, i, 0).getResultant(v(1.0)).get().evaluate());
		}
	}
}
