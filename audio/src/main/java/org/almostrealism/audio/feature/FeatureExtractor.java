package org.almostrealism.audio.feature;

import org.almostrealism.audio.WavFile;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Tensor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeatureExtractor {
	private static final ThreadLocal<FeatureComputer> computers = new ThreadLocal<>();

	public static void main(String args[]) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(1);

		List<Future<?>> futures = IntStream.range(0, 10).mapToObj(i -> (Runnable) () -> {
			try {
				main(
						Collections.singletonList(WavFile.openWavFile(
								new File("/Users/michael/CLionProjects/kaldi/test-16khz.wav"))),
						FeatureExtractor::print);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).map(executor::submit).collect(Collectors.toList());

		f: while (true) {
			for (Future<?> future : futures) {
				if (!future.isDone()) {
					Thread.sleep(5000);
					continue f;
				}
			}

			break f;
		}

		executor.submit(() -> {
			File file = new File("/Users/michael/Desktop/feature-log.csv");

			try (FileOutputStream fs = new FileOutputStream(file);
				 BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fs))) {
				out.write(computers.get().getLogTensor().toCSV());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).get();

		System.exit(0);
	}

	public static int main(List<WavFile> files, Consumer<Tensor<Scalar>> output) throws IOException {
		FeatureComputer mfcc = computers.get();

		if (mfcc == null) {
			FeatureSettings settings = new FeatureSettings();
			mfcc = new FeatureComputer(settings);
			computers.set(mfcc);
		}

		double vtlnWarp = 1.0;
		Scalar minDuration = new Scalar(0.0);

		int index = 0;
		int uttCount = 0, successCount = 0;
		for (WavFile f : files) {
			uttCount++;
			String utt = String.valueOf(index++);

			if (f.getDuration() < minDuration.getValue()) {
				System.out.println(utt + " is too short (" +
						f.getDuration() + " sec): producing no output.");
				continue;
			}

			int[][] wave = new int[f.getNumChannels()][(int) f.getFramesRemaining()];
			f.readFrames(wave, 0, (int) f.getFramesRemaining());

			int channelCount = f.getNumChannels();

			assert channelCount > 0;
			int channel = 0;

			ScalarBank waveform = WavFile.channelScalar(wave, channel);
			Tensor<Scalar> features = new Tensor<>();

			try {
				mfcc.computeFeatures(waveform, new Scalar(f.getSampleRate()), vtlnWarp, features);
			} catch (Exception e) {
				System.out.println("Failed to compute features for utterance " + utt);
				e.printStackTrace();
				continue;
			}

			output.accept(features);

			if (uttCount % 10 == 0)
				System.out.println("Processed " + uttCount + " utterances");
			System.out.println("Processed features for key " + utt);
			successCount++;
		}

		System.out.println(" Done " + successCount + " out of " + uttCount + " utterances.");
		return successCount != 0 ? 0 : 1;
	}

	public static void print(Tensor t) {
		System.out.println(Arrays.toString(IntStream.range(0, t.length()).mapToDouble(i -> ((Scalar) t.get(i, 0)).getValue()).toArray()));
	}
}
