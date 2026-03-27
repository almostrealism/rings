/*
 * Copyright 2020 Michael Murray
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

package org.almostrealism.swing;

import io.almostrealism.relation.Producer;

/**
 * An interface for objects with runtime-modifiable properties.
 *
 * <p>Previously located in {@code io.almostrealism.relation}, this interface
 * was relocated here as it is only used by the Swing UI layer.</p>
 */
public interface Editable {
	/**
	 * Stores a set of options and a selection index.
	 */
	class Selection {
		private String options[];
		private int selected;

		public Selection(String options[]) {
			this.options = options;
			this.selected = 0;
		}

		public String[] getOptions() { return this.options; }
		public void setSelected(int index) { this.selected = index; }
		public int getSelected() { return this.selected; }

		public String toString() { return this.options[this.selected]; }
	}

	/** Returns names for each editable property. */
	String[] getPropertyNames();

	/** Returns descriptions for each editable property. */
	String[] getPropertyDescriptions();

	/** Returns the class types of each editable property. */
	Class[] getPropertyTypes();

	/** Returns the current values of the properties. */
	Object[] getPropertyValues();

	/** Sets the value of the property at the specified index. */
	void setPropertyValue(Object value, int index);

	/** Sets the values of all properties. */
	void setPropertyValues(Object values[]);

	/** Returns Producer objects for input properties. */
	Producer[] getInputPropertyValues();

	/** Sets a producer input for the specified input property index. */
	void setInputPropertyValue(int index, Producer p);
}
