package org.almostrealism.audio.notes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.code.CacheManager;
import io.almostrealism.code.CachedValue;
import io.almostrealism.collect.TraversalPolicy;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.SamplingFeatures;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.SupplierWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.RAM;
import org.almostrealism.util.KeyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NoteAudioProvider implements SamplingFeatures {
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
					AudioScene.console.features(PatternNote.class).log("Cache size = " + (size / 1024 / 1024) + "mb");
			}
		});

		audioCache.setAccessListener(accessListener.get());
		audioCache.setClear(PackedCollection::destroy);
	}

	private WaveDataProvider provider;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;

	private Map<KeyPosition, Producer<PackedCollection<?>>> notes;

	public NoteAudioProvider(WaveDataProvider provider, KeyPosition<?> root) {
		this.provider = provider;
		setRoot(root);
		notes = new HashMap<>();
	}


	public WaveDataProvider getProvider() { return provider; }

	public void setProvider(WaveDataProvider provider) { this.provider = provider; }

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
	public int getSampleRate() {
		return provider.getSampleRate();
	}

	@JsonIgnore
	public double getDuration(KeyPosition<?> target) {
		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return provider.getDuration(r);
	}

	public TraversalPolicy getShape(KeyPosition<?> target) {
		double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
		return new TraversalPolicy((int) (provider.getCount() / r)).traverse(1);
	}

	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target) {
		if (target == null) {
			target = getRoot();
		}

		if (!notes.containsKey(target)) {
			notes.put(target, c(getShape(target), audioCache.get(computeAudio(target).get())));
		}

		return notes.get(target);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target) {
		return () -> args -> {
			double r = tuning.getTone(target).asHertz() / tuning.getTone(getRoot()).asHertz();
			return provider.get(r).getCollection();
		};
	}

	@JsonIgnore
	public PackedCollection<?> getAudio() {
		if (provider != null) {
			WaveData data = provider.get();
			if (data.getSampleRate() == OutputLine.sampleRate) {
				return provider.get().getCollection();
			} else {
				System.out.println("WARN: Sample rate of " + data.getSampleRate() +
						" does not match required sample rate of " + OutputLine.sampleRate);
			}
		}

		return null;
	}

	public boolean isValid() {
		Boolean valid;

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

	public static NoteAudioProvider create(String source) {
		return create(source, WesternChromatic.C1);
	}

	public static NoteAudioProvider create(String source, KeyPosition root) {
		return new NoteAudioProvider(new FileWaveDataProvider(source), root);
	}

	public static NoteAudioProvider create(Supplier<PackedCollection<?>> audioSupplier) {
		return create(audioSupplier, WesternChromatic.C1);
	}

	public static NoteAudioProvider create(Supplier<PackedCollection<?>> audioSupplier, KeyPosition root) {
		return new NoteAudioProvider(new SupplierWaveDataProvider(KeyUtils.generateKey(), audioSupplier, OutputLine.sampleRate), root);
	}
}
