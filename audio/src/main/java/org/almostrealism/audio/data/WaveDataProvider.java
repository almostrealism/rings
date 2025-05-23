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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.almostrealism.relation.Countable;

import java.util.Optional;
import java.util.function.Supplier;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@type")
public interface WaveDataProvider extends AudioDataProvider, Supplier<WaveData>, Countable, Comparable<WaveDataProvider> {

	@JsonIgnore
	String getKey();

	double getDuration();

	double getDuration(double playbackRate);

	WaveData get(double playbackRate);

	@Override
	default int compareTo(WaveDataProvider o) {
		return Optional.ofNullable(getIdentifier()).orElse("").compareTo(
				Optional.ofNullable(o.getIdentifier()).orElse(""));
	}
}
