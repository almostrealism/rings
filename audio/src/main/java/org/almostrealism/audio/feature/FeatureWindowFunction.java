package org.almostrealism.audio.feature;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.computations.ScalarBankProduct;
import org.almostrealism.CodeFeatures;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class FeatureWindowFunction implements CodeFeatures {
	private final ScalarBank win;
	Evaluable<ScalarBank> window;

	public FeatureWindowFunction(FrameExtractionSettings opts) {
		this(opts.getWindowSize(), opts.getWindowType(), opts.getBlackmanCoeff());
	}

	public FeatureWindowFunction(int frameLength, String windowType, Scalar blackmanCoeff) {
		assert frameLength > 0;

		win = new ScalarBank(frameLength);

		double a = 2.0 * Math.PI / (frameLength - 1);

		for (int i = 0; i < frameLength; i++) {
			double v = 0.5 - 0.5 * Math.cos(a * (double) i);
			if ("hanning".equals(windowType)) {
				win.set(i, v);
			} else if ("sine".equals(windowType)) {
				// when you are checking ws wikipedia, please
				// note that 0.5 * a = PI/(frameLength-1)
				win.set(i, Math.sin(0.5 * a * (double) i));
			} else if ("hamming".equals(windowType)) {
				win.set(i, 0.54 - 0.46 * Math.cos(a * (double) i));
			} else if ("povey".equals(windowType)) {  // like hamming but goes to zero at edges.
				win.set(i, Math.pow(v, 0.85));
			} else if ("rectangular".equals(windowType)) {
				win.set(i, 1.0);
			} else if ("blackman".equals(windowType)) {
				win.set(i, blackmanCoeff.getValue() - 0.5 * Math.cos(a * (double) i) +
						(0.5 - blackmanCoeff.getValue()) * Math.cos(2 * a * (double) i));
			} else {
				throw new IllegalArgumentException("Invalid window type " + windowType);
			}
		}

		this.window = ScalarBankProduct.fast(win.getCount(), scalars(win), v(win.getCount() * 2, 0)).get();
	}

	public UnaryOperator<ScalarBank> getWindow() {
		return window::evaluate;
	}

	public Producer<ScalarBank> getWindow(Supplier<Evaluable<? extends ScalarBank>> input) {
		return ScalarBankProduct.fast(win.getCount(), scalars(win), input);
	}
}
