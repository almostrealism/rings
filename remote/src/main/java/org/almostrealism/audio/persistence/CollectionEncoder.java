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

package org.almostrealism.audio.persistence;

import org.almostrealism.audio.api.Audio;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.collect.PackedCollection;

import java.util.stream.IntStream;

public class CollectionEncoder {
	public static Audio.CollectionData encode(PackedCollection<?> c) {
		if (c == null) return Audio.CollectionData.getDefaultInstance();

		Audio.CollectionData.Builder data = Audio.CollectionData.newBuilder();
		data.setTraversalPolicy(encode(c.getShape()));
		c.doubleStream().forEach(data::addData);
		return data.build();
	}

	public static Audio.TraversalPolicyData encode(TraversalPolicy shape) {
		Audio.TraversalPolicyData.Builder data = Audio.TraversalPolicyData.newBuilder();
		IntStream.range(0, shape.getDimensions()).forEach(i -> data.addDims(shape.length(i)));
		data.setTraversalAxis(shape.getTraversalAxis());
		return data.build();
	}

	public static PackedCollection<?> decode(Audio.CollectionData data) {
		TraversalPolicy shape = decode(data.getTraversalPolicy());
		if (shape.getDimensions() == 0) return null;

		return decode(data, new PackedCollection<>(shape.getTotalSize()));
	}

	public static PackedCollection<?> decode(Audio.CollectionData data,
										 	 PackedCollection<?> destination) {
		return decode(data, destination, 0);
	}

	public static PackedCollection<?> decode(Audio.CollectionData data,
											 PackedCollection<?> destination,
											 int destinationOffset) {
		TraversalPolicy shape = decode(data.getTraversalPolicy());
		if (shape.getDimensions() == 0) return null;

		destination.setMem(destinationOffset,
				data.getDataList().stream().mapToDouble(d -> d).toArray());
		return destination.range(shape, destinationOffset);
	}

	public static TraversalPolicy decode(Audio.TraversalPolicyData data) {
		return new TraversalPolicy(true,
				data.getDimsList().stream().mapToInt(i -> i).toArray())
					.traverse(data.getTraversalAxis());
	}
}
