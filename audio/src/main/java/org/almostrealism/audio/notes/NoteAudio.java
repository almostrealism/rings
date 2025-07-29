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

import io.almostrealism.relation.Producer;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuned;
import org.almostrealism.collect.CollectionFeatures;
import org.almostrealism.collect.PackedCollection;

public interface NoteAudio extends KeyboardTuned {
	Producer<PackedCollection<?>> getAudio(KeyPosition<?> target, int channel);

	WaveData getWaveData();

	default double getDuration(KeyPosition<?> target) {
		return CollectionFeatures.getInstance().shape(getAudio(target, -1)).getTotalSizeLong() / (double) getSampleRate();
	}

	int getSampleRate();
}
