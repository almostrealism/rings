package org.almostrealism.audioml;

import org.almostrealism.audioml.audio.AudioUtils;
import org.almostrealism.audioml.model.DiffusionProcess;
import org.almostrealism.audioml.model.ModelHandler;
import org.almostrealism.audioml.text.TextTokenizer;
import org.tensorflow.TensorFlow;

public class AudioGen {
	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("Usage: java AudioGen <models_base_path> <prompt> <num_threads>");
			System.exit(1);
		}

		String modelsBasePath = args[0];
		String prompt = args[1];
		int numThreads = Integer.parseInt(args[2]);
		String outputPath = "output.wav";
		long seed = 99;

		System.out.println("TensorFlow version: " + TensorFlow.version());
		System.out.println("Using models from: " + modelsBasePath);
		System.out.println("Prompt: " + prompt);
		System.out.println("Number of threads: " + numThreads);

		// Configure TensorFlow to use specified number of threads
		// Note: In full TensorFlow, this can be done with various configurations
		System.setProperty("org.tensorflow.NativeLibrary.VERBOSE", "1");

		try (ModelHandler modelHandler = new ModelHandler()) {
			long startTime = System.currentTimeMillis();

			// Step 1: Load models
			System.out.println("Loading models...");
			modelHandler.loadModels(modelsBasePath);

			// Step 2: Initialize text tokenizer
			System.out.println("Initializing tokenizer...");
			TextTokenizer tokenizer = new TextTokenizer(modelsBasePath + "/spiece.model");
			long[] promptIds = tokenizer.convertPromptToIds(prompt);

			// Step 3: Prepare attention mask
			long[] attentionMask = new long[promptIds.length];
			for (int i = 0; i < promptIds.length; i++) {
				attentionMask[i] = 1;
			}

			// Step 4: Run diffusion process
			System.out.println("Running diffusion process...");
			DiffusionProcess diffusion = new DiffusionProcess(modelHandler);
			float[] audioData = diffusion.runDiffusion(promptIds, attentionMask, seed);

			// Step 5: Save output as WAV file
			System.out.println("Saving output to " + outputPath);
			AudioUtils.saveAsWav(outputPath, audioData);

			long totalTime = System.currentTimeMillis() - startTime;
			System.out.println("Total processing time: " + totalTime + " ms");

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
}