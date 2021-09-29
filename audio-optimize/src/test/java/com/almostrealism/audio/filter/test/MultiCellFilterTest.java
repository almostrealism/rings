package com.almostrealism.audio.filter.test;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.heredity.IdentityFactor;
import org.almostrealism.heredity.ScaleFactor;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MultiCellFilterTest implements CellFeatures, TestFeatures {
	@Test
	public void identity() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new IdentityFactor<>()),
						o(2, i -> new File("multi-identity-cell-test-" + i + ".wav")),
						i -> g(0.3, 0.5))
				.sec(5).get().run();
	}

	@Test
	public void identityDelay() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new IdentityFactor<>()),
						d(2, i -> new Scalar(2 * i + 1)),
						i -> g(0.3, 0.5))
				.o(i -> new File("multi-identity-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void scale() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new ScaleFactor(0.3 * (i + 1))),
						o(2, i -> new File("multi-scale-cell-test-" + i + ".wav")),
						i -> g(0.3, 0.5))
				.sec(5).get().run();
	}

	@Test
	public void scaleDelay() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new ScaleFactor(0.3 * (i + 1))),
						d(2, i -> new Scalar(2 * i + 1)),
						i -> g(0.3, 0.5))
				.o(i -> new File("multi-scale-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void filter() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
						.m(f(2, i -> hp(2000, 0.1)),
								o(2, i -> new File("multi-filter-cell-test-" + i + ".wav")),
								i -> g(0.3, 0.5))
						.sec(5).get().run();
	}

	@Test
	public void filterDelay() throws IOException {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> hp(2000, 0.1)),
						d(2, i -> new Scalar(2 * i + 1)),
						i -> g(0.3, 0.5))
				.o(i -> new File("multi-filter-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}
}
