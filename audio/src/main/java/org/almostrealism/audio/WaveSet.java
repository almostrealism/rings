/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.ParameterizedWaveDataProviderFactory;
import org.almostrealism.audio.data.StaticWaveDataProviderFactory;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDataProviderList;
import org.almostrealism.audio.sequence.TempoAware;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.Scale;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.time.Frequency;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WaveSet implements TempoAware {
	private ParameterizedWaveDataProviderFactory source;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;
	private Scale<?> notes;

	public WaveSet() { }

	public WaveSet(WaveDataProvider source) {
		this(new StaticWaveDataProviderFactory(source));
	}

	public WaveSet(ParameterizedWaveDataProviderFactory source) {
		setSource(source);
		setTuning(new DefaultKeyboardTuning());
		setRoot(WesternChromatic.C3);
		setNotes(Scale.of(WesternChromatic.C3));
	}

	public ParameterizedWaveDataProviderFactory getSource() { return source; }

	public void setSource(ParameterizedWaveDataProviderFactory source) { this.source = source; }

	@JsonIgnore
	public int getCount() { return source.getCount(); }

	@Override
	public void setBpm(double bpm) {
		if (source instanceof TempoAware) ((TempoAware) source).setBpm(bpm);
	}

	public KeyboardTuning getTuning() { return tuning; }

	public void setTuning(KeyboardTuning tuning) { this.tuning = tuning; }

	// TODO  Maybe this should be called sourceNote, so
	// TODO  as to avoid confusion with the root of the scale
	public KeyPosition<?> getRoot() { return root; }

	public void setRoot(KeyPosition<?> root) { this.root = root; }

	public Scale<?> getNotes() { return notes; }

	public void setNotes(Scale<?> notes) { this.notes = notes; }

	@JsonIgnore
	public List<Frequency> getFrequencies() {
		double r = tuning.getTone(root).asHertz();
		return IntStream.range(0, notes.length())
				.mapToObj(notes::valueAt)
				.map(tuning::getTone)
				.map(Frequency::asHertz)
				.map(t -> t / r)
				.map(Frequency::new)
				.collect(Collectors.toList());
	}

	public WaveDataProviderList create(Producer<Scalar> x, Producer<Scalar> y, Producer<Scalar> z) {
		return source.create(x, y, z, getFrequencies());
	}
}
