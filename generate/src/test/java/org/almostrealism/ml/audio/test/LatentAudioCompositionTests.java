/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.ml.audio.test;

import ai.onnxruntime.OrtException;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.audio.AutoEncoder;
import org.almostrealism.ml.audio.OnnxAutoEncoder;
import org.almostrealism.persistence.AssetGroup;
import org.almostrealism.persistence.AssetGroupInfo;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LatentAudioCompositionTests implements TestFeatures {
	@Test
	public void attract() throws IOException, OrtException {
		int sampleRate = 44100;
		double duration = 11;

		int bins = 64;
		int frames = 256;

		AssetGroup assets = new AssetGroup(AssetGroupInfo
				.forDirectory(new File("assets/stable-audio")));
		AutoEncoder encoder = new OnnxAutoEncoder(assets);

		WaveData a = WaveData.load(new File("Library/Long Omni C1.wav"));
		WaveData b = WaveData.load(new File("Library/Dip Riser DD 168.wav"));

		PackedCollection<?> featA = encoder.encode(cp(a.getData())).evaluate();
		PackedCollection<?> featB = encoder.encode(cp(b.getData())).evaluate();

		featA = featA.reshape(bins, frames).transpose();
		featB = featB.reshape(bins, frames).transpose();

		CollectionProducer<PackedCollection<?>> scale = integers(0, frames).divide(frames * 0.6)
														.traverse(1).repeat(bins);
		CollectionProducer<PackedCollection<?>> blend =
				cp(featA).multiply(scale).add(
						cp(featB).multiply(c(1.0).subtract(scale)));
		PackedCollection<?> result = blend.evaluate();
		log(result.getShape().toStringDetail());

		PackedCollection<?> audio = encoder
				.decode(cp(result.traverse(0)).transpose()).get().evaluate();
		new WaveData(audio, sampleRate)
				.save(new File("results/latent-composition.wav"));
	}
}
