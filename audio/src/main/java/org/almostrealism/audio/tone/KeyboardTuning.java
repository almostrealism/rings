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
import org.almostrealism.time.Frequency;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
public interface KeyboardTuning {
	default Frequency getTone(KeyPosition pos) {
		return getTone(pos.position(), KeyNumbering.STANDARD);
	}

	default <T extends KeyPosition> List<Frequency> getTones(Scale<T> scale) {
		return IntStream.range(0, scale.length())
				.mapToObj(scale::valueAt)
				.map(this::getTone)
				.collect(Collectors.toList());
	}

	Frequency getTone(int key, KeyNumbering numbering);
}
