/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.sound;

import com.almostrealism.audio.DesirablesProvider;
import com.almostrealism.tone.DefaultKeyboardTuning;
import com.almostrealism.tone.KeyPosition;
import com.almostrealism.tone.KeyboardTuning;
import com.almostrealism.tone.Scale;
import org.almostrealism.time.Frequency;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DefaultDesirablesProvider<T extends KeyPosition<T>> implements DesirablesProvider {
	private final double bpm;
	private final Set<Frequency> frequencies;
	private final Set<File> samples;

	public DefaultDesirablesProvider(double bpm) {
		this(bpm, Scale.of());
	}

	public DefaultDesirablesProvider(double bpm, Scale<T> scale) {
		this(bpm, scale, new DefaultKeyboardTuning());
	}

	public DefaultDesirablesProvider(double bpm, Scale<T> scale, KeyboardTuning tuning) {
		this.bpm = bpm;
		this.frequencies = new HashSet<>();
		this.frequencies.addAll(tuning.getTones(scale));
		this.samples = new HashSet<>();
	}

	@Override
	public double getBeatPerMinute() { return bpm; }

	@Override
	public Set<Frequency> getFrequencies() { return frequencies; }

	@Override
	public Set<File> getSamples() { return samples; }
}