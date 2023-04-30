package org.almostrealism.audio.optimize;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.filter.AudioPassFilter;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Chromosome;
import org.almostrealism.heredity.Factor;
import org.almostrealism.heredity.Gene;

public class FixedFilterChromosome implements Chromosome<PackedCollection<?>>, CellFeatures {
	public static double defaultResonance = 0.1; // TODO
	private static double maxFrequency = 20000;

	private final Chromosome<PackedCollection<?>> source;
	private final int sampleRate;

	public FixedFilterChromosome(Chromosome<PackedCollection<?>> source, int sampleRate) {
		this.source = source;
		this.sampleRate = sampleRate;
	}

	@Override
	public int length() {
		return source.length();
	}

	@Override
	public Gene<PackedCollection<?>> valueAt(int pos) {
		return new FixedFilterGene(pos);
	}

	class FixedFilterGene implements Gene<PackedCollection<?>> {
		private final int index;

		public FixedFilterGene(int index) {
			this.index = index;
		}

		@Override
		public int length() { return 1; }

		@Override
		public Factor<PackedCollection<?>> valueAt(int pos) {
			Producer<PackedCollection<?>> lowFrequency = multiply(c(maxFrequency), source.valueAt(index, 0).getResultant(c(1.0)));
			Producer<PackedCollection<?>> highFrequency = multiply(c(maxFrequency), source.valueAt(index, 1).getResultant(c(1.0)));
			return new AudioPassFilter(sampleRate, lowFrequency, v(defaultResonance), true)
					.andThen(new AudioPassFilter(sampleRate, highFrequency, v(defaultResonance), false));
		}
	}
}
