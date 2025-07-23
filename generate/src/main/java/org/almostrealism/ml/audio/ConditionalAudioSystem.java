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

import ai.djl.sentencepiece.SpTokenizer;
import ai.djl.sentencepiece.SpVocabulary;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import io.almostrealism.lifecycle.Destroyable;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareException;
import org.almostrealism.ml.OnnxFeatures;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.persistence.AssetGroup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class ConditionalAudioSystem implements Destroyable, OnnxFeatures {
	public static boolean enableOnnxDit = false;

	public static double MAX_DURATION = 11.0;

	protected static final int SAMPLE_RATE = 44100;
	protected static final int NUM_STEPS = 8;
	protected static final float LOGSNR_MAX = -6.0f;
	protected static final float SIGMA_MIN = 0.0f;
	protected static final float SIGMA_MAX = 1.0f;

	// Model dimensions
	protected static final long[] DIT_X_SHAPE = new long[] { 1, 64, 256 };
	protected static final int DIT_X_SIZE = 64 * 256;
	protected static final int T5_SEQ_LENGTH = 128;

	private final SpTokenizer tokenizer;
	private final SpVocabulary vocabulary;

	private final OrtEnvironment env;
	private final OrtSession conditionersSession;
	private final AutoEncoder autoencoder;
	private final DitModel ditModel;

	public ConditionalAudioSystem(AssetGroup onnxAssets, StateDictionary ditStates)
			throws OrtException, IOException {
		tokenizer = new SpTokenizer(
				ConditionalAudioSystem.class.getClassLoader()
					.getResourceAsStream("spiece.model"));
		vocabulary = SpVocabulary.from(tokenizer);

		env = OrtEnvironment.getEnvironment();

		OrtSession.SessionOptions options = new OrtSession.SessionOptions();
		options.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());
		options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

		conditionersSession = env.createSession(onnxAssets.getAssetPath("conditioners.onnx"), options);
		autoencoder = new OnnxAutoEncoder(env, options,
				onnxAssets.getAssetPath("encoder.onnx"),
				onnxAssets.getAssetPath("decoder.onnx"));

		if (enableOnnxDit) {
			ditModel = new OnnxDitModel(env, options, onnxAssets.getAssetPath("dit.onnx"));
		} else {
			ditModel = new DiffusionTransformer(
					64,
					1024,
					16,
					8,
					1,
					768,
					768,
					"rf_denoiser",
					ditStates
			);
		}
	}

	public SpTokenizer getTokenizer() { return tokenizer; }
	public SpVocabulary getVocabulary() { return vocabulary; }

	@Override
	public OrtEnvironment getOnnxEnvironment() { return env; }
	public OrtSession getConditionersSession() { return conditionersSession; }
	public AutoEncoder getAutoencoder() { return autoencoder; }

	public DitModel getDitModel() { return ditModel; }

	protected Map<String, PackedCollection<?>> runConditioners(long[] ids, double seconds) throws OrtException {
		long[] paddedIds = new long[T5_SEQ_LENGTH];
		long[] attentionMask = new long[T5_SEQ_LENGTH];

		int tokenCount = Math.min(ids.length, T5_SEQ_LENGTH);
		System.arraycopy(ids, 0, paddedIds, 0, tokenCount);
		for (int i = 0; i < tokenCount; i++) {
			attentionMask[i] = 1;
		}

		Map<String, OnnxTensor> inputs = new HashMap<>();
		inputs.put("input_ids", packOnnx(shape(1, T5_SEQ_LENGTH), paddedIds));
		inputs.put("attention_mask", packOnnx(shape(1, T5_SEQ_LENGTH), attentionMask));
		inputs.put("seconds_total", packOnnx(shape(1), (float) seconds));


		Map<String, OnnxTensor> outputs = new HashMap<>();

		try {
			OrtSession.Result result =
					getConditionersSession().run(inputs);
			outputs.put("cross_attention_input", (OnnxTensor) result.get(0));
			outputs.put("cross_attention_masks", (OnnxTensor) result.get(1));
			outputs.put("global_cond", (OnnxTensor) result.get(2));
			return pack(outputs);
		} finally {
			inputs.forEach((key, tensor) -> tensor.close());
			outputs.forEach((key, tensor) -> tensor.close());
		}
	}

	@Override
	public void destroy() {
		if (conditionersSession != null) {
			try {
				conditionersSession.close();
			} catch (OrtException e) {
				throw new HardwareException("Unable to close conditioner session", e);
			}
		}
		if (autoencoder != null) autoencoder.destroy();
		if (ditModel != null) ditModel.destroy();
		if (env != null) env.close();
	}
}
