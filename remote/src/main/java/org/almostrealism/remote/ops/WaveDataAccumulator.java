/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.remote.ops;

import org.almostrealism.remote.api.Generation;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class WaveDataAccumulator {
	private Map<String, PackedCollection> data;
	private BiConsumer<String, WaveData> output;

	public WaveDataAccumulator(BiConsumer<String, WaveData> output) {
		this.data = new HashMap<>();
		this.output = output;
	}

	public void process(String id, Generation.AudioSegment segment) {
		PackedCollection collection = data.get(id);
		if (collection == null) {
			collection = new PackedCollection(segment.getTotalSamples());
			data.put(id, collection);
		}

		collection.setMem(segment.getIndex(), segment.getDataList().stream().mapToDouble(Double::doubleValue).toArray());
		if (segment.getIsFinal()) {
			System.out.println("WaveDataAccumulator: Final segment received for " + id);
			output.accept(id, new WaveData(data.remove(id), segment.getSampleRate()));
		}
	}
}
