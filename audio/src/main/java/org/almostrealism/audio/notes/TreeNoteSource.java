/*
 * Copyright 2024 Michael Murray
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
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TreeNoteSource implements NoteAudioSource, Named, ConsoleFeatures {
	public static boolean alwaysComputeNotes = false;

	private FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree;
	private List<NoteAudioProvider> providers;
	private List<NoteAudioProvider> notes;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;
	private Double bpm;
	private Double splitDurationBeats;

	private List<Filter> filters;

	public TreeNoteSource() { this(null); }

	public TreeNoteSource(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree) {
		this(tree, WesternChromatic.C1);
	}

	public TreeNoteSource(FileWaveDataProviderTree<? extends Supplier<FileWaveDataProvider>> tree, KeyPosition<?> root) {
		setTree(tree);
		setRoot(root);
		this.filters = new ArrayList<>();
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

	public List<Filter> getFilters() { return filters; }

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
		return filters.get(0).filterOn.readableName() + " " + filters.get(0).filterType.readableName() + " \"" + filters.get(0).filter + "\"";
	}

	public String getOrigin() { return tree instanceof Named ? ((Named) tree).getName() : ""; }

	@JsonIgnore
	public List<NoteAudioProvider> getNotes() {
		if (alwaysComputeNotes) computeProviders();
		return notes == null ? new ArrayList<>() : notes;
	}

	public void refresh() {
		if (alwaysComputeNotes) {
			providers = null;
		} else {
			computeProviders();
		}
	}

	private void computeProviders() {
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
							.map(filter -> filter.filterType.matches(filter.filterOn.select(tree, p), filter.filter))
							.reduce((a, b) -> a & b)
							.orElse(true);

					if (match) {
						providers.add(new NoteAudioProvider(p, getRoot(), getBpm()));
					}
				} catch (Exception e) {
					warn(e.getMessage() + "(" + p.getResourcePath() + ")");
				}
			});
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

	public static TreeNoteSource fromFile(File root, Filter filter) {
		TreeNoteSource t = new TreeNoteSource(new FileWaveDataProviderNode(root));
		t.getFilters().add(filter);
		return t;
	}

	public enum FilterOn {
		PATH, NAME;

		String select(FileWaveDataProviderTree tree, FileWaveDataProvider p) {
			return switch (this) {
				case NAME -> new File(p.getResourcePath()).getName();
				case PATH -> new File(tree.getRelativePath(p.getResourcePath())).getParentFile().getPath();
			};
		}

		public String readableName() {
			return switch (this) {
				case NAME -> "File Name";
				case PATH -> "File Path";
			};
		}

		public static FilterOn fromReadableName(String name) {
			return switch (name) {
				case "File Name" -> NAME;
				case "File Path" -> PATH;
				default -> null;
			};
		}
	}

	public enum FilterType {
		EQUALS, EQUALS_IGNORE_CASE, STARTS_WITH, ENDS_WITH, CONTAINS, CONTAINS_IGNORE_CASE;

		boolean matches(String value, String filter) {
			if (filter == null || filter.isEmpty()) return false;

			return switch (this) {
				case EQUALS -> value.equals(filter);
				case EQUALS_IGNORE_CASE -> value.equalsIgnoreCase(filter);
				case STARTS_WITH -> value.startsWith(filter);
				case ENDS_WITH -> value.endsWith(filter);
				case CONTAINS -> value.contains(filter);
				case CONTAINS_IGNORE_CASE -> value.toLowerCase().contains(filter.toLowerCase());
			};
		}

		public String readableName() {
			return switch (this) {
				case EQUALS -> "Exactly Matches";
				case EQUALS_IGNORE_CASE -> "Matches (Case Insensitive)";
				case STARTS_WITH -> "Starts With";
				case ENDS_WITH -> "Ends With";
				case CONTAINS -> "Contains";
				case CONTAINS_IGNORE_CASE -> "Contains (Case Insensitive)";
			};
		}

		public static FilterType fromReadableName(String name) {
			return switch (name) {
				case "Exactly Matches" -> EQUALS;
				case "Matches (Case Insensitive)" -> EQUALS_IGNORE_CASE;
				case "Starts With" -> STARTS_WITH;
				case "Ends With" -> ENDS_WITH;
				case "Contains" -> CONTAINS;
				case "Contains (Case Insensitive)" -> CONTAINS_IGNORE_CASE;
				default -> null;
			};
		}
	}

	public static class Filter {
		private FilterOn filterOn;
		private FilterType filterType;
		private String filter;

		public Filter() { }

		public Filter(FilterOn filterOn, FilterType filterType, String filter) {
			this.filterOn = filterOn;
			this.filterType = filterType;
			this.filter = filter;
		}

		public FilterOn getFilterOn() { return filterOn; }
		public void setFilterOn(FilterOn filterOn) { this.filterOn = filterOn; }

		public FilterType getFilterType() { return filterType; }
		public void setFilterType(FilterType filterType) { this.filterType = filterType; }

		public String getFilter() { return filter; }
		public void setFilter(String filter) { this.filter = filter; }

		public static Filter nameStartsWith(String prefix) {
			return new Filter(FilterOn.NAME, FilterType.STARTS_WITH, prefix);
		}
	}
}
