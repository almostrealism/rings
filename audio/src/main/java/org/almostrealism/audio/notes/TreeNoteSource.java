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
import io.almostrealism.code.Tree;
import io.almostrealism.relation.Named;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.FileWaveDataProviderNode;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.audio.tone.WesternChromatic;
import org.glassfish.grizzly.streams.Output;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TreeNoteSource implements PatternNoteSource, Named {
	public static boolean alwaysComputeNotes = false;

	private Tree<? extends Supplier<FileWaveDataProvider>> tree;
	private List<PatternNote> notes;

	private KeyboardTuning tuning;
	private KeyPosition<?> root;

	private List<Filter> filters;

	public TreeNoteSource() { this(null); }

	public TreeNoteSource(Tree<? extends Supplier<FileWaveDataProvider>> tree) {
		this(tree, WesternChromatic.C1);
	}

	public TreeNoteSource(Tree<? extends Supplier<FileWaveDataProvider>> tree, KeyPosition<?> root) {
		setTree(tree);
		setRoot(root);
		this.filters = new ArrayList<>();
	}

	public KeyPosition<?> getRoot() { return root; }
	public void setRoot(KeyPosition<?> root) { this.root = root; }

	@JsonIgnore
	public Tree<? extends Supplier<FileWaveDataProvider>> getTree() { return tree; }

	@JsonIgnore
	public void setTree(Tree<? extends Supplier<FileWaveDataProvider>> tree) {
		this.tree = tree;
		if (!alwaysComputeNotes) computeNotes();
	}

	public List<Filter> getFilters() { return filters; }

	@Override
	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;

		if (notes != null) {
			for (PatternNote note : notes) {
				note.setTuning(tuning);
			}
		}
	}

	@JsonIgnore
	@Override
	public String getName() {
		if (filters.isEmpty()) return getOrigin();
		if (filters.size() > 1) return getOrigin() + " (" + filters.size() + " filters)";
		return filters.get(0).filterOn.name() + " " + filters.get(0).filterType.name() + " \"" + filters.get(0).filter + "\"";
	}

	public String getOrigin() { return tree instanceof Named ? ((Named) tree).getName() : ""; }

	@JsonIgnore
	public List<PatternNote> getNotes() {
		if (alwaysComputeNotes) computeNotes();
		return notes == null ? new ArrayList<>() : notes;
	}

	public void refresh() {
		if (alwaysComputeNotes) {
			notes = null;
		} else {
			computeNotes();
		}
	}

	private void computeNotes() {
		notes = new ArrayList<>();
		if (tree == null) return;
		
		tree.forEach(f -> {
			FileWaveDataProvider p = f.get();

			try {
				if (p == null) {
					// System.out.println("WARN: FileWaveDataProvider produced null");
					return;
				}

				if (p.getSampleRate() != OutputLine.sampleRate) {
					return;
				}

				boolean match = filters.stream()
						.map(filter -> filter.filterType.matches(filter.filterOn.select(p), filter.filter))
						.reduce((a, b) -> a & b)
						.orElse(true);
				if (match) notes.add(new PatternNote(p, getRoot()));
			} catch (Exception e) {
				System.out.println("WARN: " + e.getMessage() + "(" + p.getResourcePath() + ")");
			}
		});
		notes.forEach(n -> n.setTuning(tuning));
	}

	public boolean checkResourceUsed(String canonicalPath) {
		if (notes == null) computeNotes();

		boolean match = notes.stream().anyMatch(note -> {
			if (note.getProvider() instanceof FileWaveDataProvider) {
				return ((FileWaveDataProvider) note.getProvider()).getResourcePath().equals(canonicalPath);
			} else {
				return false;
			}
		});

		return match;
	}

	public static TreeNoteSource fromFile(File root, Filter filter) {
		TreeNoteSource t = new TreeNoteSource(new FileWaveDataProviderNode(root));
		t.getFilters().add(filter);
		return t;
	}

	public enum FilterOn {
		PATH, NAME;

		String select(FileWaveDataProvider p) {
			return switch (this) {
				case NAME -> new File(p.getResourcePath()).getName();
				case PATH -> p.getResourcePath();
			};
		}
	}

	public enum FilterType {
		EQUALS, EQUALS_IGNORE_CASE, STARTS_WITH, ENDS_WITH, CONTAINS;

		boolean matches(String value, String filter) {
			return switch (this) {
				case EQUALS -> value.equals(filter);
				case EQUALS_IGNORE_CASE -> value.equalsIgnoreCase(filter);
				case STARTS_WITH -> value.startsWith(filter);
				case ENDS_WITH -> value.endsWith(filter);
				case CONTAINS -> value.contains(filter);
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
