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
	public void identity() {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new IdentityFactor<>()),
						o(2, i -> new File("multi-identity-cell-test-" + i + ".wav")),
						i -> g(0.3, 0.5))
				.sec(5).get().run();
	}

	@Test
	public void identityDelay() {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> new IdentityFactor<>()),
						d(2, i -> new Scalar(2 * i + 1)),
						i -> g(0.3, 0.5))
				.om(i -> new File("multi-identity-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void identityDelayFeedback() {
		w("src/test/resources/Snare Perc DD.wav", "src/test/resources/Snare Perc DD.wav")
				.d(i -> new Scalar(2))
				.m(fi(), c(g(0.0, 0.4), g(0.4, 0.0)))
				.om(i -> new File("identity-delay-feedback-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void scale() {
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
				.om(i -> new File("multi-scale-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void scaleDelayFeedback() {
		w("src/test/resources/Snare Perc DD.wav", "src/test/resources/Snare Perc DD.wav")
				.d(i -> new Scalar(2))
				.m(f(2, i -> new ScaleFactor(0.45 * (i + 1)))::get, c(g(0.0, 0.5), g(0.5, 0.0)))
				.om(i -> new File("scale-delay-feedback-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void filter() {
		w("src/test/resources/Snare Perc DD.wav")
						.m(f(2, i -> hp(2000, 0.1)),
								o(2, i -> new File("multi-filter-cell-test-" + i + ".wav")),
								i -> g(0.3, 0.5))
						.sec(5).get().run();
	}

	@Test
	public void filterDelay() {
		w("src/test/resources/Snare Perc DD.wav")
				.m(f(2, i -> hp(2000, 0.1)),
						d(2, i -> new Scalar(2 * i + 1)),
						i -> g(0.3, 0.5))
				.om(i -> new File("multi-filter-delay-cell-test-" + i + ".wav"))
				.sec(8).get().run();
	}

	@Test
	public void filterDelayFeedback() {
		w("src/test/resources/Snare Perc DD.wav", "src/test/resources/Snare Perc DD.wav")
				.d(i -> new Scalar(2))
				.m(fc(i -> hp(2000, 0.1)),
						c(g(0.0, 0.3), g(0.3, 0.0)))
				.om(i -> new File("filter-delay-feedback-test-" + i + ".wav"))
				.sec(8).get().run();
	}
}
