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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WaveDetailsNode implements Tree<WaveDetailsNode> {
	private static Comparator<Map.Entry<String, Double>> comparator =
			Map.Entry.<String, Double>comparingByValue().reversed();

	private Map<String, WaveDetailsNode> cache;
	private AudioLibrary library;
	private WaveDetails details;
	private int childLimit;

	private String resourcePath;
	private List<WaveDetailsNode> children;

	public WaveDetailsNode(AudioLibrary library, String key) {
		this(library, library.getDetailsAwait(key, false));
	}

	public WaveDetailsNode(AudioLibrary library, WaveDetails details) {
		this(library, details, 20);
	}

	public WaveDetailsNode(AudioLibrary library, WaveDetails details,
						   int childLimit) {
		this(new HashMap<>(), library, details, childLimit);
	}

	public WaveDetailsNode(Map<String, WaveDetailsNode> cache,
						   AudioLibrary library, WaveDetails details,
						   int childLimit) {
		this.cache = cache;
		this.library = library;
		this.details = details;
		this.childLimit = childLimit;
	}

	public String getResourcePath() {
		if (resourcePath == null) {
			resourcePath = ((FileWaveDataProvider) library.find(details.getIdentifier())).getResourcePath();
		}

		return resourcePath;
	}

	protected void computeChildren() {
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

	@Override
	public Collection<WaveDetailsNode> getChildren() {
		if (children == null) computeChildren();
		return children;
	}
}
