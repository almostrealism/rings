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

package org.almostrealism.audio;

import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.data.FileWaveDataProviderTree;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.audio.data.WaveDetailsFactory;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

public class AudioLibrary implements ConsoleFeatures {
	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root;
	private Map<String, WaveDetails> info;
	private Map<String, String> identifiers;

	private int sampleRate;
	private WaveDetailsFactory factory;

	public AudioLibrary(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root, int sampleRate) {
		this.root = root;
		this.info = new HashMap<>();
		this.identifiers = new HashMap<>();
		this.sampleRate = sampleRate;
		this.factory = new WaveDetailsFactory(16, 0.125, sampleRate);
	}

	public FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> getRoot() {
		return root;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public Collection<WaveDetails> getDetails() {
		return info.values();
	}

	public WaveDetails getDetails(String key) {
		return getDetails(new FileWaveDataProvider(key));
	}

	public WaveDetails getDetails(WaveDataProvider provider) {
		try {
			String id = identifiers.computeIfAbsent(provider.getKey(), k -> provider.getIdentifier());
			return info.computeIfAbsent(id, k -> computeDetails(provider));
		} catch (Exception e) {
			AudioScene.console.warn("Failed to create WaveDetails for " +
					provider.getKey() + " (" +
					Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName()) + ")");
			if (!(e.getCause() instanceof IOException) || !(provider instanceof FileWaveDataProvider)) {
				e.printStackTrace();
			}

			return null;
		}
	}

	public Map<String, Double> getSimilarities(String key) {
		return getSimilarities(new FileWaveDataProvider(key));
	}

	public Map<String, Double> getSimilarities(WaveDataProvider provider) {
		return computeSimilarities(getDetails(provider)).getSimilarities();
	}

	public WaveDataProvider find(String identifier) {
		return root.children()
				.map(Supplier::get)
				.filter(Objects::nonNull)
				.filter(f -> Objects.equals(identifier, f.getIdentifier()))
				.findFirst()
				.orElse(null);
	}

	public void include(WaveDetails details) {
		info.put(details.getIdentifier(), details);
	}

	protected WaveDetails computeDetails(WaveDataProvider provider) {
		return factory.forProvider(provider);
	}

	protected WaveDetails computeSimilarities(WaveDetails details) {
		try {
			info.values().stream()
					.filter(d -> !Objects.equals(d.getIdentifier(), details.getIdentifier()))
					.filter(d -> !details.getSimilarities().containsKey(d.getIdentifier()))
					.forEach(d -> {
						double similarity = factory.similarity(details, d);
						details.getSimilarities().put(d.getIdentifier(), similarity);
						d.getSimilarities().put(details.getIdentifier(), similarity);
					});
		} catch (Exception e) {
			log("Failed to load similarities for " + details.getIdentifier() +
					" (" + Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName()) + ")");
		}

		return details;
	}

	public void refresh() {
		refresh(null);
	}

	public void refresh(DoubleConsumer progress) {
		double count = progress == null ? 0 : root.children().count();
		if (count > 0) {
			progress.accept(0.0);
		}

		AtomicLong total = new AtomicLong(0);

		root.children().forEach(f -> {
			FileWaveDataProvider provider = f.get();

			try {
				if (provider == null) return;
				getDetails(provider);
			} finally {
				if (count > 0) {
					progress.accept(total.addAndGet(1) / count);
				}
			}
		});
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}

	public static AudioLibrary load(File root, int sampleRate) {
		return load(new FileWaveDataProviderNode(root), sampleRate, null);
	}

	public static AudioLibrary load(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root, int sampleRate) {
		return load(root, sampleRate, null);
	}

	public static AudioLibrary load(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root, int sampleRate,
									DoubleConsumer progress) {
		return new AudioLibrary(root, sampleRate);
	}
}
