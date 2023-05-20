package org.almostrealism.audio.grains;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;

public class GrainProcessor implements CodeFeatures {
	private int frames;
	private int sampleRate;
	private PackedCollection<?> count;

	private Evaluable<PackedCollection<?>> ev;

	public GrainProcessor(double duration, int sampleRate) {
		this.frames = (int) (duration * sampleRate);
		this.sampleRate = sampleRate;
		this.count = new PackedCollection<>(1);

		Producer<PackedCollection<?>> g = v(shape(3), 1);
		CollectionProducer<PackedCollection<?>> start = c(g, 0).multiply(c(sampleRate));
		CollectionProducer<PackedCollection<?>> d = c(g, 1).multiply(c(sampleRate));
		CollectionProducer<PackedCollection<?>> rate = c(g, 2);
		CollectionProducer<PackedCollection<?>> wavelength = multiply(v(shape(3), 2), c(sampleRate));
		Producer<PackedCollection<?>> phase = v(shape(3), 3);

		Producer<PackedCollection<?>> series = integers(0, frames);
		Producer<PackedCollection<?>> max = subtract(p(count), start);
		Producer<PackedCollection<?>> pos  = start.add(_mod(_mod(series, d), max));

		CollectionProducer<PackedCollection<?>> generate = interpolate(v(1, 0), pos, rate);
		generate = generate.multiply(_sinw(series, wavelength, phase, c(1.0)));
		ev = generate.get();
	}

	public WaveData apply(PackedCollection<?> input, Grain grain, PackedCollection<?> wavelength, PackedCollection<?> phase) {
		count.setMem((double) input.getCount());
		PackedCollection<?> result = ev.into(new PackedCollection<>(shape(frames), 1))
				.evaluate(input.traverse(0), grain, wavelength, phase);
		return new WaveData(result, sampleRate);
	}
}
