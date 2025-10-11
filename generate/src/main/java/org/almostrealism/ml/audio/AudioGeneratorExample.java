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

import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;

import java.io.File;
import java.util.Random;

/**
 * Example demonstrating both pure generation and sample-based generation
 * with matched noise addition strategy.
 */
public class AudioGeneratorExample {

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java AudioGeneratorExample <models_path> <output_path> <prompt> [sample1.wav sample2.wav ...]");
			System.out.println("\nExamples:");
			System.out.println("  Pure generation:");
			System.out.println("    java AudioGeneratorExample /path/to/models /path/to/output \"crispy snare drum\"");
			System.out.println("\n  Sample-based generation:");
			System.out.println("    java AudioGeneratorExample /path/to/models /path/to/output \"punchy snare\" snare1.wav snare2.wav snare3.wav");
			return;
		}

		String modelsPath = args[0];
		String outputPath = args[1];
		String prompt = args[2];

		// Check if samples were provided
		boolean useSamples = args.length > 3;

		try (AudioGenerator generator = new AudioGenerator(modelsPath)) {
			generator.setAudioDurationSeconds(1.2);

			if (useSamples) {
				// ============================================================
				// Sample-based generation (audio-to-audio / img2img style)
				// ============================================================
				System.out.println("\n=== Sample-Based Generation ===");
				System.out.println("Prompt: \"" + prompt + "\"");
				System.out.println("Loading " + (args.length - 3) + " samples...");

				// Load and add samples
				for (int i = 3; i < args.length; i++) {
					System.out.println("  Loading: " + args[i]);
					WaveData wave = WaveData.load(new File(args[i]));
					generator.addAudio(wave.getData());
				}

				// Generate variations with different strength levels
				double[] strengths = {0.2, 0.4, 0.6, 0.8};

				Random rand = new Random(42);

				for (double strength : strengths) {
					generator.setStrength(strength);

					for (int i = 0; i < 3; i++) {
						// Create random position vector for interpolation
						PackedCollection<?> position = new PackedCollection<>(
								new TraversalPolicy(generator.getComposerDimension()))
								.fill(rand::nextGaussian);

						long seed = rand.nextLong();

						String filename = String.format("%s/sample_based_strength%.1f_%d_seed%d.wav",
								outputPath, strength, i, seed);

						System.out.println("Generating with strength=" + strength +
								", seed=" + seed);

						generator.generateAudioFromSamples(position, prompt, seed, filename);
					}
				}

				System.out.println("\nSample-based generation complete!");
				System.out.println("Generated " + (strengths.length * 3) + " variations");

			} else {
				// ============================================================
				// Pure generation (standard text-to-audio)
				// ============================================================
				System.out.println("\n=== Pure Generation (No Samples) ===");
				System.out.println("Prompt: \"" + prompt + "\"");

				Random rand = new Random(42);

				// Generate multiple variations with different seeds
				int numVariations = 5;
				for (int i = 0; i < numVariations; i++) {
					long seed = rand.nextLong();
					String filename = String.format("%s/pure_gen_%d_seed%d.wav",
							outputPath,
							i,
							seed);

					System.out.println("Generating variation " + (i + 1) + "/" + numVariations +
							" (seed=" + seed + ")");
					generator.generateAudio(prompt, seed, filename);
				}

				System.out.println("\nPure generation complete!");
				System.out.println("Generated " + numVariations + " variations");
			}
		}
	}
}
