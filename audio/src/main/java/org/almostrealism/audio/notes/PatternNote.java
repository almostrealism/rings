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
import io.almostrealism.code.CacheManager;
import io.almostrealism.code.CachedValue;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.StaticWaveDataProvider;
import org.almostrealism.audio.data.SupplierWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.pattern.NoteAudioFilter;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.RAM;
import org.almostrealism.hardware.cl.CLMemory;
import io.almostrealism.relation.Factor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PatternNote implements CellFeatures, SamplingFeatures {

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
				if (size > 1024)
					System.out.println("PatternNote: Cache size = " + (size / 1024 / 1024) + "mb");
			}
		});

		audioCache.setAccessListener(accessListener.get());
		audioCache.setClear(PackedCollection::destroy);
	}

	private WaveDataProvider provider;
	private PackedCollection<?> audio;

	private PatternNote delegate;
	private NoteAudioFilter filter;

	private Boolean valid;
	private KeyPosition<?> root;

	private KeyboardTuning tuning;
	private Map<KeyPosition, Producer<PackedCollection<?>>> notes;

	public PatternNote() { this(null, WesternChromatic.C1); }

	public PatternNote(WaveDataProvider provider, KeyPosition root) {
		this.provider = provider;
		setRoot(root);
		notes = new HashMap<>();
	}

	protected PatternNote(PatternNote delegate, NoteAudioFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
		setRoot(delegate.getRoot());
	}

	@Deprecated
	public String getSource() {
		if (provider instanceof FileWaveDataProvider) {
			return ((FileWaveDataProvider) provider).getResourcePath();
		}

		return null;
	}

	@Deprecated
	public void setSource(String source) {
		this.valid = null;

		if (source == null) return;
		if (provider == null) provider = new FileWaveDataProvider(source);

		try {
			WaveData data = provider.get();

			if (data.getSampleRate() == OutputLine.sampleRate) {
				audio = data.getCollection();
			} else {
				System.out.println("WARN: Sample rate of " + data.getSampleRate() +
						" does not match required sample rate of " + OutputLine.sampleRate);
				valid = false;
			}
		} catch (RuntimeException e) {
			valid = false;
		}
	}

	public WaveDataProvider getProvider() { return provider; }

	public void setProvider(WaveDataProvider provider) { this.provider = provider; }

	public KeyPosition<?> getRoot() { return root; }

	public void setRoot(KeyPosition<?> root) { this.root = root; }

	@JsonIgnore
	public void setTuning(KeyboardTuning tuning) {
		if (delegate != null) {
			delegate.setTuning(tuning);
		} else if (tuning != this.tuning) {
			this.tuning = tuning;
			notes.clear();
		}
	}

	@JsonIgnore
	public int getSampleRate() {
		if (delegate != null) return delegate.getSampleRate();
		return provider.getSampleRate();
	}

	@JsonIgnore
	public double getDuration(KeyPosition<?> target) {
		if (delegate != null) return delegate.getDuration(target);

		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return provider.getDuration(r);
	}

	public TraversalPolicy getShape(KeyPosition<?> target) {
		if (delegate != null) return delegate.getShape(target);

		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return new TraversalPolicy((int) (provider.getCount() / r)).traverse(1);
	}

	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, double noteDuration) {
		if (delegate == null) {
			System.out.println("WARN: No AudioNoteFilter for PatternNote, note duration will be ignored");
			return getAudio(target);
		} else {
			// System.out.println("PatternNote: duration = " + noteDuration);
			return computeAudio(target, noteDuration);
		}
	}

	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target) {
		if (delegate != null) {
			return delegate.getAudio(target);
		} else if (!notes.containsKey(target)) {
			notes.put(target, c(getShape(target), audioCache.get(computeAudio(target, -1.0).get())));
		}

		return notes.get(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, double noteDuration) {
		if (delegate == null) {
			return () -> args -> {
				double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
				return provider.get(r).getCollection();
			};
		} else if (noteDuration > 0) {
			return sampling(getSampleRate(), getDuration(target),
					() -> filter.apply(delegate.getAudio(target), c(noteDuration)));
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@JsonIgnore
	public PackedCollection<?> getAudio() {
		if (audio == null && provider != null) {
			WaveData data = provider.get();
			if (data.getSampleRate() == OutputLine.sampleRate) {
				audio = provider.get().getCollection();
			} else {
				System.out.println("WARN: Sample rate of " + data.getSampleRate() +
						" does not match required sample rate of " + OutputLine.sampleRate);
			}
		}

		return audio;
	}

	public boolean isValid() {
		if (delegate != null) return delegate.isValid();
		if (audio != null) return true;
		if (valid != null) return valid;

		try {
			valid = provider.getSampleRate() == OutputLine.sampleRate;
		} catch (Exception e) {
			if (provider instanceof FileWaveDataProvider) {
				System.out.println("WARN: " + e.getMessage() + "(" + ((FileWaveDataProvider) provider).getResourcePath() + ")");
			} else {
				System.out.println("WARN: " + e.getMessage());
			}

			valid = false;
		}

		return valid;
	}

	public static PatternNote create(String source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNote create(String source, KeyPosition root) {
		return new PatternNote(new FileWaveDataProvider(source), root);
	}

	public static PatternNote create(WaveData source) {
		return create(source, WesternChromatic.C1);
	}

	public static PatternNote create(WaveData source, KeyPosition root) {
		return new PatternNote(new StaticWaveDataProvider(source), root);
	}

	public static PatternNote create(PatternNote delegate, NoteAudioFilter filter) {
		return new PatternNote(delegate, filter);
	}

	public static PatternNote create(PatternNote delegate, Factor<PackedCollection<?>> factor) {
		return new PatternNote(delegate, (audio, duration) -> factor.getResultant(audio));
	}

	public static PatternNote create(Supplier<PackedCollection<?>> audioSupplier) {
		return create(audioSupplier, WesternChromatic.C1);
	}

	public static PatternNote create(Supplier<PackedCollection<?>> audioSupplier, KeyPosition root) {
		return new PatternNote(new SupplierWaveDataProvider(audioSupplier, OutputLine.sampleRate), root);
	}
}
