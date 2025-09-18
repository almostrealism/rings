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

package org.almostrealism.ml.audio.test;

import ai.onnxruntime.OrtException;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.audio.AutoEncoder;
import org.almostrealism.ml.audio.AutoEncoderFeatureProvider;
import org.almostrealism.ml.audio.OnnxAutoEncoder;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class AutoEncoderFeatureProviderTests implements TestFeatures {
	String modelsDirectory = "/Users/michael/Documents/AlmostRealism/models/";

	protected AutoEncoderFeatureProvider provider() {
		try {
			AutoEncoder encoder = new OnnxAutoEncoder(
					modelsDirectory + "encoder.onnx",
					modelsDirectory + "decoder.onnx");
			return new AutoEncoderFeatureProvider(encoder);
		} catch (OrtException e) {
			throw new RuntimeException(e);
		}
	}

	protected double similarity(Producer<PackedCollection<?>> a, Producer<PackedCollection<?>> b) {
		return similarity(a, b, 256);
	}

	protected double similarity(Producer<PackedCollection<?>> a, Producer<PackedCollection<?>> b, int limit) {
		return 1.0 - multiply(a, b).sum(1)
				.divide(multiply(length(1, a), length(1, b)))
				.evaluate().doubleStream().limit(limit).average().orElse(-1.0);
	}

	@Test
	public void similarity() throws IOException {
		AutoEncoderFeatureProvider provider = provider();

		PackedCollection<?> snare1 =
				provider.computeFeatures(WaveData.load(new File("Library/Snare Perc DD.wav")));
		PackedCollection<?> snare2 =
				provider.computeFeatures(WaveData.load(new File("Library/Snare Gold 1.wav")));
		PackedCollection<?> dip =
				provider.computeFeatures(WaveData.load(new File("Library/Dip Flop DD 159.wav")));

		System.out.println("Comparing snares: " + similarity(cp(snare1), cp(snare2)));
		System.out.println("Comparing snare 1 and dip: " + similarity(cp(snare1), cp(dip)));
		System.out.println("Comparing snare 2 and dip: " + similarity(cp(snare2), cp(dip)));
	}
}
