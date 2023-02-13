package org.almostrealism.audio.arrange.test;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.arrange.DefaultChannelSection;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class DefaultChannelSectionTest implements CellFeatures {
	@Test
	public void section() throws IOException {
		int samples = 2 * 8 * OutputLine.sampleRate;

		DefaultChannelSection.Factory factory = new DefaultChannelSection.Factory(new ConfigurableGenome(),
											1, () -> 2.0, 8, OutputLine.sampleRate);
		DefaultChannelSection section = factory.createSection(0);

		WaveData data = WaveData.load(new File("Library/Snare Perc DD.wav"));
		PackedCollection<?> input = new PackedCollection<>(samples);
		input.setMem(data.getCollection().toArray(0, data.getCollection().getMemLength()));

		PackedCollection<?> result = new PackedCollection<>(samples);
		Producer<PackedCollection<?>> destination = p(result);
		Producer<PackedCollection<?>> source = p(input);

		OperationList process = new OperationList();
		process.add(factory.setup());
		process.add(section.process(destination, source));

		process.get().run();
		System.out.println("Processed " + result.getMemLength() + " samples");
	}
}
