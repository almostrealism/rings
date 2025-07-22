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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AudioLibrary implements ConsoleFeatures {
	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root;
	private Map<String, WaveDetails> info;
	private Map<String, String> identifiers;

	private int sampleRate;
	private WaveDetailsFactory factory;
	private Consumer<Exception> errorListener;

	public AudioLibrary(File root, int sampleRate) {
		this(new FileWaveDataProviderNode(root), sampleRate);
	}

	public AudioLibrary(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root, int sampleRate) {
		this.root = root;
		this.info = new HashMap<>();
		this.identifiers = new HashMap<>();
		this.sampleRate = sampleRate;
		this.factory = new WaveDetailsFactory(sampleRate);
	}

	public FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> getRoot() {
		return root;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public Consumer<Exception> getErrorListener() {
		return errorListener;
	}

	public void setErrorListener(Consumer<Exception> errorListener) {
		this.errorListener = errorListener;
	}

	public WaveDetailsFactory getWaveDetailsFactory() {
		return factory;
	}

	public Collection<WaveDetails> getDetails() {
		return info.values();
	}

	public WaveDetails getDetails(String key, boolean persistent) {
		return getDetails(new FileWaveDataProvider(key), persistent);
	}

	public WaveDetails getDetails(WaveDataProvider provider) {
		return getDetails(provider, false);
	}

	/**
	 * Retrieve {@link WaveDetails} for the given {@link WaveDataProvider}.
	 *
	 * @param provider  {@link WaveDataProvider} to retrieve details for.
	 * @param persistent  If true, the details will be stored in the library for future use
	 *                    even if no associated file can be found.
	 *
	 * @return  {@link WaveDetails} for the given provider, or null if an error occurs.
	 */
	public WaveDetails getDetails(WaveDataProvider provider, boolean persistent) {
		try {
			String id = identifiers.computeIfAbsent(provider.getKey(), k -> provider.getIdentifier());

			WaveDetails details = info.computeIfAbsent(id, k -> computeDetails(provider, null, persistent));
			if (getWaveDetailsFactory().getFeatureProvider() != null && details.getFeatureData() == null) {
				details = computeDetails(provider, details, persistent);
				info.put(id, details);
			}

			details.setPersistent(persistent || details.isPersistent());
			return details;
		} catch (Exception e) {
			AudioScene.console.warn("Failed to create WaveDetails for " +
					provider.getKey() + " (" +
					Optional.ofNullable(e.getMessage()).orElse(e.getClass().getSimpleName()) + ")");
			if (!(e.getCause() instanceof IOException) || !(provider instanceof FileWaveDataProvider)) {
				if (getErrorListener() == null) {
					e.printStackTrace();
				} else {
					getErrorListener().accept(e);
				}
			}

			return null;
		}
	}

	public Map<String, Double> getSimilarities(String key) {
		return getSimilarities(new FileWaveDataProvider(key));
	}

	public Map<String, Double> getSimilarities(WaveDataProvider provider) {
		return computeSimilarities(getDetails(provider, false)).getSimilarities();
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
		if (details.getIdentifier() == null) {
			throw new IllegalArgumentException();
		}

		info.put(details.getIdentifier(), details);
	}

	protected WaveDetails computeDetails(WaveDataProvider provider, WaveDetails existing, boolean persistent) {
		WaveDetails details = factory.forProvider(provider, existing);
		details.setPersistent((existing != null && existing.isPersistent()) || persistent);
		return details;
	}

	protected WaveDetails computeSimilarities(WaveDetails details) {
		try {
			info.values().stream()
					.filter(d -> details == null || !Objects.equals(d.getIdentifier(), details.getIdentifier()))
					.filter(d -> !details.getSimilarities().containsKey(d.getIdentifier()))
					.forEach(d -> {
						double similarity = factory.similarity(details, d);
						details.getSimilarities().put(d.getIdentifier(), similarity);

						if (details.getIdentifier() != null)
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
				getDetails(provider, true);
			} finally {
				if (count > 0) {
					progress.accept(total.addAndGet(1) / count);
				}
			}
		});
	}

	public void cleanup(Predicate<String> preserve) {
		// Identify current library files
		Set<String> activeIds = root.children()
				.map(Supplier::get).filter(Objects::nonNull)
				.map(WaveDataProvider::getIdentifier).filter(Objects::nonNull)
				.collect(Collectors.toSet());

		// Exclude persistent info and those associated with active files
		// or otherwise explicitly preserved
		List<String> keys = info.entrySet().stream()
				.filter(e -> !e.getValue().isPersistent())
				.filter(e -> !activeIds.contains(e.getKey()))
				.filter(e -> preserve == null || !preserve.test(e.getKey()))
				.map(Map.Entry::getKey).toList();

		// Remove everything else
		keys.forEach(info::remove);
	}

	@Override
	public Console console() { return AudioScene.console; }
}
