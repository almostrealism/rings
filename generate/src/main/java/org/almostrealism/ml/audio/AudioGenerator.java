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
import io.almostrealism.profile.OperationProfile;
import io.almostrealism.profile.OperationProfileNode;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleConsumer;

public class AudioGenerator extends ConditionalAudioSystem {

	private double audioDurationSeconds;

	private DoubleConsumer progressMonitor;

	public AudioGenerator(String modelsPath) throws OrtException, IOException {
		this(new AssetGroup(new Asset(new File(modelsPath + "/conditioners.onnx")),
				new Asset(new File(modelsPath + "/encoder.onnx")),
				new Asset(new File(modelsPath + "/decoder.onnx")),
				new Asset(new File(modelsPath + "/dit.onnx"))),
				new StateDictionary(modelsPath + "/weights"));
	}

	public AudioGenerator(AssetGroup onnxAssets, StateDictionary ditStates) throws OrtException, IOException {
		super(onnxAssets, ditStates);
		audioDurationSeconds = 10.0;
	}

	public double getAudioDuration() { return audioDurationSeconds; }
	public void setAudioDurationSeconds(double seconds) {
		this.audioDurationSeconds = seconds;
	}

	public DoubleConsumer getProgressMonitor() { return progressMonitor; }
	public void setProgressMonitor(DoubleConsumer monitor) {
		this.progressMonitor = monitor;
	}

	public void generateAudio(String prompt, long seed, String outputPath) throws OrtException, IOException {
		double[][] audio = generateAudio(prompt, seed);
		try (WavFile f = WavFile.newWavFile(new File(outputPath), 2, audio[0].length, 32, SAMPLE_RATE)) {
			f.writeFrames(audio);
		}
	}

	public double[][] generateAudio(String prompt, long seed) throws OrtException {
		try {
			return generateAudio(tokenize(prompt), seed);
		} finally {
			if (progressMonitor != null) {
				progressMonitor.accept(1.0);
			}
		}
	}

	public double[][] generateAudio(long[] tokenIds, long seed) {
		log("Generating audio with seed " + seed +
				" (duration = " + getAudioDuration() + ")");

		// 1. Process tokens through conditioners
		Map<String, PackedCollection<?>> conditionerOutputs = runConditioners(tokenIds);

		// 2. Run diffusion steps
		PackedCollection<?> finalLatent = runDiffusionSteps(
				conditionerOutputs.get("cross_attention_input"),
				conditionerOutputs.get("global_cond"),
				seed
		);

		// 3. Decode audio
		long start = System.currentTimeMillis();
		double[][] audio = decodeAudio(finalLatent);
		log((System.currentTimeMillis() - start) + "ms for autoencoder");
		return audio;
	}

	protected Map<String, PackedCollection<?>> runConditioners(long[] ids) {
		return runConditioners(ids, getAudioDuration());
	}

	private PackedCollection<?> runDiffusionSteps(PackedCollection<?> crossAttentionInput,
												  PackedCollection<?> globalCond, long seed) {
		// Generate sigma values
		float[] sigmas = new float[NUM_STEPS + 1];
		fillSigmas(sigmas, LOGSNR_MAX, 2.0f);

		Random random = new Random(seed);
		PackedCollection<?> x = initialX(random);

		long samplingTotal = 0;
		long modelTotal = 0;

		PackedCollection<?> tPC = new PackedCollection<>(1);

		// Run diffusion steps
		for (int step = 0; step < NUM_STEPS; step++) {
			float currT = sigmas[step];
			float nextT = sigmas[step + 1];
			tPC.setMem(0, currT);

			if (progressMonitor != null) {
				progressMonitor.accept((double) step / NUM_STEPS);
			}

			// Run DiffusionTransformer
			long start = System.currentTimeMillis();
			PackedCollection<?> output = getDitModel().forward(x, tPC, crossAttentionInput, globalCond);

			checkNan(x, "input after model step " + step);
			checkNan(output, "output after model step " + step);

			modelTotal += System.currentTimeMillis() - start;
			start = System.currentTimeMillis();

			double[] xData = x.toArray();
			double[] outputData = output.toArray();

			// Apply ping-pong sampling
			for (int i = 0; i < DIT_X_SIZE; i++) {
				outputData[i] = xData[i] - (currT * outputData[i]);
			}

			// Generate new noise
			float[] newNoise = new float[DIT_X_SIZE];
			for (int i = 0; i < DIT_X_SIZE; i++) {
				newNoise[i] = (float) random.nextGaussian();
			}

			// Update x for next step
			float[] newX = new float[DIT_X_SIZE];
			for (int i = 0; i < DIT_X_SIZE; i++) {
				newX[i] = (float) ((1.0f - nextT) * outputData[i]) + (nextT * newNoise[i]);
			}

			// Update x for next iteration
			x.setMem(newX);
			checkNan(x, "new input after model step " + step);

			samplingTotal += System.currentTimeMillis() - start;
		}

		log("Diffusion completed - " + samplingTotal + "ms sampling, " + modelTotal + "ms model");

		double total = x.doubleStream().map(Math::abs).sum();

		if (HardwareFeatures.outputMonitoring)
			log("Average latent amplitude = " + (total / DIT_X_SIZE) + " (" + x.count(Double::isNaN) + " NaN values)");

		return x;
	}

	/**
	 * Initialize random noise.
	 */
	private PackedCollection<?> initialX(Random random) {
		float[] initialX = new float[DIT_X_SIZE];
		for (int i = 0; i < DIT_X_SIZE; i++) {
			initialX[i] = (float) random.nextGaussian();
		}

		PackedCollection<?> x = new PackedCollection<>(shape(DIT_X_SHAPE));
		x.setMem(initialX);
		return x;
	}

	private double[][] decodeAudio(PackedCollection<?> latent) {
		PackedCollection<?> result = getAutoencoder().decode(cp(latent)).evaluate();

		double data[] = result.toArray();
		int totalSamples = data.length;
		int channelSamples = totalSamples / 2; // Stereo audio, 2 channels
		int finalSamples = (int) (getAudioDuration() * SAMPLE_RATE);

		double[][] stereoAudio = new double[2][finalSamples];
		for (int i = 0; i < finalSamples; i++) {
			stereoAudio[0][i] = data[i];
			stereoAudio[1][i] = data[i + channelSamples];
		}

		return stereoAudio;
	}

	private void fillSigmas(float[] arr, float start, float end) {
		int size = arr.length;
		float step = (end - start) / (size - 1);

		// Linspace
		arr[0] = start;
		arr[size - 1] = end;

		for (int i = 1; i < size - 1; i++) {
			arr[i] = arr[i - 1] + step;
		}

		// Apply sigmoid transformation
		for (int i = 0; i < size; i++) {
			arr[i] = 1.0f / (1.0f + (float) Math.exp(arr[i]));
		}

		// Set boundaries
		arr[0] = SIGMA_MAX;
		arr[size - 1] = SIGMA_MIN;
	}

	private void checkNan(PackedCollection<?> x, String context) {
		if (HardwareFeatures.outputMonitoring) {
			long nanCount = x.count(Double::isNaN);

			if (nanCount > 0) {
				warn(nanCount + " NaN values detected at " + context);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java AudioGenerator <models_path> <output_path> <prompt> <duration>");
			return;
		}

		String modelsPath = args[0];
		String outputPath = args[1];
		String prompt = args[2];
		double duration = Double.parseDouble(args[3]);

		Random rand = new Random();

		try (AudioGenerator generator = new AudioGenerator(modelsPath)) {
			generator.setAudioDurationSeconds(duration);

			for (int i = 0; i < 10; i++) {
				long seed = rand.nextLong();
				System.out.println("AudioGenerator: Generating audio with seed " + seed);
				generator.generateAudio(prompt, seed, outputPath + "/output_" + seed + ".wav");
			}

			OperationProfile profile = generator.getDitModel().getProfile();

			if (profile instanceof OperationProfileNode) {
				((OperationProfileNode) profile).save("results/dit.xml");
			}
		}
	}
}