package org.almostrealism.audio.feature.test;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Tensor;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.feature.FeatureComputer;
import org.almostrealism.audio.feature.FeatureExtractor;
import org.almostrealism.audio.feature.FeatureSettings;
import org.almostrealism.collect.PackedCollection;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureComputerTest {
	private static final ThreadLocal<FeatureComputer> computers = new ThreadLocal<>();

	public boolean call() throws IOException {
		computeFeatures(WavFile.openWavFile(
				new File("/Users/michael/CLionProjects/kaldi/test-16khz.wav")),
				FeatureExtractor::print);
		return true;
	}

	// TODO  @Test
	public void singleThreaded() throws IOException { assert call(); }

	// TODO  @Test
	public void multiThreaded() {
		ExecutorService executor = Executors.newFixedThreadPool(8);

		List<Future> futures = (List<Future>) IntStream.range(0, 10)
				.mapToObj(i -> (Callable) this::call)
				.map(executor::submit)
				.collect(Collectors.toList());

		// TODO  check that all futures completed
	}

	public static void computeFeatures(WavFile file, Consumer<Tensor<Scalar>> output) throws IOException {
		FeatureComputer mfcc = computers.get();

		if (mfcc == null) {
			FeatureSettings settings = new FeatureSettings();
			mfcc = new FeatureComputer(settings);
			computers.set(mfcc);
		}

		Scalar minDuration = new Scalar(0.0);

		if (file.getDuration() < minDuration.getValue()) {
			throw new IllegalArgumentException("File is too short (" +
					file.getDuration() + " sec): producing no output.");
		}

		int[][] wave = new int[file.getNumChannels()][(int) file.getFramesRemaining()];
		file.readFrames(wave, 0, (int) file.getFramesRemaining());

		int channelCount = file.getNumChannels();

		assert channelCount > 0;
		int channel = 0;

		PackedCollection<Scalar> waveform = WavFile.channelScalar(wave, channel);
		Tensor<Scalar> features = new Tensor<>();
		double vtlnWarp = 1.0;
		mfcc.computeFeatures(waveform, new Scalar(file.getSampleRate()), vtlnWarp, features);
		output.accept(features);

		System.out.println("Processed features");
	}

	public static void print(Tensor t) { System.out.println(t.toHTML()); }
}
