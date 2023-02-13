package org.almostrealism.audio;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.algebra.Scalar;

import java.util.function.Supplier;

public interface SamplingFeatures extends CodeFeatures {

	default int toFrames(double sec) { return (int) (OutputLine.sampleRate * sec); }

	default Producer<Scalar> toFrames(Supplier<Evaluable<? extends Scalar>> sec) {
		return scalarsMultiply(v(OutputLine.sampleRate), sec);
	}

	default int toFramesMilli(int msec) { return (int) (OutputLine.sampleRate * msec / 1000d); }

	default Producer<Scalar> toFramesMilli(Supplier<Evaluable<? extends Scalar>> msec) {
		return scalarsMultiply(v(OutputLine.sampleRate / 1000d), msec);
	}
}
