package org.almostrealism.ml.audio;

import ai.djl.sentencepiece.SpTokenizer;
import ai.djl.sentencepiece.SpVocabulary;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.almostrealism.audio.WavFile;

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AudioGenerator implements AutoCloseable {
	// ONNX Runtime sessions
	private final OrtEnvironment env;
	private final OrtSession conditionersSession;
	private final OrtSession ditSession;
	private final OrtSession autoencoderSession;

	// Constants (matching C++ implementation)
	private static final float AUDIO_LEN_SEC = 10.0f;
	private static final int NUM_STEPS = 8;
	private static final float LOGSNR_MAX = -6.0f;
	private static final float SIGMA_MIN = 0.0f;
	private static final float SIGMA_MAX = 1.0f;

	// Model dimensions
	private static final long[] DIT_X_SHAPE = new long[]{1, 64, 256};
	private static final int DIT_X_SIZE = 64 * 256;
	private static final int T5_SEQ_LENGTH = 128;

	public AudioGenerator(String modelsPath) throws OrtException {
		// Initialize ONNX Runtime
		env = OrtEnvironment.getEnvironment();
		OrtSession.SessionOptions options = new OrtSession.SessionOptions();

		// Set number of threads
		options.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());
		options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

		// Create sessions for each model
		conditionersSession = env.createSession(modelsPath + "/conditioners.onnx", options);
		ditSession = env.createSession(modelsPath + "/dit.onnx", options);
		autoencoderSession = env.createSession(modelsPath + "/autoencoder.onnx", options);
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
		// Prepare tokenized input
		long[] paddedIds = new long[T5_SEQ_LENGTH];
		long[] attentionMask = new long[T5_SEQ_LENGTH];

		// Fill ids and attention mask
		int tokenCount = Math.min(ids.length, T5_SEQ_LENGTH);
		System.arraycopy(ids, 0, paddedIds, 0, tokenCount);
		for (int i = 0; i < tokenCount; i++) {
			attentionMask[i] = 1;
		}

		// Create input tensors
		OnnxTensor inputIds = OnnxTensor.createTensor(env, LongBuffer.wrap(paddedIds), new long[]{1, T5_SEQ_LENGTH});
		OnnxTensor attentionMaskTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(attentionMask), new long[]{1, T5_SEQ_LENGTH});
		OnnxTensor secondsTotal = OnnxTensor.createTensor(env, FloatBuffer.wrap(new float[]{AUDIO_LEN_SEC}), new long[]{1});

		// Create inputs map
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("input_ids", inputIds);
		inputs.put("attention_mask", attentionMaskTensor);
		inputs.put("seconds_total", secondsTotal);

		// Run inference
		OrtSession.Result result = conditionersSession.run(inputs);

		// Get outputs as OnnxTensor objects
		Map<String, OnnxTensor> outputs = new HashMap<>();
		outputs.put("cross_attention_input", (OnnxTensor) result.get(0));
		outputs.put("cross_attention_masks", (OnnxTensor) result.get(1));
		outputs.put("global_cond", (OnnxTensor) result.get(2));

		// Close input tensors
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

		OnnxTensor xTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(x), DIT_X_SHAPE);

		// Run diffusion steps
		for (int step = 0; step < NUM_STEPS; step++) {
			float currT = sigmas[step];
			float nextT = sigmas[step + 1];

			// Create time tensor
			OnnxTensor tTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(new float[]{currT}), new long[]{1});

			// Prepare DiT inputs
			Map<String, OnnxTensor> ditInputs = new HashMap<>();
			ditInputs.put("x", xTensor);
			ditInputs.put("t", tTensor);
			ditInputs.put("cross_attn_cond", crossAttentionInput);
			ditInputs.put("global_cond", globalCond);

			// Run DiT model
			OrtSession.Result ditResult = ditSession.run(ditInputs);
			OnnxTensor ditOutput = (OnnxTensor) ditResult.get(0);

			// Get output data
			FloatBuffer outputBuffer = ditOutput.getFloatBuffer();
			float[] outputData = new float[DIT_X_SIZE];
			outputBuffer.get(outputData);

			// Get current x data
			FloatBuffer xBuffer = xTensor.getFloatBuffer();
			float[] xData = new float[DIT_X_SIZE];
			xBuffer.get(xData);

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
			for (int i = 0; i < DIT_X_SIZE; i++) {
				x[i] = ((1.0f - nextT) * outputData[i]) + (nextT * newNoise[i]);
			}

			// Clean up
			xTensor.close();
			tTensor.close();
			ditOutput.close();

			// Create new x tensor for next step
			xTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(x), DIT_X_SHAPE);
		}

		return xTensor;
	}

	private double[][] decodeAudio(OnnxTensor latent) throws OrtException {
		// Run autoencoder
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("sampled", latent);

		OrtSession.Result result = autoencoderSession.run(inputs);
		OnnxTensor audioTensor = (OnnxTensor) result.get(0);

		// Get audio data
		FloatBuffer audioBuffer = audioTensor.getFloatBuffer();
		int totalSamples = audioBuffer.capacity();
		int channelSamples = totalSamples / 2; // Stereo audio

		// Create stereo audio array
		double[][] stereoAudio = new double[2][channelSamples];
		for (int i = 0; i < channelSamples; i++) {
			stereoAudio[0][i] = audioBuffer.get(i);                  // Left channel
			stereoAudio[1][i] = audioBuffer.get(i + channelSamples); // Right channel
		}

		audioTensor.close();
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

	@Override
	public void close() throws Exception {
		if (conditionersSession != null) conditionersSession.close();
		if (ditSession != null) ditSession.close();
		if (autoencoderSession != null) autoencoderSession.close();
		if (env != null) env.close();
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("Usage: java -jar audiogen.jar <models_path> <prompt> <output_path>");
			return;
		}

		String modelsPath = args[0];
		String prompt = args[1];
		String outputPath = args[2];
		long seed = 99; // Fixed seed for reproducibility

		try {
			// Initialize tokenizer and generators
			SpTokenizer tokenizer = new SpTokenizer(AudioGenerator.class.getClassLoader().getResourceAsStream("spiece.model"));
			SpVocabulary vocabulary = SpVocabulary.from(tokenizer);

			AudioGenerator generator = new AudioGenerator(modelsPath);

			// Start timing
			long startTime = System.currentTimeMillis();

			// 1. Tokenize the prompt
			System.out.println("Tokenizing prompt: " + prompt);
			long[] tokenIds = tokenizer.tokenize(prompt).stream().mapToLong(vocabulary::getIndex).toArray();

			// 2. Generate audio
			System.out.println("Generating audio...");
			double[][] stereoAudio = generator.generateAudio(tokenIds, seed);

			// 3. Save as WAV
			System.out.println("Saving audio to " + outputPath);
			try (WavFile f = WavFile.newWavFile(new File(outputPath), 2, stereoAudio[0].length, 32, 44100)) {
				f.writeFrames(stereoAudio);
			}

			// Report time
			long endTime = System.currentTimeMillis();
			System.out.println("Total generation time: " + (endTime - startTime) + " ms");

			// Clean up
			generator.close();

		} catch (Exception e) {
			System.err.println("Error generating audio: " + e.getMessage());
			e.printStackTrace();
		}
	}
}