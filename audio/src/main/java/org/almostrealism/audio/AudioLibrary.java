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
import org.almostrealism.audio.data.WaveDetailsJob;
import org.almostrealism.io.ConsoleFeatures;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AudioLibrary implements ConsoleFeatures {
	public static double BACKGROUND_PRIORITY = 0.0;
	public static double DEFAULT_PRIORITY = 0.5;
	public static double HIGH_PRIORITY = 1.0;

	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root;
	private int sampleRate;

	private Map<String, String> identifiers;
	private Map<String, WaveDetails> info;

	private WaveDetailsFactory factory;
	private PriorityBlockingQueue<WaveDetailsJob> queue;
	private int totalJobs;

	private DoubleConsumer progressListener;
	private Consumer<Exception> errorListener;
	private ExecutorService executor;

	public AudioLibrary(File root, int sampleRate) {
		this(new FileWaveDataProviderNode(root), sampleRate);
	}

	public AudioLibrary(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root, int sampleRate) {
		this.root = root;
		this.sampleRate = sampleRate;
		this.identifiers = new HashMap<>();
		this.info = new HashMap<>();
		this.factory = new WaveDetailsFactory(sampleRate);
		this.queue = new PriorityBlockingQueue<>(100, Comparator.comparing(WaveDetailsJob::getPriority).reversed());

		start();
	}

	public void start() {
		if (executor != null) return;

		executor = new ThreadPoolExecutor(1, 1,
				60, TimeUnit.MINUTES, (BlockingQueue) queue);
	}

	public FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> getRoot() {
		return root;
	}

	public int getSampleRate() { return sampleRate; }

	public DoubleConsumer getProgressListener() { return progressListener; }
	public void setProgressListener(DoubleConsumer progressListener) {
		this.progressListener = progressListener;
	}

	public Consumer<Exception> getErrorListener() { return errorListener; }
	public void setErrorListener(Consumer<Exception> errorListener) {
		this.errorListener = errorListener;
	}

	public WaveDetailsFactory getWaveDetailsFactory() { return factory; }

	public Collection<WaveDetails> getAllDetails() { return info.values(); }

	public Optional<WaveDetails> getDetailsNow(String key) {
		return getDetailsNow(new FileWaveDataProvider(key));
	}

	public Optional<WaveDetails> getDetailsNow(String key, boolean persistent) {
		return getDetailsNow(new FileWaveDataProvider(key), persistent);
	}

	public Optional<WaveDetails> getDetailsNow(WaveDataProvider provider) {
		return getDetailsNow(provider, false);
	}

	public Optional<WaveDetails> getDetailsNow(WaveDataProvider provider, boolean persistent) {
		return Optional.ofNullable(getDetails(provider, persistent, DEFAULT_PRIORITY).getNow(null));
	}

	public WaveDetails getDetailsAwait(String key, boolean persistent) {
		return getDetailsAwait(new FileWaveDataProvider(key), persistent);
	}

	public WaveDetails getDetailsAwait(WaveDataProvider provider) {
		return getDetailsAwait(provider, false);
	}

	public WaveDetails getDetailsAwait(WaveDataProvider provider, boolean persistent) {
		try {
			return getDetails(provider, persistent, HIGH_PRIORITY).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public void getDetails(String file, Consumer<WaveDetails> consumer, boolean priority) {
		getDetails(new FileWaveDataProvider(file), consumer, priority);
	}

	public void getDetails(WaveDataProvider provider, Consumer<WaveDetails> consumer, boolean priority) {
		getDetails(provider, false, priority ? HIGH_PRIORITY : DEFAULT_PRIORITY).thenAccept(consumer);
	}

	/**
	 * Retrieve {@link WaveDetails} for the given {@link WaveDataProvider}, queueing its computation
	 * if it is not already available.
	 *
	 * @param provider  {@link WaveDataProvider} to retrieve details for.
	 * @param persistent  If true, the details will be stored in the library for future use
	 *                    even if no associated file can be found.
	 *
	 * @return  {@link CompletableFuture} with the {@link WaveDetails} for the given provider.
	 */
	protected CompletableFuture<WaveDetails> getDetails(WaveDataProvider provider, boolean persistent, double priority) {
		WaveDetails existing = info.get(provider.getIdentifier());

		if (existing == null) {
			return submitJob(provider, persistent, priority).getFuture();
		} else {
			existing.setPersistent(persistent || existing.isPersistent());
			return CompletableFuture.completedFuture(existing);
		}
	}

	/**
	 * Compute {@link WaveDetails} for the given {@link WaveDataProvider}.
	 *
	 * @param provider  {@link WaveDataProvider} to retrieve details for.
	 * @param persistent  If true, the details will be stored in the library for future use
	 *                    even if no associated file can be found.
	 *
	 * @return  {@link WaveDetails} for the given provider, or null if an error occurs.
	 */
	protected WaveDetails computeDetails(WaveDataProvider provider, boolean persistent) {
		try {
			String id = provider.getIdentifier();

			WaveDetails details = info.computeIfAbsent(id, k -> computeDetails(provider, null, persistent));
			if (getWaveDetailsFactory().getFeatureProvider() != null && details.getFeatureData() == null) {
				details = computeDetails(provider, details, persistent);
				info.put(id, details);
			}

			details.setPersistent(persistent || details.isPersistent());
			return details;
		} catch (Exception e) {
			warn("Failed to create WaveDetails for " +
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
		return computeSimilarities(getDetailsAwait(provider, false)).getSimilarities();
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

	protected WaveDetails processJob(WaveDetailsJob job) {
		if (job == null) return null;

		try {
			return computeDetails(job.getTarget(), job.isPersistent());
		} finally {
			if (progressListener != null) {
				double total = totalJobs <= 0 ? 1.0 : totalJobs;
				double remaining = queue.size() / total;

				if (remaining <= 1.0) {
					progressListener.accept(1.0 - remaining);
				} else {
					progressListener.accept(-1.0);
				}
			}
		}
	}

	protected WaveDetailsJob submitJob(WaveDataProvider provider, boolean persistent, double priority) {
		return submitJob(new WaveDetailsJob(this::processJob, provider, persistent, priority));
	}

	protected WaveDetailsJob submitJob(WaveDetailsJob job) {
		if (job.getTarget() != null) {
			identifiers.computeIfAbsent(job.getTarget().getKey(), k -> job.getTarget().getIdentifier());
		}

		executor.execute(job);
		totalJobs++;
		return job;
	}

	public void stop() {
		executor.shutdown();

		try {
			if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
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

						if (Math.abs(similarity - 1.0) < 1e-5) {
							warn("Identical features for distinct files");
						}

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

	public CompletableFuture<Void> refresh() {
		return refresh(null);
	}

	public CompletableFuture<Void> refresh(DoubleConsumer progress) {
		double count = progress == null ? 0 : root.children().count();
		if (count > 0) {
			progress.accept(0.0);
		}

		CompletableFuture<Void> future = new CompletableFuture<>();

		AtomicLong total = new AtomicLong(0);

		root.children().forEach(f -> {
			FileWaveDataProvider provider = f.get();

			try {
				if (provider == null) return;
				submitJob(new WaveDetailsJob(this::processJob, provider, true, BACKGROUND_PRIORITY));
			} finally {
				if (count > 0) {
					progress.accept(total.addAndGet(1) / count);
				}
			}
		});

		WaveDetailsJob last = new WaveDetailsJob(this::processJob, null, false, -1.0);
		last.getFuture().thenRun(() -> future.complete(null));
		executor.execute(last);
		return future;
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
}
