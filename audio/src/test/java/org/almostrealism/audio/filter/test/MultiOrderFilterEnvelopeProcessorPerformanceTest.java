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

package org.almostrealism.audio.filter.test;

import io.almostrealism.profile.OperationProfileNode;
import org.almostrealism.audio.filter.MultiOrderFilterEnvelopeProcessor;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.Hardware;
import org.almostrealism.util.TestFeatures;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Performance test for {@link MultiOrderFilterEnvelopeProcessor} that replicates
 * real-world usage patterns by loading histogram data from actual runs.
 */
public class MultiOrderFilterEnvelopeProcessorPerformanceTest implements TestFeatures {
	public static final int SAMPLE_RATE = OutputLine.sampleRate;
	public static final double MAX_SECONDS = 90.0;

	public static boolean enableProfile = true;

	/**
	 * Loads histogram data from results/filter_dist.csv and runs the processor
	 * with the same distribution of input sizes to measure realistic performance.
	 * <p>
	 * This is a full-scale test that replicates all 400k+ calls from the histogram.
	 * Use {@link #realisticDistributionScaled()} for a faster test.
	 * </p>
	 */
	@Test
	public void realisticDistribution() throws IOException {
		OperationProfileNode profile = enableProfile ?
				new OperationProfileNode("MultiOrderFilterEnvelopeProcessorPerformanceTest") : null;
		Hardware.getLocalHardware().assignProfile(profile);

		try {
			runRealisticDistribution(1.0);
		} finally {
			if (profile != null) {
				profile.save("results/filterEnvelope.xml");
			}
		}
	}

	/**
	 * Runs a scaled-down version of the realistic distribution test (10% of calls).
	 * This is faster while still maintaining the distribution shape.
	 */
	@Test
	public void realisticDistributionScaled() throws IOException {
		OperationProfileNode profile = enableProfile ?
				new OperationProfileNode("MultiOrderFilterEnvelopeProcessorPerformanceTest") : null;
		Hardware.getLocalHardware().assignProfile(profile);

		try {
			runRealisticDistribution(0.1);
		} finally {
			if (profile != null) {
				profile.save("results/filterEnvelopeScaled.xml");
			}
		}
	}

	/**
	 * Runs the realistic distribution test with a configurable scale factor.
	 *
	 * @param scaleFactor  Fraction of calls to process (0.0 to 1.0)
	 */
	private void runRealisticDistribution(double scaleFactor) throws IOException {
		// Try multiple possible paths for the histogram file
		File histogramFile = new File("audio/results/filter_dist.csv");
		if (!histogramFile.exists()) {
			histogramFile = new File("results/filter_dist.csv");
		}
		if (!histogramFile.exists()) {
			histogramFile = new File("../audio/results/filter_dist.csv");
		}

		if (!histogramFile.exists()) {
			log("Histogram file not found.");
			log("Tried paths:");
			log("  - audio/results/filter_dist.csv");
			log("  - results/filter_dist.csv");
			log("  - ../audio/results/filter_dist.csv");
			log("Skipping performance test");
			return;
		}

		log("Using histogram file: " + histogramFile.getAbsolutePath());

		// Create processor
		MultiOrderFilterEnvelopeProcessor processor =
			new MultiOrderFilterEnvelopeProcessor(SAMPLE_RATE, MAX_SECONDS);

		// Configure ADSR parameters (typical values)
		processor.setDuration(5.0);
		processor.setAttack(0.5);
		processor.setDecay(1.0);
		processor.setSustain(0.7);
		processor.setRelease(2.0);

		// Load the histogram to get the distribution
		processor.loadHistogram(histogramFile);
		long[] histogram = processor.getHistogram();

		// Calculate total number of calls
		long totalCalls = 0;
		for (long count : histogram) {
			totalCalls += count;
		}

		log("Loaded histogram with " + totalCalls + " total calls");
		log("Scale factor: " + (scaleFactor * 100) + "%");

		// Generate input sizes matching the distribution
		List<Integer> inputSizes = generateInputSizes(histogram, scaleFactor);
		log("Generated " + inputSizes.size() + " test inputs");

		// Warm-up phase
		log("Warming up...");
		warmUp(processor, 100);

		// Run performance test
		log("Running performance test...");
		long startTime = System.nanoTime();
		long totalFramesProcessed = 0;

		for (int inputSize : inputSizes) {
			PackedCollection<?> input = new PackedCollection<>(inputSize);
			PackedCollection<?> output = new PackedCollection<>(inputSize);

			// Fill input with test data (silence is fine for performance testing)
			processor.process(input, output);

			totalFramesProcessed += inputSize;

			input.destroy();
			output.destroy();
		}

		long endTime = System.nanoTime();
		double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;

		// Report results
		log("\n=== Performance Results ===");
		log("Total calls: " + inputSizes.size());
		log("Total frames processed: " + totalFramesProcessed);
		log("Total time: " + String.format("%.3f", elapsedSeconds) + " seconds");
		log("Average time per call: " +
			String.format("%.3f", (elapsedSeconds / inputSizes.size()) * 1000) + " ms");
		log("Throughput: " +
			String.format("%.2f", totalFramesProcessed / elapsedSeconds) + " frames/sec");
		log("Throughput: " +
			String.format("%.2f", (totalFramesProcessed / SAMPLE_RATE) / elapsedSeconds) + "x realtime");

		processor.destroy();
	}

	/**
	 * Generates a list of input sizes matching the histogram distribution.
	 *
	 * @param histogram    The histogram bin counts
	 * @param scaleFactor  Fraction of calls to generate (0.0 to 1.0)
	 * @return List of input frame sizes to use for testing
	 */
	private List<Integer> generateInputSizes(long[] histogram, double scaleFactor) {
		List<Integer> sizes = new ArrayList<>();
		Random random = new Random(42); // Fixed seed for reproducibility

		for (int bin = 0; bin < histogram.length; bin++) {
			long count = histogram[bin];
			if (count == 0) continue;

			// Apply scale factor to count
			long scaledCount = Math.max(1, (long) (count * scaleFactor));

			int minFrames = MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MIN_FRAMES +
				(bin * ((MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MAX_FRAMES -
					MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MIN_FRAMES) /
					MultiOrderFilterEnvelopeProcessor.HISTOGRAM_BINS));
			int maxFrames = minFrames +
				((MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MAX_FRAMES -
					MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MIN_FRAMES) /
					MultiOrderFilterEnvelopeProcessor.HISTOGRAM_BINS) - 1;

			if (bin == histogram.length - 1) {
				maxFrames = MultiOrderFilterEnvelopeProcessor.HISTOGRAM_MAX_FRAMES;
			}

			// Generate 'scaledCount' random sizes within this bin's range
			for (int i = 0; i < scaledCount; i++) {
				int size = minFrames + random.nextInt(maxFrames - minFrames + 1);
				sizes.add(size);
			}
		}

		// Shuffle to avoid sequential processing of same-sized inputs
		Collections.shuffle(sizes, random);

		return sizes;
	}

	/**
	 * Warm-up phase to ensure JIT compilation and cache warming.
	 *
	 * @param processor  The processor to warm up
	 * @param iterations Number of warm-up iterations
	 */
	private void warmUp(MultiOrderFilterEnvelopeProcessor processor, int iterations) {
		Random random = new Random(123);

		for (int i = 0; i < iterations; i++) {
			int size = 50000 + random.nextInt(100000);
			PackedCollection<?> input = new PackedCollection<>(size);
			PackedCollection<?> output = new PackedCollection<>(size);

			processor.process(input, output);

			input.destroy();
			output.destroy();
		}
	}

	/**
	 * Simple performance test with a single input size to establish baseline.
	 */
	@Test
	public void baseline() {
		log("Running baseline performance test...");

		MultiOrderFilterEnvelopeProcessor processor =
			new MultiOrderFilterEnvelopeProcessor(SAMPLE_RATE, MAX_SECONDS);

		processor.setDuration(5.0);
		processor.setAttack(0.5);
		processor.setDecay(1.0);
		processor.setSustain(0.7);
		processor.setRelease(2.0);

		// Warm-up
		warmUp(processor, 50);

		// Test with a typical size
		int testSize = 132000; // ~3 seconds at 44100 Hz
		int iterations = 1000;

		log("Testing with " + iterations + " iterations of " + testSize + " frames");

		long startTime = System.nanoTime();

		for (int i = 0; i < iterations; i++) {
			PackedCollection<?> input = new PackedCollection<>(testSize);
			PackedCollection<?> output = new PackedCollection<>(testSize);

			processor.process(input, output);

			input.destroy();
			output.destroy();
		}

		long endTime = System.nanoTime();
		double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;

		log("\n=== Baseline Results ===");
		log("Total time: " + String.format("%.3f", elapsedSeconds) + " seconds");
		log("Average time per call: " +
			String.format("%.3f", (elapsedSeconds / iterations) * 1000) + " ms");
		log("Throughput: " +
			String.format("%.2f", (testSize * iterations) / elapsedSeconds) + " frames/sec");

		processor.destroy();
	}
}
