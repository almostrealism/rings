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

import ai.djl.sentencepiece.SpTokenizer;
import ai.djl.sentencepiece.SpVocabulary;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.almostrealism.profile.OperationProfile;
import io.almostrealism.profile.OperationProfileNode;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.ml.OnnxFeatures;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.DoubleConsumer;

public class AudioGenerator implements AutoCloseable, OnnxFeatures {
	public static boolean enableOnnxDit = false;

	public static double MAX_DURATION = 11.0;

	private static final int SAMPLE_RATE = 44100;
	private static final int NUM_STEPS = 8;
	private static final float LOGSNR_MAX = -6.0f;
	private static final float SIGMA_MIN = 0.0f;
	private static final float SIGMA_MAX = 1.0f;

	// Model dimensions
	private static final long[] DIT_X_SHAPE = new long[] { 1, 64, 256 };
	private static final int DIT_X_SIZE = 64 * 256;
	private static final int T5_SEQ_LENGTH = 128;

	private final SpTokenizer tokenizer;
	private final SpVocabulary vocabulary;

	private final OrtEnvironment env;
	private final OrtSession conditionersSession;
	private final OrtSession autoencoderSession;
	private final DitModel ditModel;

	private double audioDurationSeconds;

	private DoubleConsumer progressMonitor;

	public AudioGenerator(String modelsPath) throws OrtException, IOException {
		this(new AssetGroup(new Asset(new File(modelsPath + "/conditioners.onnx")),
				new Asset(new File(modelsPath + "/autoencoder.onnx")),
				new Asset(new File(modelsPath + "/dit.onnx"))),
				new StateDictionary(modelsPath + "/weights"));
	}

	public AudioGenerator(AssetGroup onnxAssets, StateDictionary ditStates) throws OrtException, IOException {
		tokenizer = new SpTokenizer(AudioGenerator.class.getClassLoader().getResourceAsStream("spiece.model"));
		vocabulary = SpVocabulary.from(tokenizer);

		env = OrtEnvironment.getEnvironment();

		OrtSession.SessionOptions options = new OrtSession.SessionOptions();
		options.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());
		options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

		conditionersSession = env.createSession(onnxAssets.getAssetPath("conditioners.onnx"), options);
		autoencoderSession = env.createSession(onnxAssets.getAssetPath("autoencoder.onnx"), options);

		if (enableOnnxDit) {
			ditModel = new OnnxDitModel(env, options, onnxAssets.getAssetPath("dit.onnx"));
		} else {
			ditModel = new DiffusionTransformer(
					64,
					1024,
					16,
					8,
					1,
					768,
					768,
					"rf_denoiser",
					ditStates
			);
		}

		audioDurationSeconds = 10.0;
	}

	public DitModel getDitModel() { return ditModel; }

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

		log("Wrote " + outputPath);
	}

	public double[][] generateAudio(String prompt, long seed) throws OrtException {
		try {
			long[] tokenIds = tokenizer.tokenize(prompt).stream().mapToLong(vocabulary::getIndex).toArray();
			return generateAudio(tokenIds, seed);
		} finally {
			if (progressMonitor != null) {
				progressMonitor.accept(1.0);
			}
		}
	}

	public double[][] generateAudio(long[] tokenIds, long seed) throws OrtException {
		log("Generating audio with seed " + seed +
				" (duration = " + getAudioDuration() + ")");

		// 1. Process tokens through conditioners
		Map<String, OnnxTensor> conditionerOutputs = runConditioners(tokenIds);

		// 2. Run diffusion steps
		OnnxTensor finalLatent = runDiffusionSteps(
				conditionerOutputs.get("cross_attention_input"),
				conditionerOutputs.get("global_cond"),
				seed
		);

		// 3. Decode audio
		long start = System.currentTimeMillis();
		double[][] audio = decodeAudio(finalLatent);
		log((System.currentTimeMillis() - start) + "ms for autoencoder");

		// 4. Clean up
		for (OnnxTensor tensor : conditionerOutputs.values()) {
			tensor.close();
		}
		finalLatent.close();

		return audio;
	}

	private Map<String, OnnxTensor> runConditioners(long[] ids) throws OrtException {
		// Same as original implementation
		long[] paddedIds = new long[T5_SEQ_LENGTH];
		long[] attentionMask = new long[T5_SEQ_LENGTH];

		int tokenCount = Math.min(ids.length, T5_SEQ_LENGTH);
		System.arraycopy(ids, 0, paddedIds, 0, tokenCount);
		for (int i = 0; i < tokenCount; i++) {
			attentionMask[i] = 1;
		}

		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("input_ids", packOnnx(env, shape(1, T5_SEQ_LENGTH), paddedIds));
		inputs.put("attention_mask", packOnnx(env, shape(1, T5_SEQ_LENGTH), attentionMask));
		inputs.put("seconds_total", packOnnx(env, shape(1), (float) getAudioDuration()));

		try {
			OrtSession.Result result =
					conditionersSession.run(inputs);
			Map<String, OnnxTensor> outputs = new HashMap<>();
			outputs.put("cross_attention_input", (OnnxTensor) result.get(0));
			outputs.put("cross_attention_masks", (OnnxTensor) result.get(1));
			outputs.put("global_cond", (OnnxTensor) result.get(2));
			return outputs;
		} finally {
			inputs.forEach((key, tensor) -> tensor.close());
		}
	}

	private OnnxTensor runDiffusionSteps(OnnxTensor crossAttentionInput,
										 OnnxTensor globalCond, long seed)
									throws OrtException {
		// Initialize random noise
		Random random = new Random(seed);
		float[] x = new float[DIT_X_SIZE];
		for (int i = 0; i < DIT_X_SIZE; i++) {
			x[i] = (float) random.nextGaussian();
		}

		// Generate sigma values
		float[] sigmas = new float[NUM_STEPS + 1];
		fillSigmas(sigmas, LOGSNR_MAX, 2.0f);

		// Convert OnnxTensors to PackedCollections
		PackedCollection<?> xPC = new PackedCollection<>(shape(DIT_X_SHAPE));
		xPC.setMem(x);
		checkNan(xPC, "initial x");

		// Get cross attention and global condition data
		PackedCollection<?> crossAttnCondPC = pack(crossAttentionInput);
		PackedCollection<?> globalCondPC = pack(globalCond);

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
			PackedCollection<?> output = ditModel.forward(xPC, tPC, crossAttnCondPC, globalCondPC);

			checkNan(xPC, "input after model step " + step);
			checkNan(output, "output after model step " + step);

			modelTotal += System.currentTimeMillis() - start;
			start = System.currentTimeMillis();

			double[] xData = xPC.toArray();
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
			xPC.setMem(newX);
			checkNan(xPC, "new input after model step " + step);

			samplingTotal += System.currentTimeMillis() - start;
		}

		log("Diffusion completed - " + samplingTotal + "ms sampling, " + modelTotal + "ms model");

		double total = xPC.doubleStream().map(Math::abs).sum();

		if (HardwareFeatures.outputMonitoring)
			log("Average latent amplitude = " + (total / DIT_X_SIZE) + " (" + xPC.count(Double::isNaN) + " NaN values)");

		return toOnnx(env, xPC);
	}

	private double[][] decodeAudio(OnnxTensor latent) throws OrtException {
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("sampled", latent);

		OrtSession.Result result = autoencoderSession.run(inputs);
		OnnxTensor audioTensor = (OnnxTensor) result.get(0);

		try {
			FloatBuffer audioBuffer = audioTensor.getFloatBuffer();
			int totalSamples = audioBuffer.capacity();
			int channelSamples = totalSamples / 2; // Stereo audio, 2 channels
			int finalSamples = (int) (getAudioDuration() * SAMPLE_RATE);

			double total = 0;
			double[][] stereoAudio = new double[2][finalSamples];
			for (int i = 0; i < finalSamples; i++) {
				stereoAudio[0][i] = audioBuffer.get(i);
				stereoAudio[1][i] = audioBuffer.get(i + channelSamples);
				total += Math.abs(stereoAudio[0][i]) + Math.abs(stereoAudio[1][i]);
			}

			return stereoAudio;
		} finally {
			audioTensor.close();
		}
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

	@Override
	public void close() throws OrtException {
		if (conditionersSession != null) conditionersSession.close();
		if (autoencoderSession != null) autoencoderSession.close();
		if (ditModel != null) ditModel.destroy();
		if (env != null) env.close();
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java -jar audiogen.jar <models_path> <output_path> <prompt> <duration>");
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