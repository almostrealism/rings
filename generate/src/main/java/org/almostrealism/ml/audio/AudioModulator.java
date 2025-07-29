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
		this(onnxAssets, System.currentTimeMillis());
	}

	public AudioModulator(AssetGroup onnxAssets, long seed) throws OrtException {
		this(onnxAssets, DIM, seed);
	}

	public AudioModulator(AssetGroup onnxAssets, int dim, long seed) throws OrtException {
		composer = new AudioComposer(new OnnxAutoEncoder(
				onnxAssets.getAssetPath("encoder.onnx"),
				onnxAssets.getAssetPath("decoder.onnx")), dim, seed);
	}

	public void setAudioDuration(double seconds) {
		composer.setAudioDuration(Math.min(composer.getMaximumAudioDuration(), seconds));
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

	public void generateAudio(PackedCollection<?> position, String destination) {
		generateAudio(position, new File(destination));
	}

	public void generateAudio(PackedCollection<?> position, File destination) {
		PackedCollection<?> result = project(position);
		WaveData out = new WaveData(result, (int) composer.getSampleRate());
		out.save(destination);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java AudioModulator <models_path> <output_path> <input1> [additional_inputs...]");
			return;
		}

		int sampleRate = 44100;
		boolean noise = false;
		boolean empty = false;

		String modelsPath = args[0];
		String outputPath = args[1];

		List<String> inputs = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			inputs.add(args[i]);
		}

		try (AudioModulator modulator = new AudioModulator(modelsPath)) {
			for (String in : inputs) {
				WaveData wave = WaveData.load(new File(in));
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

			if (empty) {
				modulator.addFeatures(new PackedCollection<>(new TraversalPolicy(64, 256)));
			}

			int count = 8;

			for (int i = 0; i < count; i++) {
				PackedCollection<?> position =
						new PackedCollection<>(new TraversalPolicy(DIM))
								.randnFill();

				Path op = Path.of(outputPath).resolve("modulated_" + i + ".wav");
				modulator.generateAudio(position, op.toFile());
				Console.root().features(AudioModulator.class)
						.log("Saved modulated audio to " + op);
			}
		}
	}

	@Override
	public void close() {
		composer.destroy();
	}
}
