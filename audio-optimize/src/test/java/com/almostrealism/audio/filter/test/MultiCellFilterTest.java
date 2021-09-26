package com.almostrealism.audio.filter.test;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MultiCellFilterTest implements CellFeatures, TestFeatures {
	@Test
	public void multi() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
						.m(f(2, i -> hp(2000, 0.1)),
								o(2, i -> new File("filter-cell-test-" + i + ".wav")),
								i -> g(0.3, 0.5))
						.sec(10).get().run();
	}
}
