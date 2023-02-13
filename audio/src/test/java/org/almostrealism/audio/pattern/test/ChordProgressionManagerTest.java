package org.almostrealism.audio.pattern.test;

import org.almostrealism.audio.pattern.ChordProgressionManager;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.audio.tone.WesternScales;
import org.almostrealism.heredity.ConfigurableGenome;
import org.junit.Test;

import java.util.stream.IntStream;

public class ChordProgressionManagerTest {
	@Test
	public void progression() {
		ConfigurableGenome genome = new ConfigurableGenome();

		ChordProgressionManager progression = new ChordProgressionManager(genome, WesternScales.minor(WesternChromatic.G1, 1));
		progression.setSize(8);
		progression.setDuration(16);

		IntStream.range(0, 10).forEach(i -> {
			genome.assignTo(genome.getParameters().random());
			progression.refreshParameters();
			System.out.println(progression.getRegionString());
		});
	}
}
