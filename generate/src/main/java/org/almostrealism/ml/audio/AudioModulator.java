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

package org.almostrealism.ml.audio;

import ai.onnxruntime.OrtException;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.Console;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AudioModulator implements AutoCloseable, CodeFeatures {
	public static final int DIM = 8;

	private final AudioComposer composer;

	public AudioModulator(String modelsPath) throws OrtException {
		this(new AssetGroup(
						new Asset(new File(modelsPath + "/encoder.onnx")),
						new Asset(new File(modelsPath + "/decoder.onnx"))));
	}

	public AudioModulator(AssetGroup onnxAssets) throws OrtException {
		composer = new AudioComposer(new OnnxAutoEncoder(
				onnxAssets.getAssetPath("encoder.onnx"),
				onnxAssets.getAssetPath("decoder.onnx")), DIM);
	}

	public void addAudio(PackedCollection<?> audio) {
		composer.addAudio(cp(audio));
	}

	public void addFeatures(PackedCollection<?> features) {
		composer.addSource(cp(features));
	}

	public PackedCollection<?> project(PackedCollection<?> position) {
		return composer.getResultant(cp(position)).evaluate();
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java AudioModulator <models_path> <output_path> <input1> [additional_inputs...]");
			return;
		}

		int sampleRate = 44100;
		boolean noise = false;

		String modelsPath = args[0];
		String outputPath = args[1];

		List<String> inputs = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			inputs.add(args[i]);
		}

		try (AudioModulator modulator = new AudioModulator(modelsPath)) {
			for (String in : inputs) {
				WaveData wave = WaveData.loadMultiChannel(new File(in));
				if (wave.getSampleRate() != sampleRate) {
					throw new IllegalArgumentException();
				}

				modulator.addAudio(wave.getData());
			}

			if (noise) {
				modulator.addFeatures(
						new PackedCollection<>(new TraversalPolicy(64, 256))
								.randnFill());
			}

			int count = 8;

			for (int i = 0; i < count; i++) {
				PackedCollection<?> position =
						new PackedCollection<>(new TraversalPolicy(DIM))
								.randnFill();

//				double p = (double) i / (count - 1);
//				double l = 2 * p - 1;
//				double r = 1 - 2 * p;
//				position.fill(l, r);
//				position.print();

				PackedCollection<?> result = modulator.project(position);
				WaveData out = new WaveData(result, sampleRate);

				Path op = Path.of(outputPath).resolve("modulated_" + i + ".wav");
				out.saveMultiChannel(op.toFile());
				Console.root().features(AudioModulator.class)
						.log("Saved modulated audio to " + op);
			}
		}
	}

	@Override
	public void close() throws Exception {
		composer.destroy();
	}
}
