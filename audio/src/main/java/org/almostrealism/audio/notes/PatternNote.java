/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.notes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.code.CachedValue;
import io.almostrealism.expression.Product;
import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.Waves;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.PassThroughProducer;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.time.computations.Interpolate;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatternNote {
	private static ContextSpecific<KernelizedEvaluable<PackedCollection<?>>> interpolate;

	static {
		interpolate = new DefaultContextSpecific<>(() ->
				new Interpolate(
						new PassThroughProducer<>(-1, 0, -1),
						new PassThroughProducer<>(1, 1),
						new PassThroughProducer<>(2, 2, -1),
						v -> new Product(v, HardwareFeatures.ops().expressionForDouble(1.0 / OutputLine.sampleRate))).get());
	}

	// TODO  Referring to a file like this should be done inside a Supplier
	// TODO  provided as an argument to the constructor. There's no reason
	// TODO  for the note to have some built in notion of audio files.
	private String source;

	private PackedCollection audio;
	private Supplier<PackedCollection> audioSupplier;

	private Boolean valid;
	private KeyPosition<?> root;

	@JsonIgnore
	private FileWaveDataProvider provider;

	private KeyboardTuning tuning;
	private Map<KeyPosition, CachedValue<PackedCollection>> notes;

	public PatternNote() { this((String) null); }

	public PatternNote(String source) {
		this(source, WesternChromatic.C1);
	}

	public PatternNote(PackedCollection audio) {
		this(audio, WesternChromatic.C1);
	}

	public PatternNote(Supplier<PackedCollection> audioSupplier) {
		this(audioSupplier, WesternChromatic.C1);
	}

	public PatternNote(String source, KeyPosition root) {
		setSource(source);
		setRoot(root);
		notes = new HashMap<>();
	}

	public PatternNote(PackedCollection audio, KeyPosition root) {
		setAudio(audio);
		setRoot(root);
		notes = new HashMap<>();
	}

	public PatternNote(Supplier<PackedCollection> audioSupplier, KeyPosition root) {
		this.audioSupplier = audioSupplier;
		setRoot(root);
		notes = new HashMap<>();
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
		this.valid = null;
	}

	public KeyPosition<?> getRoot() { return root; }

	public void setRoot(KeyPosition<?> root) { this.root = root; }

	@JsonIgnore
	public void setTuning(KeyboardTuning tuning) {
		if (tuning != this.tuning) {
			this.tuning = tuning;
			notes.clear();
		}
	}

	@JsonIgnore
	public double getDuration() {
		return getAudio().getMemLength() / (double) OutputLine.sampleRate;
	}

	public Producer<PackedCollection> getAudio(KeyPosition<?> target, int length) {
		return () -> {
			Evaluable<PackedCollection> audio = getAudio(target).get();
			return args -> audio.evaluate().range(new TraversalPolicy(length));
		};
	}

	public CachedValue<PackedCollection> getAudio(KeyPosition<?> target) {
		if (!notes.containsKey(target)) {
			notes.put(target, new CachedValue<>(args -> {
				double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();

				PackedCollection<?> rate = new PackedCollection(1);
				rate.setMem(0, r);

				PackedCollection<?> audio = getAudio();
				PackedCollection<?> dest = WaveData.allocateCollection((int) (r * audio.getMemLength()));

				interpolate.getValue().kernelEvaluate(dest.traverse(1), audio.traverse(0), WaveOutput.timelineScalar.getValue(), rate.traverse(0));
				return dest;
			}));
		}

		return notes.get(target);
	}

	@JsonIgnore
	public PackedCollection getAudio() {
		if (audio == null) {
			if (audioSupplier == null) {
				if (provider == null) provider = new FileWaveDataProvider(source);

				WaveData data = provider.get();
				if (data.getSampleRate() == OutputLine.sampleRate) {
					audio = provider.get().getCollection();
				} else {
					System.out.println("WARN: Sample rate of " + data.getSampleRate() +
							" does not match required sample rate of " + OutputLine.sampleRate);
				}
			} else {
				audio = audioSupplier.get();
			}
		}

		return audio;
	}

	@JsonIgnore
	public void setAudio(PackedCollection audio) {
		this.audio = audio;
	}

	public boolean isValid() {
		if (audio != null || audioSupplier != null) return true;
		if (valid != null) return valid;
		valid = Waves.isValid(new File(source), w -> w.getSampleRate() == OutputLine.sampleRate);
		return valid;
	}
}
