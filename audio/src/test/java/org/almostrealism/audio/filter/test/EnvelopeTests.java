/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.filter.test;

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.filter.EnvelopeSection;
import org.almostrealism.audio.filter.ParameterizedFilterEnvelope;
import org.almostrealism.audio.filter.ParameterizedVolumeEnvelope;
import org.almostrealism.audio.notes.PatternNoteLayer;
import org.almostrealism.audio.tone.DefaultKeyboardTuning;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.jni.NativeCompiler;
import org.almostrealism.hardware.metal.MetalProgram;
import org.almostrealism.util.TestSettings;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class EnvelopeTests implements CellFeatures, EnvelopeFeatures {
//	static {
//		NativeCompiler.enableInstructionSetMonitoring = !TestSettings.skipLongTests;
//		MetalProgram.enableProgramMonitoring = !TestSettings.skipLongTests;
//	}

	@Test
	public void attackSample() throws IOException {
		WaveData.load(new File("Library/organ.wav"))
				.sample(attack(c(1.0)))
				.save(new File("results/attack-sample.wav"));
	}

	@Test
	public void attack() {
		double attack = 0.5;

		EnvelopeSection env = envelope(attack(c(attack)));

		PackedCollection<?> data = new PackedCollection<>(4 * 44100);
		data = c(p(data.traverseEach())).add(c(1.0)).get().evaluate();

		new WaveData(data, 44100)
				.sample(env).save(new File("results/attack.wav"));
	}

	@Test
	public void adsr() {
		double duration = 8.0;
		double attack = 0.5;
		double decay = 1.0;
		double sustain = 0.3;
		double release = 3.0;

		EnvelopeSection env = envelope(attack(c(attack)))
				.andThenDecay(c(attack), c(decay), c(sustain))
				.andThen(c(attack + decay), sustain(c(sustain)))
				.andThenRelease(c(duration), c(sustain), c(release), c(0.0));


		PackedCollection<?> data = new PackedCollection<>(10 * 44100);
		data = c(p(data.traverseEach())).add(c(1.0)).get().evaluate();

		new WaveData(data, 44100)
				.sample(env).save(new File("results/adsr.wav"));
	}

	@Test
	public void asr() {
		double d0 = 0.5;
		double d1 = 3.0;
		double d2 = 7.0;

		double v0 = 0.2;
		double v1 = 0.8;
		double v2 = 0.5;
		double v3 = 0.95;

		EnvelopeSection env = envelope(linear(c(0.0), c(d0), c(v0), c(v1)))
				.andThenRelease(c(d0), c(v1), c(d1).subtract(c(d0)), c(v2))
				.andThenRelease(c(d1), c(v2), c(d2).subtract(c(d1)), c(v3));


		PackedCollection<?> data = new PackedCollection<>(7 * 44100);
		data = c(p(data.traverseEach())).add(c(1.0)).get().evaluate();

		new WaveData(data, 44100)
				.sample(env).save(new File("results/asr.wav"));
	}

	@Test
	public void adsrFilter() throws IOException {
		double duration = 4.0;
		double attack = 0.1;
		double decay = 0.16;
		double sustain = 0.04;
		double release = 1.5;

		EnvelopeSection env = envelope(c(duration), c(attack), c(decay), c(sustain), c(release));

		TimeCell clock = new TimeCell();
		Producer<PackedCollection<?>> freq = frames(clock.frame(), () -> env.get().getResultant(c(1000)));

		WaveData audio = WaveData.load(new File("Library/organ.wav"));
		cells(1, i -> audio.toCell(clock.frameScalar()))
				.addRequirement(clock)
				.f(i -> lp(freq, c(0.1)))
				.o(i -> new File("results/adsr-filter.wav"))
				.sec(4)
				.get().run();
	}

	@Test
	public void parameterizedVolumeEnvelope() {
		ParameterizedVolumeEnvelope penv = ParameterizedVolumeEnvelope.random(ParameterizedVolumeEnvelope.Mode.STANDARD_NOTE);
		PatternNoteLayer result = penv.apply(ParameterSet.random(),
				PatternNoteLayer.create("Library/organ.wav"));
		result.setTuning(new DefaultKeyboardTuning());
		new WaveData(result.getAudio(null, 4.0, null).evaluate(), 44100)
				.save(new File("results/parameterized-volume-envelope.wav"));
	}

	@Test
	public void parameterizedFilterEnvelope() {
		ParameterizedFilterEnvelope penv = ParameterizedFilterEnvelope.random(ParameterizedFilterEnvelope.Mode.STANDARD_NOTE);
		PatternNoteLayer result = penv.apply(ParameterSet.random(),
				PatternNoteLayer.create("Library/organ.wav"));
		result.setTuning(new DefaultKeyboardTuning());
		new WaveData(result.getAudio(null, 4.0, null).evaluate(), 44100)
				.save(new File("results/parameterized-filter-envelope.wav"));
	}

	@Test
	public void envelope() {
		double duration = 8.0;
		double attack = 0.5;
		double decay = 1.0;
		double sustain = 0.3;
		double release = 3.0;

		EnvelopeSection env = envelope(c(duration), c(attack), c(decay), c(sustain), c(release));

		PackedCollection<?> data = new PackedCollection<>(10 * 44100);
		data = c(p(data.traverseEach())).add(c(1.0)).get().evaluate();

		new WaveData(data, 44100)
				.sample(env).save(new File("results/envelope.wav"));
	}
}
