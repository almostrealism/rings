package org.almostrealism.ml.audio;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.Console;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.nio.file.Path;

public class AudioModulator implements AutoCloseable, CodeFeatures {
	private final OrtEnvironment env;
	private final AutoEncoder autoencoder;

	public AudioModulator(String modelsPath) throws OrtException {
		this(new AssetGroup(
						new Asset(new File(modelsPath + "/encoder.onnx")),
						new Asset(new File(modelsPath + "/decoder.onnx"))));
	}

	public AudioModulator(AssetGroup onnxAssets) throws OrtException {
		env = OrtEnvironment.getEnvironment();

		OrtSession.SessionOptions options = new OrtSession.SessionOptions();
		options.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());
		options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
		autoencoder = new OnnxAutoEncoder(env, options,
				onnxAssets.getAssetPath("encoder.onnx"),
				onnxAssets.getAssetPath("decoder.onnx"));
	}

	public PackedCollection<?> project(PackedCollection<?> audio) {
		TraversalPolicy shape = shape(2, OnnxAutoEncoder.FRAME_COUNT);
		PackedCollection<?> paddedAudio = pad(shape, cp(audio), 0, 0).evaluate();
		PackedCollection<?> encoded = autoencoder.encode(paddedAudio);

		TraversalPolicy encodedShape = encoded.getShape();
		log("Encoded shape = " + encodedShape);

		double a = 0.7;
		double b = (1 - a) * 1.3;
		PackedCollection<?> noise = new PackedCollection<>(encodedShape).randnFill();
		PackedCollection<?> sum = cp(encoded).multiply(c(a)).add(cp(noise).multiply(c(b))).evaluate();
		return autoencoder.decode(sum);
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java AudioModulator <models_path> <input> <output_path>");
			return;
		}

		String modelsPath = args[0];
		String input = args[1];
		String outputPath = args[2];

		try (AudioModulator modulator = new AudioModulator(modelsPath)) {
			WaveData wave = WaveData.loadMultiChannel(new File(input));
			if (wave.getSampleRate() != 44100) {
				throw new IllegalArgumentException();
			}

			PackedCollection<?> data = modulator.project(wave.getCollection());
			WaveData out = new WaveData(data, wave.getSampleRate());

			Path p = Path.of(outputPath).resolve("modulated.wav");
			out.saveMultiChannel(p.toFile());
			Console.root().features(AudioModulator.class)
					.log("Saved modulated audio to " + p);
		}
	}

	@Override
	public void close() throws Exception {
		autoencoder.destroy();
		env.close();
	}
}
