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

package org.almostrealism.ml;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.TensorInfo;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

import java.nio.FloatBuffer;
import java.util.Objects;

public interface OnnxFeatures extends CodeFeatures {
	default TraversalPolicy shape(TensorInfo info) {
		return new TraversalPolicy(info.getShape());
	}

	/**
	 * Converts an {@link OnnxTensor} to a {@link PackedCollection}.
	 */
	default PackedCollection<?> pack(OnnxTensor tensor) throws OrtException {
		FloatBuffer buffer = tensor.getFloatBuffer();
		float[] data = new float[buffer.capacity()];
		buffer.get(data);

		PackedCollection<?> result = new PackedCollection<>(shape(tensor.getInfo()));
		result.setMem(0, data);
		return result;
	}

	/**
	 * Converts a {@link PackedCollection} to an {@link OnnxTensor}.
	 *
	 * @param env The OrtEnvironment to use for creating the tensor.
	 * @param collection The {@link PackedCollection} to convert.
	 * @return An {@link OnnxTensor} representing the {@link PackedCollection}.
	 * @throws OrtException If there is an error creating the tensor.
	 */
	default OnnxTensor toOnnx(OrtEnvironment env, PackedCollection<?> collection) throws OrtException {
		return OnnxTensor.createTensor(env,
				FloatBuffer.wrap(Objects.requireNonNull(collection).toFloatArray()),
				collection.getShape().extentLong());
	}
}
