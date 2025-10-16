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

package org.almostrealism.audio.data;

import io.almostrealism.relation.Tree;
import org.almostrealism.audio.AudioLibrary;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A tree node representing an audio sample ({@link WaveDetails}) with navigable
 * connections to similar audio samples from an {@link AudioLibrary}.
 * <p>
 * Each node provides access to its most similar samples as children, creating
 * a navigable similarity tree. This enables exploration of audio libraries by
 * similarity, useful for sample selection, recommendation, and interpolation.
 * <p>
 * Children are lazy-loaded and cached to optimize memory usage. Similarity
 * scores are computed by the {@link AudioLibrary#getSimilarities(WaveDetails)}
 * method, typically based on audio features like spectral characteristics.
 * <p>
 * Example usage:
 * <pre>
 * WaveDetailsNode root = new WaveDetailsNode(library, "kick_drum.wav");
 * root.prepareChildren();
 * // Navigate to most similar sample
 * WaveDetailsNode similar = root.getChildren().iterator().next();
 * </pre>
 *
 * @see AudioLibrary
 * @see WaveDetails
 * @see Tree
 */
public class WaveDetailsNode implements Tree<WaveDetailsNode> {
	/**
	 * Comparator for sorting similarity scores in descending order.
	 * Higher similarity values appear first.
	 */
	private static Comparator<Map.Entry<String, Double>> comparator =
			Map.Entry.<String, Double>comparingByValue().reversed();

	/**
	 * Shared cache of nodes to prevent duplicate node creation when
	 * the same audio sample appears in multiple branches of the tree.
	 */
	private Map<String, WaveDetailsNode> cache;

	/**
	 * The audio library containing all available samples and similarity data.
	 */
	private AudioLibrary library;

	/**
	 * The audio sample metadata represented by this node.
	 */
	private WaveDetails details;

	/**
	 * Maximum number of children (similar samples) to create for this node.
	 */
	private int childLimit;

	/**
	 * Cached file path to the audio resource. Lazy-loaded on first access.
	 */
	private String resourcePath;

	/**
	 * List of child nodes representing similar audio samples. Null until
	 * {@link #prepareChildren()} or {@link #getChildren()} is called.
	 */
	private List<WaveDetailsNode> children;

	/**
	 * Creates a node for the audio sample identified by the given key.
	 * <p>
	 * The {@link WaveDetails} are retrieved from the library synchronously.
	 * A new cache is created for this tree.
	 *
	 * @param library the audio library containing the sample
	 * @param key     the unique identifier for the audio sample
	 */
	public WaveDetailsNode(AudioLibrary library, String key) {
		this(library, library.getDetailsAwait(key, false));
	}

	/**
	 * Creates a node for the given audio sample details with default settings.
	 * <p>
	 * Uses a child limit of 20 and creates a new cache for this tree.
	 *
	 * @param library the audio library containing similar samples
	 * @param details the audio sample metadata for this node
	 */
	public WaveDetailsNode(AudioLibrary library, WaveDetails details) {
		this(library, details, 20);
	}

	/**
	 * Creates a node for the given audio sample details with a custom child limit.
	 * <p>
	 * Creates a new cache for this tree.
	 *
	 * @param library    the audio library containing similar samples
	 * @param details    the audio sample metadata for this node
	 * @param childLimit maximum number of similar samples to include as children
	 */
	public WaveDetailsNode(AudioLibrary library, WaveDetails details,
						   int childLimit) {
		this(new HashMap<>(), library, details, childLimit);
	}

	/**
	 * Creates a node with a shared cache for efficient tree construction.
	 * <p>
	 * This is the primary constructor used internally when building the tree.
	 * The cache prevents duplicate node creation when the same audio sample
	 * appears in multiple branches.
	 *
	 * @param cache      shared node cache across the entire tree
	 * @param library    the audio library containing similar samples
	 * @param details    the audio sample metadata for this node
	 * @param childLimit maximum number of similar samples to include as children
	 */
	public WaveDetailsNode(Map<String, WaveDetailsNode> cache,
						   AudioLibrary library, WaveDetails details,
						   int childLimit) {
		this.cache = cache;
		this.library = library;
		this.details = details;
		this.childLimit = childLimit;
	}

	/**
	 * Returns the file system path to the audio resource represented by this node.
	 * <p>
	 * The path is lazy-loaded and cached on first access. This assumes the
	 * underlying data provider is a {@link FileWaveDataProvider}.
	 *
	 * @return the absolute or relative path to the audio file
	 */
	public String getResourcePath() {
		if (resourcePath == null) {
			resourcePath = ((FileWaveDataProvider) library.find(details.getIdentifier())).getResourcePath();
		}

		return resourcePath;
	}

	/**
	 * Ensures that child nodes are populated.
	 * <p>
	 * If children have not been loaded yet, this triggers {@link #refreshChildren()}.
	 * This is useful for preloading the tree structure before traversal.
	 * <p>
	 * Subsequent calls have no effect - children are not refreshed unless
	 * {@link #refreshChildren()} is called directly.
	 */
	public void prepareChildren() {
		if (children == null)
			refreshChildren();
	}

	/**
	 * Refreshes the list of child nodes by querying the library for similar samples.
	 * <p>
	 * This method:
	 * <ol>
	 *   <li>Retrieves similarity scores from {@link AudioLibrary#getSimilarities(WaveDetails)}</li>
	 *   <li>Sorts samples by similarity (highest first)</li>
	 *   <li>Creates or retrieves cached nodes for the top matches</li>
	 *   <li>Limits results to {@link #childLimit}</li>
	 * </ol>
	 * <p>
	 * This method can be called multiple times to update the children based on
	 * changes to the library's similarity data.
	 */
	protected void refreshChildren() {
		children = library.getSimilarities(details).entrySet().stream()
				.sorted(comparator)
				.map(Map.Entry::getKey)
				.map(id -> cache.computeIfAbsent(id, k -> {
					WaveDetails d = library.get(k);
					return new WaveDetailsNode(cache, library, d, childLimit);
				}))
				.limit(childLimit)
				.collect(Collectors.toList());
	}

	/**
	 * Returns the child nodes representing similar audio samples.
	 * <p>
	 * Children are lazy-loaded on first access. If {@link #prepareChildren()} or
	 * {@link #refreshChildren()} has not been called, returns an empty collection.
	 * <p>
	 * The returned children are ordered by similarity, with the most similar
	 * samples appearing first.
	 *
	 * @return an immutable collection of child nodes, or empty if not yet loaded
	 */
	@Override
	public Collection<WaveDetailsNode> getChildren() {
		return children == null ? Collections.emptyList() : children;
	}
}
