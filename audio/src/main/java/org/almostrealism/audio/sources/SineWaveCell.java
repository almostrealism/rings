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

package org.almostrealism.audio.sources;

import io.almostrealism.relation.Evaluable;
import org.almostrealism.audio.filter.Envelope;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.CollectionTemporalCellAdapter;
import org.almostrealism.algebra.Scalar;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.OperationList;

import java.util.function.Supplier;

// TODO  Reimplement as a function of org.almostrealism.graph.TimeCell
public class SineWaveCell extends CollectionTemporalCellAdapter implements SamplingFeatures {
	private Envelope env;
	private final SineWaveCellData data;

	private double noteLength;
	private double waveLength;
	private double phase;
	private double amplitude;

	public SineWaveCell() {
		this(new PolymorphicAudioData());
	}

	public SineWaveCell(SineWaveCellData data) {
		this.data = data;
	}

	public void setEnvelope(Envelope e) { this.env = e; }

	public void strike() { data.setNotePosition(0); }
	
	public void setFreq(double hertz) { this.waveLength = hertz / (double) OutputLine.sampleRate; }

	public Supplier<Runnable> setFreq(Producer<Scalar> hertz) {
		return a(1, data::getWaveLength, divide(hertz, v(OutputLine.sampleRate)));
	}

	// TODO  Rename to milli, default should be seconds
	public void setNoteLength(int msec) { this.noteLength = toFramesMilli(msec); }

	// TODO  Rename to milli, default should be seconds
	public Supplier<Runnable> setNoteLength(Supplier<Evaluable<? extends Scalar>> noteLength) {
		return a(1, data::getNoteLength, toFramesMilli(noteLength));
	}
	
	public void setPhase(double phase) { this.phase = phase; }
	
	public void setAmplitude(double amp) { amplitude = amp; }

	public Supplier<Runnable> setAmplitude(Producer<Scalar> amp) {
		return a(1, data::getAmplitude, amp);
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList defaults = new OperationList("SineWaveCell Default Value Assignment");
		defaults.add(a(1, data::getDepth, v(CollectionTemporalCellAdapter.depth)));
		defaults.add(a(1, data::getNotePosition, v(0)));
		defaults.add(a(1, data::getWavePosition, v(0)));
		defaults.add(a(1, data::getNoteLength, v(noteLength)));
		defaults.add(a(1, data::getWaveLength, v(waveLength)));
		defaults.add(a(1, data::getPhase, v(phase)));
		defaults.add(a(1, data::getAmplitude, v(amplitude)));

		Supplier<Runnable> customization = super.setup();

		OperationList setup = new OperationList("SineWaveCell Setup");
		setup.add(defaults);
		setup.add(customization);
		return setup;
	}

	@Override
	public Supplier<Runnable> push(Producer<PackedCollection<?>> protein) {
		PackedCollection<?> value = new PackedCollection<>(1);
		OperationList push = new OperationList("SineWaveCell Push");
		push.add(new SineWavePush(data, env == null ? v(1.0) :
					env.getScale(data::getNotePosition), value));
		push.add(super.push(p(value)));
		return push;
	}

	@Override
	public Supplier<Runnable> tick() {
		OperationList tick = new OperationList("SineWaveCell Tick");
		tick.add(new SineWaveTick(data, env == null ? v(1.0) :
				env.getScale(data::getNotePosition)));
		tick.add(super.tick());
		return tick;
	}
}
