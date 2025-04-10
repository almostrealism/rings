/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.synth;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.audio.tone.KeyNumbering;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Cell;
import org.almostrealism.graph.SummationCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.Temporal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class AudioSynthesizer implements Temporal, StatelessSource, SamplingFeatures {
	private OvertoneSeries tones;

	private List<SineWaveCell> voices;
	private SummationCell output;
	private KeyboardTuning tuning;
	private AudioSynthesisModel model;

	public AudioSynthesizer() {
		this(null);
	}

	public AudioSynthesizer(AudioSynthesisModel model) {
		this(model, 7);
	}

	public AudioSynthesizer(AudioSynthesisModel model, int voiceCount) {
		setModel(model);
		this.tones = new OvertoneSeries(2, voiceCount - 3);
		this.tuning = new DefaultKeyboardTuning();

		output = new SummationCell();
		voices = new ArrayList<>();

		for (int i = 0; i < voiceCount; i++) {
			voices.add(new SineWaveCell());
			voices.get(i).setReceptor(output);
		}
	}

	public Cell<PackedCollection<?>> getOutput() {
		return output;
	}

	public void setTuning(KeyboardTuning t) { this.tuning = t; }

	public AudioSynthesisModel getModel() { return model; }
	public void setModel(AudioSynthesisModel model) { this.model = model; }

	public void setNoteMidi(int key) {
		setFrequency(tuning.getTone(key, KeyNumbering.MIDI));
	}

	public void setFrequency(Frequency f) {
		tones.setRoot(f);

		Iterator<SineWaveCell> itr = voices.iterator();
		for (Frequency r : tones) itr.next().setFreq(r.asHertz());
	}

	public void strike() {
		for (SineWaveCell s : voices) s.strike();
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("AudioSynthesizer Tick");
		voices.stream().map(cell -> cell.push(null)).forEach(tick::add);
		return tick;
	}

	@Override
	public Producer<PackedCollection<?>> generate(BufferDetails buffer,
												  Producer<PackedCollection<?>> params,
												  Producer<PackedCollection<?>> frequency) {
		double amp = 0.75;
		return sampling(buffer.getSampleRate(), () -> {
			double scale = amp / tones.total();

			List<Producer<?>> series = new ArrayList<>();

			for (Frequency f : tones) {
				CollectionProducer<PackedCollection<?>> t =
						integers(0, buffer.getFrames()).divide(buffer.getSampleRate());
				CollectionProducer<PackedCollection<?>> signal =
						sin(t.multiply(2 * Math.PI).multiply(f.asHertz()).multiply(frequency));

				if (model != null) {
					Producer<PackedCollection<?>> levels = model.getLevels(f.asHertz(), t);
					signal = signal.multiply(levels);
				}

				series.add(signal);
			}

			return add(series).multiply(scale);
		});
	}
}
