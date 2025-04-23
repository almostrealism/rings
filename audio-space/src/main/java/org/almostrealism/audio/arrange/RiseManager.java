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

package org.almostrealism.audio.arrange;

import io.almostrealism.code.OperationMetadata;
import io.almostrealism.code.OperationWithInfo;
import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.data.PolymorphicAudioData;
import org.almostrealism.audio.notes.AutomatedPitchNoteAudio;
import org.almostrealism.audio.notes.NoteAudioContext;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteAudio;
import org.almostrealism.audio.pattern.PatternElement;
import org.almostrealism.audio.pattern.PatternFeatures;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.audio.synth.AudioSynthesizer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.time.Frequency;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class RiseManager implements Setup, PatternFeatures, CellFeatures {
	public static final double riseDuration = 100;

	private ConfigurableGenome genome;
	private int sampleRate;

	private OperationList setup;
	private StatelessSource generator;
	private PackedCollection<?> destination;

	public RiseManager(ConfigurableGenome genome, Supplier<AudioSceneContext> context, int sampleRate) {
		this.genome = genome;
		this.sampleRate = sampleRate;
		this.setup = new OperationList("RiseManager Setup");
		this.generator = new AudioSynthesizer(2, 2);

		PatternNoteAudio note = new AutomatedPitchNoteAudio(generator, riseDuration);
		PatternElement element = new PatternElement(new PatternNote(List.of(note)), 0.0);
		element.setAutomationParameters(new PackedCollection<>(6).fill(0.5));

		setup.add(OperationWithInfo.of(new OperationMetadata("RiseManager.render", "RiseManager.render"),
				() -> () -> {
					AudioSceneContext ctx = context.get();
					render(ctx, new NoteAudioContext(), List.of(element), false, 0);
					destination = ctx.getDestination();
				}
		));
	}

	public ConfigurableGenome getGenome() {
		return genome;
	}

	@Override
	public Supplier<Runnable> setup() { return setup; }

	public CellList getRise(int frames) {
		Producer<PackedCollection<?>> audio =
				func(shape(frames), args -> destination, false);
		return w(PolymorphicAudioData.supply(PackedCollection.factory()),
				sampleRate, frames, null, null, traverse(0, audio));
	}
}
