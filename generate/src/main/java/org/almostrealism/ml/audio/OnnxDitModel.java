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
import org.almostrealism.ml.OnnxFeatures;

import java.util.HashMap;
import java.util.Map;

public class OnnxDitModel implements DitModel, OnnxFeatures {
	private final OrtEnvironment env;
	private final OrtSession session;

	public OnnxDitModel(OrtEnvironment env, OrtSession.SessionOptions options, String modelsPath) throws OrtException {
		this.env = env;
		this.session = env.createSession(modelsPath + "/dit.onnx", options);
	}

	public PackedCollection<?> forward(PackedCollection<?> x, PackedCollection<?> t,
									   PackedCollection<?> crossAttnCond,
									   PackedCollection<?> globalCond) {
		try {
			// Prepare DiT inputs
			Map<String, OnnxTensor> ditInputs = new HashMap<>();
			ditInputs.put("x", toOnnx(env, x));
			ditInputs.put("t", toOnnx(env, t));
			ditInputs.put("cross_attn_cond", toOnnx(env, crossAttnCond));
			ditInputs.put("global_cond", toOnnx(env, globalCond));

			// Run DiT model
			OrtSession.Result ditResult = session.run(ditInputs);
			OnnxTensor ditOutput = (OnnxTensor) ditResult.get(0);
			return pack(ditOutput);
		} catch (OrtException e) {
			throw new RuntimeException("Error running DiT model", e);
		}
	}

	@Override
	public void destroy() {
		try {
			session.close();
		} catch (OrtException e) {
			throw new RuntimeException(e);
		}
	}
}
