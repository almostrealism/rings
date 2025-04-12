/*
 * Copyright 2025 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.audio.synth;

import org.almostrealism.audio.tone.RelativeFrequencySet;
import org.almostrealism.time.Frequency;

import java.util.ArrayList;
import java.util.List;

public class OvertoneSeries implements RelativeFrequencySet {
	private int subCount, superCount;

	public OvertoneSeries(int subCount, int superCount) {
		this.subCount = subCount;
		this.superCount = superCount;
	}

	@Override
	public Iterable<Frequency> getFrequencies(Frequency fundamental) {
		return () -> {
			List<Frequency> l = new ArrayList<>();

			for (int i = subCount; i > 0; i--) {
				l.add(new Frequency(fundamental.asHertz() / Math.pow(2, i)));
			}

			l.add(new Frequency(fundamental.asHertz()));

			for (int i = 1; i <= superCount; i++) {
				l.add(new Frequency(fundamental.asHertz() * Math.pow(2, i)));
			}

			return l.iterator();
		};
	}

	public int count() { return subCount + superCount + 1; }
}
