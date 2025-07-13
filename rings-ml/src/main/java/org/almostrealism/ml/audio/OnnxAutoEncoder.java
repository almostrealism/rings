package org.almostrealism.ml.audio;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareException;
import org.almostrealism.ml.OnnxFeatures;

import java.util.HashMap;
import java.util.Map;

public class OnnxAutoEncoder implements AutoEncoder, OnnxFeatures {
	public static final int FRAME_COUNT = 524288;

	private final OrtEnvironment env;
	private final OrtSession encoderSession;
	private final OrtSession decoderSession;

	public OnnxAutoEncoder(OrtEnvironment environment,
						   OrtSession.SessionOptions options,
						   String encoderModelPath,
						   String decoderModelPath) throws OrtException {
		env = environment;
		encoderSession = env.createSession(encoderModelPath, options);
		decoderSession = env.createSession(decoderModelPath, options);
	}

	@Override
	public OrtEnvironment getOnnxEnvironment() { return env; }

	@Override
	public PackedCollection<?> encode(PackedCollection<?> audio) {
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("audio", toOnnx(audio));

		OnnxTensor latentTensor = null;

		try {
			OrtSession.Result result = encoderSession.run(inputs);
			latentTensor = (OnnxTensor) result.get(0);
			return pack(latentTensor);
		} catch (OrtException e) {
			throw new HardwareException("Unable to run ONNX encoder", e);
		} finally {
			if (latentTensor != null)
				latentTensor.close();
		}
	}

	@Override
	public PackedCollection<?> decode(PackedCollection<?> latent) {
		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("sampled", toOnnx(latent));

		OnnxTensor audioTensor = null;

		try {
			OrtSession.Result result = decoderSession.run(inputs);
			audioTensor = (OnnxTensor) result.get(0);
			return pack(audioTensor);
		} catch (OrtException e) {
			throw new HardwareException("Unable to run ONNX decoder", e);
		} finally {
			if (audioTensor != null)
				audioTensor.close();
		}
	}


	@Override
	public void destroy() {
		try {
			encoderSession.close();
			decoderSession.close();
		} catch (OrtException e) {
			throw new HardwareException("Failed to close ONNX sessions", e);
		}
	}
}
