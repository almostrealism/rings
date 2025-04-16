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
import io.almostrealism.uml.Named;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.data.FileWaveDataProviderFilter;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.data.FileWaveDataProviderTree;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TreeNoteSource extends NoteAudioSourceBase implements Named, ConsoleFeatures {
	public static boolean alwaysComputeNotes = false;

	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree;
	private List<NoteAudioProvider> providers;
	private List<NoteAudioProvider> notes;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;
	private Double bpm;
	private Double splitDurationBeats;
	private boolean useSynthesizer;
	private boolean forwardPlayback;
	private boolean reversePlayback;

	private List<FileWaveDataProviderFilter> filters;

	public TreeNoteSource() { this((FileWaveDataProviderTree) null); }


	public TreeNoteSource(KeyPosition<?> root) {
		this(null, root);
	}

	public TreeNoteSource(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree) {
		this(tree, WesternChromatic.C1);
	}

	public TreeNoteSource(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree,
						  KeyPosition<?> root) {
		this.filters = new ArrayList<>();
		setForwardPlayback(true);
		setTree(tree);
		setRoot(root);
	}

	public KeyPosition<?> getRoot() { return root; }
	public void setRoot(KeyPosition<?> root) { this.root = root; }

	public Double getBpm() { return bpm; }
	public void setBpm(Double bpm) {
		if (!Objects.equals(this.bpm, bpm)) {
			this.bpm = bpm;

			if (providers != null) {
				providers.forEach(n -> n.setBpm(bpm));
			}

			computeNotes();
		}
	}

	public Double getSplitDurationBeats() { return splitDurationBeats; }
	public void setSplitDurationBeats(Double splitDurationBeats) {
		if (!Objects.equals(this.splitDurationBeats, splitDurationBeats)) {
			this.splitDurationBeats = splitDurationBeats;
			computeNotes();
		}
	}

	@JsonIgnore
	public FileWaveDataProviderTree<?> getTree() { return tree; }

	@JsonIgnore
	public void setTree(FileWaveDataProviderTree tree) {
		this.tree = tree;
		if (!alwaysComputeNotes) computeProviders();
	}

	public List<FileWaveDataProviderFilter> getFilters() { return filters; }

	@Override
	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;

		if (notes != null) {
			for (NoteAudioProvider note : notes) {
				note.setTuning(tuning);
			}
		}
	}

	@JsonIgnore
	@Override
	public String getName() {
		if (filters.isEmpty()) return getOrigin();
		if (filters.size() > 1) return getOrigin() + " (" + filters.size() + " filters)";
		return filters.get(0).getFilterOn().readableName() + " " +
				filters.get(0).getFilterType().readableName() + " \"" +
				filters.get(0).getFilter() + "\"";
	}

	public String getOrigin() { return tree instanceof Named ? ((Named) tree).getName() : ""; }

	@JsonIgnore
	public List<NoteAudio> getNotes() {
		if (alwaysComputeNotes) computeProviders();
		return notes == null ? Collections.emptyList() : Collections.unmodifiableList(notes);
	}

	@Override
	public boolean isUseSynthesizer() { return useSynthesizer; }

	public void setUseSynthesizer(boolean useSynthesizer) {
		this.useSynthesizer = useSynthesizer;
	}

	@Override
	public boolean isForwardPlayback() {
		return forwardPlayback;
	}

	public void setForwardPlayback(boolean forwardPlayback) {
		this.forwardPlayback = forwardPlayback;
	}

	@Override
	public boolean isReversePlayback() {
		return reversePlayback;
	}

	public void setReversePlayback(boolean reversePlayback) {
		this.reversePlayback = reversePlayback;
	}

	public void refresh() {
		if (alwaysComputeNotes) {
			providers = null;
		} else {
			computeProviders();
		}
	}

	private void computeProviders() {
		if (filters == null) {
			return;
		}

		providers = new ArrayList<>();

		if (tree != null) {
			tree.children().forEach(f -> {
				FileWaveDataProvider p = f.get();

				try {
					if (p == null) {
						return;
					}

					if (p.getSampleRate() != OutputLine.sampleRate) {
						return;
					}

					boolean match = filters.stream()
							.map(filter -> filter.matches(tree, p))
							.reduce((a, b) -> a & b)
							.orElse(true);

					if (match) {
						providers.add(new NoteAudioProvider(p, getRoot(), getBpm()));
					}
				} catch (Exception e) {
					warn(e.getMessage() + "(" + p.getResourcePath() + ")");
				}
			});

			Collections.sort(providers);
		}

		computeNotes();
	}

	private void computeNotes() {
		if (providers == null) {
			notes = null;
			return;
		}

		if (getSplitDurationBeats() == null || getBpm() == null) {
			providers.forEach(n -> n.setTuning(tuning));
			notes = providers;
		} else {
			notes = providers.stream().flatMap(n -> {
				n.setTuning(tuning);
				return n.split(getSplitDurationBeats()).stream();
			}).collect(Collectors.toList());
		}
	}

	public boolean checkResourceUsed(String canonicalPath) {
		if (providers == null) computeProviders();

		boolean match = providers.stream().anyMatch(note -> {
			if (note.getProvider() instanceof FileWaveDataProvider) {
				return ((FileWaveDataProvider) note.getProvider())
						.getResourcePath().equals(canonicalPath);
			} else {
				return false;
			}
		});

		return match;
	}

	@Override
	public Console console() {
		return CellFeatures.console;
	}

	public static TreeNoteSource fromFile(File root, FileWaveDataProviderFilter filter) {
		TreeNoteSource t = new TreeNoteSource(new FileWaveDataProviderNode(root));
		t.getFilters().add(filter);
		return t;
	}
}
