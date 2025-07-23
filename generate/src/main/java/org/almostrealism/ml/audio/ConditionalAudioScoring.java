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

import ai.onnxruntime.OrtException;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ConditionalAudioScoring extends ConditionalAudioSystem {

	public ConditionalAudioScoring(String modelsPath) throws OrtException, IOException {
		this(new AssetGroup(new Asset(new File(modelsPath + "/conditioners.onnx")),
						new Asset(new File(modelsPath + "/encoder.onnx")),
						new Asset(new File(modelsPath + "/decoder.onnx")),
						new Asset(new File(modelsPath + "/dit.onnx"))),
				new StateDictionary(modelsPath + "/weights"));
	}

	public ConditionalAudioScoring(AssetGroup onnxAssets, StateDictionary ditStates)
			throws OrtException, IOException {
		super(onnxAssets, ditStates);
	}

	public double computeScore(long promptTokenIds[], PackedCollection<?> audio, double duration) throws OrtException {
		// 1. Process tokens through conditioners
		Map<String, PackedCollection<?>> conditionerOutputs = runConditioners(promptTokenIds, duration);

		// 2. Get audio latent
		PackedCollection<?> audioLatent = getAutoencoder().encode(cp(audio)).evaluate();

		// 3. Run one forward pass of DIT with conditioning
		getDitModel().forward(audioLatent, pack(0.5),
						conditionerOutputs.get("cross_attention_input"),
						conditionerOutputs.get("global_cond"));

		// TODO 4. Extract cross-attention weights

		// TODO 5. Aggregate attention as compatibility score
		return 0.0;
	}
}
