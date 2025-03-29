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

package org.almostrealism.audio.data;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public class FileWaveDataProviderFilter {
	public static final String[] SEPARATORS = {" ", "-", "_", "."};

	private FilterOn filterOn;
	private FilterType filterType;
	private String filter;

	public FileWaveDataProviderFilter() { }

	public FileWaveDataProviderFilter(FilterOn filterOn, FilterType filterType, String filter) {
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

	public boolean matches(FileWaveDataProviderTree tree, FileWaveDataProvider p) {
		return getFilterType().matches(getFilterOn().select(tree, p), getFilter());
	}

	public String coerceToMatch(String value, boolean allowEqual) {
		return getFilterType().coerceToMatch(value, getFilter(), allowEqual);
	}

	public Path coerceToMatch(Path relativePath) {
		String selected = getFilterOn().select(relativePath);
		String result = coerceToMatch(selected, getFilterOn() == FilterOn.PATH);
		System.out.println("FileWaveDataProviderFilter[" + relativePath +
				"]: Coerced " + selected + " to " + result);

		if (getFilterOn() == FilterOn.PATH) {
			return new File(result).toPath()
					.resolve(relativePath.toFile().getName());
		} else if (getFilterOn() == FilterOn.NAME) {
			Path parent = relativePath.getParent();
			return parent == null ? Path.of(result) : parent.resolve(result);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public static FileWaveDataProviderFilter nameStartsWith(String prefix) {
		return new FileWaveDataProviderFilter(FilterOn.NAME, FilterType.STARTS_WITH, prefix);
	}

	public enum FilterOn {
		PATH, NAME;

		String select(FileWaveDataProviderTree tree, FileWaveDataProvider p) {
			return switch (this) {
				case NAME -> new File(p.getResourcePath()).getName();
				case PATH -> stripSlash(new File(tree.getRelativePath(p.getResourcePath())).getParentFile().getPath());
			};
		}

		String select(Path relativePath) {
			return select(relativePath.toFile());
		}

		String select(File relativeFile) {
			return switch (this) {
				case NAME -> relativeFile.getName();
				case PATH -> Optional.ofNullable(relativeFile.getParentFile())
						.map(File::getPath).orElse(null);
			};
		}

		public String readableName() {
			return switch (this) {
				case NAME -> "File Name";
				case PATH -> "File Path";
			};
		}

		public static String stripSlash(String value) {
			if (value != null && value.startsWith("/")) {
				return value.substring(1);
			} else {
				return value;
			}
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
			if (value == null || filter == null || filter.isEmpty()) return false;

			return switch (this) {
				case EQUALS -> value.equals(filter);
				case EQUALS_IGNORE_CASE -> value.equalsIgnoreCase(filter);
				case STARTS_WITH -> value.startsWith(filter);
				case ENDS_WITH -> value.endsWith(filter);
				case CONTAINS -> value.contains(filter);
				case CONTAINS_IGNORE_CASE -> value.toLowerCase().contains(filter.toLowerCase());
			};
		}

		String coerceToMatch(String value, String filter, boolean allowEqual) {
			if (filter == null || filter.isEmpty() || matches(value, filter)) return value;


			FilterType type = this;

			if (value == null) {
				// If there is no value, the only coercion
				// that is possible is direct assignment
				type = FilterType.EQUALS;
			}

			return switch (type) {
				case STARTS_WITH, CONTAINS, CONTAINS_IGNORE_CASE -> paddedJoin(filter, value);
				case ENDS_WITH -> paddedJoin(value, filter);
				case EQUALS, EQUALS_IGNORE_CASE -> allowEqual ? filter : value;
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

	public static String paddedJoin(String a, String b) {
		if (endsWithSeparator(a) || startsWithSeparator(b)) {
			return a + b;
		} else {
			return a + " " + b;
		}
	}

	public static boolean startsWithSeparator(String text) {
		for (String separator : SEPARATORS) {
			if (text.startsWith(separator)) return true;
		}

		return false;
	}

	public static boolean endsWithSeparator(String text) {
		for (String separator : SEPARATORS) {
			if (text.endsWith(separator)) return true;
		}

		return false;
	}
}
