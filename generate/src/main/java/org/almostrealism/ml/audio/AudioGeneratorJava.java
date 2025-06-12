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
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.OnnxFeatures;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AudioGeneratorJava implements AutoCloseable, OnnxFeatures {
	public static boolean enableOnnxDit = true;

	private static final float AUDIO_LEN_SEC = 10.0f;
	private static final int NUM_STEPS = 8;
	private static final float LOGSNR_MAX = -6.0f;
	private static final float SIGMA_MIN = 0.0f;
	private static final float SIGMA_MAX = 1.0f;

	// Model dimensions
	private static final long[] DIT_X_SHAPE = new long[] { 1, 64, 256 };
	private static final int DIT_X_SIZE = 64 * 256;
	private static final int T5_SEQ_LENGTH = 128;

	private final OrtEnvironment env;
	private final OrtSession conditionersSession;
	private final OrtSession autoencoderSession;
	private final DitModel ditModel;

	private final SpTokenizer tokenizer;
	private final SpVocabulary vocabulary;

	public AudioGeneratorJava(String modelsPath, String weightsDir) throws OrtException, IOException {
		tokenizer = new SpTokenizer(AudioGenerator.class.getClassLoader().getResourceAsStream("spiece.model"));
		vocabulary = SpVocabulary.from(tokenizer);

		env = OrtEnvironment.getEnvironment();

		OrtSession.SessionOptions options = new OrtSession.SessionOptions();
		options.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());
		options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

		conditionersSession = env.createSession(modelsPath + "/conditioners.onnx", options);
		autoencoderSession = env.createSession(modelsPath + "/autoencoder.onnx", options);

		if (enableOnnxDit) {
			ditModel = new OnnxDitModel(env, options, modelsPath);
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
					weightsDir
			);
		}
	}

	public void generateAudio(String prompt, long seed, String outputPath) throws OrtException, IOException {
		double[][] audio = generateAudio(prompt, seed);
		try (WavFile f = WavFile.newWavFile(new File(outputPath), 2, audio[0].length, 32, 44100)) {
			f.writeFrames(audio);
		}
	}

	public double[][] generateAudio(String prompt, long seed) throws OrtException {
		long[] tokenIds = tokenizer.tokenize(prompt).stream().mapToLong(vocabulary::getIndex).toArray();
		return generateAudio(tokenIds, seed);
	}

	public double[][] generateAudio(long[] tokenIds, long seed) throws OrtException {
		// 1. Process tokens through conditioners
		long start = System.currentTimeMillis();
		Map<String, OnnxTensor> conditionerOutputs = runConditioners(tokenIds);
		System.out.println("Conditioners: " + (System.currentTimeMillis() - start) + "ms");

		// 2. Run diffusion steps
		start = System.currentTimeMillis();
		OnnxTensor finalLatent = runDiffusionSteps(
				conditionerOutputs.get("cross_attention_input"),
				conditionerOutputs.get("global_cond"),
				seed
		);
		System.out.println("Diffusion: " + (System.currentTimeMillis() - start) + "ms");

		// 3. Decode audio
		start = System.currentTimeMillis();
		double[][] audio = decodeAudio(finalLatent);
		System.out.println("Autoencoder: " + (System.currentTimeMillis() - start) + "ms");

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

		OnnxTensor inputIds = OnnxTensor.createTensor(env, LongBuffer.wrap(paddedIds), new long[]{1, T5_SEQ_LENGTH});
		OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), new long[]{1, T5_SEQ_LENGTH});
		OnnxTensor secondsTotal = OnnxTensor.createTensor(env, FloatBuffer.wrap(new float[]{AUDIO_LEN_SEC}), new long[]{1});

		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("input_ids", inputIds);
		inputs.put("attention_mask", attentionMaskTensor);
		inputs.put("seconds_total", secondsTotal);

		OrtSession.Result result = conditionersSession.run(inputs);

		Map<String, OnnxTensor> outputs = new HashMap<>();
		outputs.put("cross_attention_input", (OnnxTensor) result.get(0));
		outputs.put("cross_attention_masks", (OnnxTensor) result.get(1));
		outputs.put("global_cond", (OnnxTensor) result.get(2));

		inputIds.close();
		attentionMaskTensor.close();
		secondsTotal.close();

		return outputs;
	}

	private OnnxTensor runDiffusionSteps(OnnxTensor crossAttentionInput, OnnxTensor globalCond, long seed)
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

		// Get cross attention and global condition data
		PackedCollection<?> crossAttnCondPC = pack(crossAttentionInput);
		PackedCollection<?> globalCondPC = pack(globalCond);

		// Run diffusion steps
		for (int step = 0; step < NUM_STEPS; step++) {
			float currT = sigmas[step];
			float nextT = sigmas[step + 1];

			// Create time tensor
			PackedCollection<?> tPC = new PackedCollection<>(1);
			tPC.setMem(0, currT);

			// Run DiffusionTransformer
			PackedCollection<?> outputPC = ditModel.forward(xPC, tPC, crossAttnCondPC, globalCondPC);

			// Get data back as array
			double[] xData = xPC.toArray();
			double[] outputData = outputPC.toArray();

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
		}

		// Convert final result back to OnnxTensor
		float[] finalX = xPC.toFloatArray();
		return OnnxTensor.createTensor(env, FloatBuffer.wrap(finalX), DIT_X_SHAPE);
	}

	private double[][] decodeAudio(OnnxTensor latent) throws OrtException {
		// Same as original implementation
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("sampled", latent);

		OrtSession.Result result = autoencoderSession.run(inputs);
		OnnxTensor audioTensor = (OnnxTensor) result.get(0);

		FloatBuffer audioBuffer = audioTensor.getFloatBuffer();
		int totalSamples = audioBuffer.capacity();
		int channelSamples = totalSamples / 2; // Stereo audio

		double[][] stereoAudio = new double[2][channelSamples];
		for (int i = 0; i < channelSamples; i++) {
			stereoAudio[0][i] = audioBuffer.get(i);                  // Left channel
			stereoAudio[1][i] = audioBuffer.get(i + channelSamples); // Right channel
		}

		audioTensor.close();
		return stereoAudio;
	}

	private void fillSigmas(float[] arr, float start, float end) {
		// Same as original implementation
		int size = arr.length;
		float step = (end - start) / (size - 1);

		arr[0] = start;
		arr[size - 1] = end;

		for (int i = 1; i < size - 1; i++) {
			arr[i] = arr[i - 1] + step;
		}

		for (int i = 0; i < size; i++) {
			arr[i] = 1.0f / (1.0f + (float) Math.exp(arr[i]));
		}

		arr[0] = SIGMA_MAX;
		arr[size - 1] = SIGMA_MIN;
	}

	@Override
	public void close() throws OrtException {
		if (conditionersSession != null) conditionersSession.close();
		if (autoencoderSession != null) autoencoderSession.close();
		if (env != null) env.close();
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.out.println("Usage: java -jar audiogen.jar <models_path> <weights_dir> <prompt> <output_path>");
			return;
		}

		String modelsPath = args[0];
		String weightsDir = args[1];
		String prompt = args[2];
		String outputPath = args[3];
		long seed = 99;

		try (AudioGeneratorJava generator = new AudioGeneratorJava(modelsPath, weightsDir)) {
			generator.generateAudio(prompt, seed, outputPath);
		}
	}
}