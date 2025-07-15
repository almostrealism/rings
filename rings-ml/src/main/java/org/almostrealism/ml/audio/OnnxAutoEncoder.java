/*
 * Copyright 2025 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.ml.audio;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareException;
import org.almostrealism.ml.OnnxFeatures;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class OnnxAutoEncoder implements AutoEncoder, OnnxFeatures {
	public static final int SAMPLE_RATE = 44100;
	public static final int FRAME_COUNT = 524288;

	private final OrtEnvironment env;
	private final OrtSession encoderSession;
	private final OrtSession decoderSession;
	private boolean destroyEnv;

	public OnnxAutoEncoder(String encoderModelPath,
						   String decoderModelPath) throws OrtException {
		this(OrtEnvironment.getEnvironment(),
				OnnxFeatures.defaultOptions(),
				encoderModelPath, decoderModelPath);
		this.destroyEnv = true;
	}

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
	public double getSampleRate() { return SAMPLE_RATE; }

	@Override
	public double getLatentSampleRate() {
		double ratio = 256.0 / FRAME_COUNT;
		return getSampleRate() * ratio;
	}

	@Override
	public PackedCollection<?> encode(PackedCollection<?> audio) {
		Map<String, OnnxTensor> inputs = new HashMap<>();

		if (audio.getShape().getDimensions() == 1 || audio.getShape().length(0) == 1) {
			float data[] = audio.toFloatArray();

			// Duplicate mono audio data to create stereo input
			FloatBuffer buf = FloatBuffer.allocate(2 * data.length);
			buf.put(data);
			buf.put(data);
			buf.position(0);
			inputs.put("audio", packOnnx(shape(2, data.length), buf));
		} else if (audio.getShape().length(0) == 2) {
			inputs.put("audio", toOnnx(audio));
		} else {
			throw new IllegalArgumentException(audio.getShape() +
					" is not a valid shape for audio data");
		}

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

			if (destroyEnv) {
				env.close();
			}
		} catch (OrtException e) {
			throw new HardwareException("Failed to close ONNX sessions", e);
		}
	}
}
