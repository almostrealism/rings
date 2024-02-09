/*
 * Copyright 2023 Michael Murray
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
import org.almostrealism.algebra.ScalarBankFeatures;
import org.almostrealism.algebra.computations.ScalarBankPad;
import org.almostrealism.audio.feature.FeatureWindowFunction;
import org.almostrealism.audio.feature.FrameExtractionSettings;
import org.almostrealism.Ops;
import org.almostrealism.collect.PackedCollection;

import java.util.function.Supplier;

public class WindowPreprocess extends ScalarBankPad {
	public WindowPreprocess(FrameExtractionSettings settings, Supplier<Evaluable<? extends PackedCollection<Scalar>>> input) {
		this(settings.getWindowSize(), settings.getPaddedWindowSize(), settings.getWindowType(),
				settings.getBlackmanCoeff(), settings.getPreemphCoeff(), input);
	}

	public WindowPreprocess(int windowSize, int paddedWindowSize, String windowType, Scalar blackmanCoeff, Scalar preemphCoeff, Supplier<Evaluable<? extends PackedCollection<Scalar>>> input) {
		super(windowSize, paddedWindowSize,
				new FeatureWindowFunction(windowSize, windowType, blackmanCoeff).getWindow(
						ScalarBankFeatures.getInstance().preemphasize(windowSize,
								input, Ops.o().v(preemphCoeff))));
	}
}
