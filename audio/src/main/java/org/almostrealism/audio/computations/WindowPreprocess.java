/*
 * Copyright 2021 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.computations;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.computations.Preemphasize;
import org.almostrealism.algebra.computations.ScalarBankPad;
import org.almostrealism.algebra.computations.ScalarBankPadFast;
import org.almostrealism.audio.feature.FeatureWindowFunction;
import org.almostrealism.audio.feature.FrameExtractionSettings;
import org.almostrealism.Ops;

import java.util.function.Supplier;

public class WindowPreprocess extends ScalarBankPadFast {
	public WindowPreprocess(FrameExtractionSettings settings, Supplier<Evaluable<? extends ScalarBank>> input) {
		this(settings.getWindowSize(), settings.getPaddedWindowSize(), settings.getWindowType(),
				settings.getBlackmanCoeff(), settings.getPreemphCoeff(), input);
	}

	public WindowPreprocess(int windowSize, int paddedWindowSize, String windowType, Scalar blackmanCoeff, Scalar preemphCoeff, Supplier<Evaluable<? extends ScalarBank>> input) {
		super(windowSize, paddedWindowSize,
				new FeatureWindowFunction(windowSize, windowType, blackmanCoeff).getWindow(
						Preemphasize.fast(windowSize,
							input, Ops.ops().v(preemphCoeff))));
	}
}
