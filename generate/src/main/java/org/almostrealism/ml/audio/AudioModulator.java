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
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AudioModulator implements AutoCloseable, CodeFeatures {
	public static final int DIM = 8;

	private final AudioComposer composer;
	private double audioDuration;

	public AudioModulator(String modelsPath) throws OrtException {
		this(modelsPath, System.currentTimeMillis());
	}

	public AudioModulator(String modelsPath, long seed) throws OrtException {
		this(new AssetGroup(
				new Asset(new File(modelsPath + "/encoder.onnx")),
				new Asset(new File(modelsPath + "/decoder.onnx"))), seed);
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
		audioDuration = composer.getMaximumAudioDuration();
	}

	public double getAudioDuration() { return audioDuration; }
	public void setAudioDuration(double seconds) {
		this.audioDuration = Math.min(composer.getMaximumAudioDuration(), seconds);
	}

	public void addAudio(PackedCollection audio) {
		composer.addAudio(cp(audio));
	}

	public void addFeatures(PackedCollection features) {
		composer.addSource(cp(features));
	}

	public PackedCollection project(PackedCollection position) {
		try (PackedCollection result = composer.getResultant(cp(position)).evaluate()) {
			double[] data = result.toArray();
			int totalSamples = data.length;
			int channelSamples = totalSamples / 2; // Stereo audio, 2 channels
			int finalSamples = (int) (getAudioDuration() * composer.getSampleRate());

			double[] stereoAudio = new double[2 * finalSamples];
			for (int i = 0; i < finalSamples; i++) {
				stereoAudio[i] = data[i];
				stereoAudio[finalSamples + i] = data[channelSamples + i];
			}

			return pack(stereoAudio).reshape(2, finalSamples);
		}
	}

	public void generateAudio(PackedCollection position, String destination) {
		generateAudio(position, new File(destination));
	}

	public void generateAudio(PackedCollection position, File destination) {
		PackedCollection result = project(position);
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
		inputs.addAll(Arrays.asList(args).subList(2, args.length));

		long seed = 79;
		Random rand = new Random(seed + 1000);

		try (AudioModulator modulator = new AudioModulator(modelsPath, seed)) {
			for (String in : inputs) {
				WaveData wave = WaveData.load(new File(in));
				if (wave.getSampleRate() != sampleRate) {
					throw new IllegalArgumentException();
				}

				modulator.addAudio(wave.getData());
			}

			if (noise) {
				modulator.addFeatures(
						new PackedCollection(new TraversalPolicy(64, 256))
								.randnFill());
			}

			if (empty) {
				modulator.addFeatures(new PackedCollection(new TraversalPolicy(64, 256)));
			}

			int count = 8;

			for (int i = 0; i < count; i++) {
				PackedCollection position =
						new PackedCollection(new TraversalPolicy(DIM))
								.fill(rand::nextGaussian);

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
