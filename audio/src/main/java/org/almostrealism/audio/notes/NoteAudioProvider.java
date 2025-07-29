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

package org.almostrealism.audio.notes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.code.CacheManager;
import io.almostrealism.code.CachedValue;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Producer;
import io.almostrealism.relation.Validity;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.DelegateWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.SupplierWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.mem.RAM;
import org.almostrealism.io.Console;
import org.almostrealism.util.KeyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NoteAudioProvider implements NoteAudio, Validity, Comparable<NoteAudioProvider>, SamplingFeatures {
	public static boolean enableVerbose = false;

	private static CacheManager<PackedCollection<?>> audioCache = new CacheManager<>();

	static {
		OperationList accessListener = new OperationList();
		accessListener.add(() -> CacheManager.maxCachedEntries(audioCache, 200));
		accessListener.add(() -> () -> {
			if (Math.random() < 0.005) {
				long size = audioCache.getCachedOrdered().stream()
						.map(CachedValue::evaluate)
						.map(PackedCollection::getMem)
						.mapToLong(m -> m instanceof RAM ? ((RAM) m).getSize() : 0)
						.sum();
				if (enableVerbose && size > 1024)
					CellFeatures.console.features(NoteAudioProvider.class).log("Cache size = " + (size / 1024 / 1024) + "mb");
			}
		});

		audioCache.setAccessListener(accessListener.get());
		audioCache.setValid(c -> !c.isDestroyed());
		audioCache.setClear(c -> {
			// If the cached value is a subset of another value,
			// it does not make sense to destroy it just because
			// it is no longer being stored in the cache
			if (c.getRootDelegate().getMemLength() == c.getMemLength()) {
				c.destroy();
			}
		});
	}

	private WaveDataProvider provider;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;
	private Double bpm;

	private Map<KeyPosition, Producer<PackedCollection<?>>> notes;

	public NoteAudioProvider(WaveDataProvider provider, KeyPosition<?> root) {
		this(provider, root, null);
	}

	public NoteAudioProvider(WaveDataProvider provider, KeyPosition<?> root, Double bpm) {
		this(provider, root, bpm, null);
	}

	public NoteAudioProvider(WaveDataProvider provider, KeyPosition<?> root, Double bpm, KeyboardTuning tuning) {
		this.provider = provider;
		this.tuning = tuning;
		setRoot(root);
		setBpm(bpm);
		notes = new HashMap<>();
	}

	public WaveDataProvider getProvider() { return provider; }
	public void setProvider(WaveDataProvider provider) { this.provider = provider; }

	public KeyPosition<?> getRoot() { return root; }
	public void setRoot(KeyPosition<?> root) { this.root = root; }

	public Double getBpm() { return bpm; }
	public void setBpm(Double bpm) { this.bpm = bpm; }

	@JsonIgnore
	@Override
	public void setTuning(KeyboardTuning tuning) {
		if (tuning != this.tuning) {
			this.tuning = tuning;
			notes.clear();
		}
	}

	@JsonIgnore
	public KeyboardTuning getTuning() { return tuning; }

	@JsonIgnore
	@Override
	public int getSampleRate() {
		return provider.getSampleRate();
	}

	@JsonIgnore
	@Override
	public double getDuration(KeyPosition<?> target) {
		if (target == null) return provider.getDuration();

		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return provider.getDuration(r);
	}

	public TraversalPolicy getShape(KeyPosition<?> target) {
		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return new TraversalPolicy((int) (provider.getCount() / r)).traverse(1);
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, int channel) {
		if (target == null) {
			target = getRoot();
		}

		if (!notes.containsKey(target)) {
			notes.put(target, c(getShape(target), audioCache.get(computeAudio(target, channel).get())));
		}

		return notes.get(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, int channel) {
		return () -> args -> {
			double r = target == null ? 1.0 :
					(tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz());
			return provider.getChannelData(channel, r);
		};
	}

	@JsonIgnore
	public WaveData getWaveData() {
		if (provider != null) {
			WaveData data = provider.get();

			if (data == null) {
				throw new UnsupportedOperationException();
			}

			if (data.getSampleRate() == OutputLine.sampleRate) {
				return data;
			} else {
				warn("Sample rate of " + data.getSampleRate() +
						" does not match required sample rate of " + OutputLine.sampleRate);
			}
		}

		return null;
	}

	@Override
	public boolean isValid() {
		Boolean valid;

		try {
			valid = provider.getSampleRate() == OutputLine.sampleRate;
		} catch (Exception e) {
			if (provider instanceof FileWaveDataProvider) {
				warn(e.getMessage() + "(" + ((FileWaveDataProvider) provider).getResourcePath() + ")");
			} else {
				warn(e.getMessage());
			}

			valid = false;
		}

		return valid;
	}

	public List<NoteAudioProvider> split(double durationBeats) {
		if (getBpm() == null)
			throw new IllegalArgumentException();

		double duration = durationBeats * 60.0 / getBpm();
		int frames = (int) (duration * OutputLine.sampleRate);
		int total = (int) (getProvider().getCount() / (duration * OutputLine.sampleRate));
		return IntStream.range(0, total)
				.mapToObj(i -> new DelegateWaveDataProvider(getProvider(), i * frames, frames))
				.map(p -> new NoteAudioProvider(p, getRoot(), getBpm(), tuning))
				.collect(Collectors.toList());
	}

	@Override
	public int compareTo(NoteAudioProvider o) {
		return getProvider().compareTo(o.getProvider());
	}

	@Override
	public Console console() {
		return CellFeatures.console;
	}

	public static NoteAudioProvider create(String source) {
		return create(source, WesternChromatic.C1);
	}

	public static NoteAudioProvider create(String source, KeyPosition root) {
		return new NoteAudioProvider(new FileWaveDataProvider(source), root);
	}

	public static NoteAudioProvider create(String source, KeyPosition root, KeyboardTuning tuning) {
		return new NoteAudioProvider(new FileWaveDataProvider(source), root, null, tuning);
	}

	public static NoteAudioProvider create(Supplier<PackedCollection<?>> audioSupplier) {
		return create(audioSupplier, WesternChromatic.C1);
	}

	public static NoteAudioProvider create(Supplier<PackedCollection<?>> audioSupplier, KeyPosition root) {
		return new NoteAudioProvider(new SupplierWaveDataProvider(KeyUtils.generateKey(), audioSupplier, OutputLine.sampleRate), root);
	}
}
