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

import java.util.function.Consumer;

public class WaveDataPublisher {
	public static final int BATCH_SIZE = (int) Math.pow(2, 16);

	public WaveDataPublisher() {

	}

	public void publish(WaveData data, Consumer<Generation.AudioSegment> segment) {
		int index = 0;

		double samples[] = data.getCollection().toArray(0, data.getCollection().getMemLength());

		System.out.println("WaveDataPublisher: Publishing " + samples.length + " samples");

		while (index < samples.length) {
			Generation.AudioSegment.Builder builder = Generation.AudioSegment.newBuilder();
			builder.setIndex(index);
			builder.setSampleRate(data.getSampleRate());
			builder.setTotalSamples(samples.length);
			builder.setIsFinal(index + BATCH_SIZE >= samples.length);

			int length = Math.min(BATCH_SIZE, samples.length - index);

			for (int i = 0; i < length; i++) {
				builder.addData(samples[index + i]);
			}

			segment.accept(builder.build());
			index += BATCH_SIZE;
		}
	}
}
