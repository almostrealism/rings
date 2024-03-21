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
import org.almostrealism.audio.data.WaveDetails;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;

public class AudioLibrary {
	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root;
	private Map<String, WaveDetails> info;

	public AudioLibrary(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root) {
		this.root = root;
		this.info = new HashMap<>();
	}

	public FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> getRoot() {
		return root;
	}

	public Collection<WaveDetails> getDetails() {
		return info.values();
	}

	public void refresh() {
		root.children().forEach(f -> {
			FileWaveDataProvider provider = f.get();
			if (provider == null) return;

			try {
				WaveDetails details = WaveDetails.create(provider);
				if (details != null) {
					info.put(details.getIdentifier(), details);
				}
			} catch (Exception e) {
				AudioScene.console.warn("Failed to create WaveDetails for " +
						provider.getKey() + " (" + e.getMessage() + ")");
			}
		});
	}

	public static AudioLibrary load(File root) {
		return load(new FileWaveDataProviderNode(root), null);
	}

	public static AudioLibrary load(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root) {
		return load(root, null);
	}

	public static AudioLibrary load(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> root,
									DoubleConsumer progress) {
		return new AudioLibrary(root);
	}
}
