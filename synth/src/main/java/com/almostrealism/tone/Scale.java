/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.tone;

import io.almostrealism.uml.Plural;

public interface Scale<T extends KeyPosition> extends Plural<T> {
	int length();

	static <T extends KeyPosition> Scale<T> of(T... notes) {
		return new Scale<T>() {
			@Override
			public int length() {
				return notes.length;
			}

			@Override
			public T valueAt(int pos) {
				return notes[pos];
			}
		};
	}
}
