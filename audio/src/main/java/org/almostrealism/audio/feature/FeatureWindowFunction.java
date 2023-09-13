package org.almostrealism.audio.feature;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

import java.util.function.UnaryOperator;

public class FeatureWindowFunction implements CodeFeatures {
	private final PackedCollection<Scalar> win;
	Evaluable<PackedCollection<Scalar>> window;

	public FeatureWindowFunction(FrameExtractionSettings opts) {
		this(opts.getWindowSize(), opts.getWindowType(), opts.getBlackmanCoeff());
	}

	public FeatureWindowFunction(int frameLength, String windowType, Scalar blackmanCoeff) {
		assert frameLength > 0;

		win = Scalar.scalarBank(frameLength);

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

		this.window = scalarBankProduct(win.getCount(), scalars(win), v(win.getCount() * 2, 0)).get();
	}

	public UnaryOperator<PackedCollection<Scalar>> getWindow() {
		return window::evaluate;
	}

	public Producer<PackedCollection<Scalar>> getWindow(Producer<PackedCollection<Scalar>> input) {
		return scalarBankProduct(win.getCount(), scalars(win), input);
	}
}
