package org.almostrealism.audioml.model;

import org.almostrealism.audioml.utils.TensorUtils;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.FloatNdArray;
import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.util.Random;

public class DiffusionProcess {
	// Constants from the C++ implementation
	private static final int NUM_STEPS = 8;
	private static final float AUDIO_LEN_SEC = 10.0f;
	private static final float LOGSNR_MAX = -6.0f;
	private static final float SIGMA_MIN = 0.0f;
	private static final float SIGMA_MAX = 1.0f;

	private final ModelHandler modelHandler;

	public DiffusionProcess(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
	}

	public float[] runDiffusion(long[] promptIds, long[] attentionMask, long seed) {
		// Step 1: Run T5 model to get conditioning
		var t5Output = modelHandler.runT5Model(promptIds, attentionMask, AUDIO_LEN_SEC);
		Tensor crossAttn = t5Output.get("crossAttn");
		Tensor globalCond = t5Output.get("globalCond");

		// Step 2: Prepare for diffusion
		long startTime = System.currentTimeMillis();

		// Get DiT input dimensions from crossAttn shape
		Shape crossAttnShape = ((TFloat32)crossAttn).shape();
		Shape xShape = Shape.of(1, 32, 32); // This may need adjustment based on model requirements

		// Initialize x with random normal distribution
		TFloat32 x = TensorUtils.createRandomNormalTensor(xShape, seed);

		// Calculate sigmas for diffusion steps
		float[] sigmas = calculateSigmas(NUM_STEPS + 1);

		// Step 3: Run diffusion process
		for (int i = 0; i < NUM_STEPS; i++) {
			float currentT = sigmas[i];
			float nextT = sigmas[i + 1];

			// Create time tensor
			TFloat32 tTensor = TFloat32.scalarOf(currentT);

			// Run DiT model
			Tensor ditOutput = modelHandler.runDitModel(x, tTensor, crossAttn, globalCond);

			// Update x with sampler ping-pong
			x = samplerPingPong((TFloat32)ditOutput, x, currentT, nextT, i, seed + i + 4564);

			// Close the temporary tensors
			tTensor.close();
			ditOutput.close();
		}

		// Step 4: Run autoencoder to generate final audio
		Tensor autoencoderOutput = modelHandler.runAutoencoderModel(x);

		// Convert to float array
		float[] audioData = TensorUtils.tensorToFloatArray((TFloat32)autoencoderOutput);

		// Clean up
		crossAttn.close();
		globalCond.close();
		x.close();
		autoencoderOutput.close();

		long endTime = System.currentTimeMillis();
		System.out.println("DiT process completed in " + (endTime - startTime) + " ms");

		return audioData;
	}

	private float[] calculateSigmas(int size) {
		float[] sigmas = new float[size];

		// Linear space from logsnr_max to 2.0
		float start = LOGSNR_MAX;
		float end = 2.0f;
		float step = (end - start) / (size - 1);

		// Fill linear values
		sigmas[0] = start;
		sigmas[size - 1] = end;
		for (int i = 1; i < size - 1; i++) {
			sigmas[i] = sigmas[i - 1] + step;
		}

		// Convert to sigmas
		for (int i = 0; i < size; i++) {
			sigmas[i] = 1.0f / (1.0f + (float)Math.exp(sigmas[i]));
		}

		// Set boundary values
		sigmas[0] = SIGMA_MAX;
		sigmas[size - 1] = SIGMA_MIN;

		return sigmas;
	}

	private TFloat32 samplerPingPong(TFloat32 ditOutput, TFloat32 xInput, float curT, float nextT, int stepIdx, long seed) {
		Shape shape = xInput.shape();
		int size = (int)shape.size();

		// Convert tensors to float arrays for easier manipulation
		float[] outputArray = TensorUtils.tensorToFloatArray(ditOutput);
		float[] xArray = TensorUtils.tensorToFloatArray(xInput);

		// Step 1: Modify output data based on current state
		for (int i = 0; i < size; i++) {
			outputArray[i] = xArray[i] - (curT * outputArray[i]);
		}

		// Step 2: Generate new random noise
		float[] noiseArray = new float[size];
		Random random = new Random(seed);
		for (int i = 0; i < size; i++) {
			noiseArray[i] = (float)random.nextGaussian();
		}

		// Step 3: Update x
		for (int i = 0; i < size; i++) {
			xArray[i] = ((1.0f - nextT) * outputArray[i]) + (nextT * noiseArray[i]);
		}

		// Create new tensor from updated array
		return TensorUtils.createTensorFromArray(xArray, shape);
	}
}