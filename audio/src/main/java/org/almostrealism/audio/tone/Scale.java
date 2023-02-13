/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.tone;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.almostrealism.uml.Plural;

import java.util.function.Consumer;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
public interface Scale<T extends KeyPosition> extends Plural<T> {
	int length();

	static <T extends KeyPosition> Scale<T> of(T... notes) {
		return new StaticScale<>(notes);
	}

	default void forEach(Consumer<T> consumer) {
		for (int i = 0; i < length(); i++) {
			consumer.accept(valueAt(i));
		}
	}
}
