/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.code.ExpressionFeatures;
import io.almostrealism.expression.Product;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.HardwareFeatures;
import org.almostrealism.hardware.PassThroughProducer;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;
import org.almostrealism.time.computations.Interpolate;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class WaveDataProviderAdapter implements WaveDataProvider,
								Comparable<WaveDataProvider>, CodeFeatures {
	private static Map<String, ContextSpecific<WaveData>> loaded;
	private static ContextSpecific<Evaluable<PackedCollection<?>>> interpolate;

	static {
		loaded = new HashMap<>();
		interpolate = new DefaultContextSpecific<>(() ->
				new Interpolate(
						new PassThroughProducer<>(1, 0),
						new PassThroughProducer<>(1, 1),
						new PassThroughProducer<>(1, 2),
						v -> Product.of(v, ExpressionFeatures.getInstance().e(1.0 / OutputLine.sampleRate)),
						v -> Product.of(v, ExpressionFeatures.getInstance().e(OutputLine.sampleRate))).get());
	}

	protected void clearKey(String key) {
		loaded.remove(key);
	}

	protected abstract WaveData load();

	protected void unload() { clearKey(getKey()); }

	@Override
	public double getDuration(double playbackRate) {
		return getDuration() / playbackRate;
	}

	@Override
	public WaveData get(double playbackRate) {
		WaveData original = get();
		if (playbackRate == 1.0) return original;

		if (original.getSampleRate() != OutputLine.sampleRate) {
			System.out.println("WARN: Cannot alter playback rate of audio which is not at " + OutputLine.sampleRate);
			return null;
		}

		PackedCollection<?> rate = PackedCollection.factory().apply(1);
		rate.setMem(0, playbackRate);

		PackedCollection<?> audio = original.getCollection();
		int len = (int) (audio.getMemLength() / playbackRate);
		PackedCollection<?> dest = new PackedCollection<>(len);

		// TODO  This can use CollectionFeatures::integers instead of taking a timeline argument
		PackedCollection<?> timeline = WaveOutput.timeline.getValue();

		interpolate.getValue().into(dest.traverse(1))
				.evaluate(audio.traverse(0),
						timeline.range(shape(dest.getMemLength())).traverseEach(),
						rate.traverse(0));
		return new WaveData(dest.traverse(1), original.getSampleRate());
	}

	@JsonIgnore
	@Override
	public WaveData get() {
		if (loaded.get(getKey()) == null) {
			loaded.put(getKey(), new DefaultContextSpecific<>(this::load));
			loaded.get(getKey()).init();
		}

		return loaded.get(getKey()).getValue();
	}
}
