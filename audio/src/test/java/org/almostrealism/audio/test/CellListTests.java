package org.almostrealism.audio.test;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.AdjustableDelayCell;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.mem.MemoryDataCopy;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class CellListTests implements CellFeatures {
	@Test
	public void export() throws IOException {
		WaveData data = WaveData.load(new File("Library/Snare Perc DD.wav"));
		int samples = data.getCollection().getMemLength();

		PackedCollection<?> result = new PackedCollection<>(samples);
		Producer<PackedCollection<?>> destination = p(result);
		Producer<PackedCollection<?>> source = p(data.getCollection());

		PackedCollection<?> input = new PackedCollection<>(samples);
		PackedCollection<PackedCollection<?>> output = new PackedCollection<PackedCollection<?>>(shape(1, samples)).traverse(1);

		CellList cells = cells(1, i -> new WaveCell(input.traverseEach(), OutputLine.sampleRate)); // .f(i -> lp(2000, 0.1));

		OperationList process = new OperationList();
		process.add(new MemoryDataCopy("CellListTest Export Input", () -> source.get().evaluate(), () -> input, samples));
		process.add(cells.export(output));
		process.add(new MemoryDataCopy("CellListTest Export Output", () -> output, () -> destination.get().evaluate(), samples));

		process.get().run();
		System.out.println("Exported " + result.getMemLength() + " frames");
		Assert.assertFalse(result.toDouble(30) == 0.0);
	}

	@Test
	public void mselfDelay() {
		CellList cells = w("Library/Snare Perc DD.wav");

		CellList delays = IntStream.range(0, 1)
				.mapToObj(i -> new AdjustableDelayCell(OutputLine.sampleRate, scalar(2.0)))
				.collect(CellList.collector());

		cells = cells.m(fi(), delays)
				.mself(fi(), i -> g(2.0))
				.sum().o(i -> new File("results/mself-delay-test.wav"));

		cells.sec(10).get().run();
	}
}
