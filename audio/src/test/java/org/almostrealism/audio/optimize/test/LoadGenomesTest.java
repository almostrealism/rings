package org.almostrealism.audio.optimize.test;

import org.almostrealism.audio.optimize.AudioPopulationOptimizer;
import org.almostrealism.audio.optimize.DefaultAudioGenome;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class LoadGenomesTest implements TestFeatures {
	@Test
	public void loadGenomes() throws FileNotFoundException {
		DefaultAudioGenome genome = new DefaultAudioGenome(6, 3, null);
		genome.assignTo(AudioPopulationOptimizer.read(new FileInputStream("Population.xml")).get(20));
		genome.setup().get().run();
		for (int i = 0; i < 6; i++) {
			System.out.println("Generator " + i + ": " +
					genome.valueAt(DefaultAudioGenome.GENERATORS, i, 0).getResultant(c(1.0)).get().evaluate().toDouble(0));
		}
	}
}
