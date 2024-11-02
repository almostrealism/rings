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

package org.almostrealism.audio.filter;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Factor;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.ChannelInfo;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.NoteAudioFilter;
import org.almostrealism.collect.CollectionProducer;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.computations.DynamicCollectionProducer;

import java.util.List;

public class ParameterizedLayerEnvelope implements ParameterizedEnvelope {
	public static boolean enableReusableProducer = false;

	public static final int MAX_SECONDS = 180;

	private static Evaluable<PackedCollection<?>> env;

	static {
		EnvelopeFeatures o = EnvelopeFeatures.getInstance();

		CollectionProducer<PackedCollection<?>> mainDuration = o.cv(o.shape(1), 1);
		CollectionProducer<PackedCollection<?>> duration0 = mainDuration.multiply(o.cv(o.shape(1), 2));
		CollectionProducer<PackedCollection<?>> duration1 = mainDuration.multiply(o.cv(o.shape(1), 3));
		CollectionProducer<PackedCollection<?>> duration2 = mainDuration.multiply(o.cv(o.shape(1), 4));
		CollectionProducer<PackedCollection<?>> volume0 = o.cv(o.shape(1), 5);
		CollectionProducer<PackedCollection<?>> volume1 = o.cv(o.shape(1), 6);
		CollectionProducer<PackedCollection<?>> volume2 = o.cv(o.shape(1), 7);
		CollectionProducer<PackedCollection<?>> volume3 = o.cv(o.shape(1), 8);

		Factor<PackedCollection<?>> factor =
				o.envelope(o.linear(o.c(0.0), duration0, volume0, volume1))
						.andThenRelease(duration0, volume1, duration1.subtract(duration0), volume2)
						.andThenRelease(duration1, volume2, duration2.subtract(duration1), volume3).get();
		env = o.sampling(OutputLine.sampleRate, MAX_SECONDS,
				() -> factor.getResultant(o.v(1, 0))).get();
	}

	private ParameterizedEnvelopeLayers parent;
	private int layer;

	public ParameterizedLayerEnvelope(ParameterizedEnvelopeLayers parent, int layer) {
		this.parent = parent;
		this.layer = layer;
	}

	@Override
	public NoteAudioFilter createFilter(ParameterSet params, ChannelInfo.Voicing voicing) {
		return new Filter(params, voicing);
	}

	public class Filter implements NoteAudioFilter {
		private ParameterSet params;
		private ChannelInfo.Voicing voicing;

		public Filter(ParameterSet params, ChannelInfo.Voicing voicing) {
			this.params = params;
			this.voicing = voicing;
		}

		public double getAttack() {
			return parent.getAttack(layer, params);
		}

		public double getSustain() {
			return parent.getSustain(layer, params);
		}

		public double getRelease() {
			return parent.getRelease(layer, params);
		}

		public double getVolume0() {
			return parent.getVolume(layer, 0, params);
		}

		public double getVolume1() {
			return parent.getVolume(layer, 1, params);
		}

		public double getVolume2() {
			return parent.getVolume(layer, 2, params);
		}

		public double getVolume3() {
			return parent.getVolume(layer, 3, params);
		}

		@Override
		public Producer<PackedCollection<?>> apply(Producer<PackedCollection<?>> audio,
												   Producer<PackedCollection<?>> duration,
												   Producer<PackedCollection<?>> automationLevel) {
			PackedCollection<?> d0 = new PackedCollection<>(1);
			d0.set(0, getAttack());

			PackedCollection<?> d1 = new PackedCollection<>(1);
			d1.set(0, getSustain());

			PackedCollection<?> d2 = new PackedCollection<>(1);
			d2.set(0, getRelease());

			PackedCollection<?> v0 = new PackedCollection<>(1);
			v0.set(0, getVolume0());

			PackedCollection<?> v1 = new PackedCollection<>(1);
			v1.set(0, getVolume1());

			PackedCollection<?> v2 = new PackedCollection<>(1);
			v2.set(0, getVolume2());

			PackedCollection<?> v3 = new PackedCollection<>(1);
			v3.set(0, getVolume3());

			if (enableReusableProducer) {
				return instruct("ParameterizedLayerEnvelope.filter", p -> {
					CollectionProducer<PackedCollection<?>> mainDuration = c(p[1]);
					CollectionProducer<PackedCollection<?>> duration0 = mainDuration.multiply(c(p[2]));
					CollectionProducer<PackedCollection<?>> duration1 = mainDuration.multiply(c(p[3]));
					CollectionProducer<PackedCollection<?>> duration2 = mainDuration.multiply(c(p[4]));
					CollectionProducer<PackedCollection<?>> volume0 = c(p[5]);
					CollectionProducer<PackedCollection<?>> volume1 = c(p[6]);
					CollectionProducer<PackedCollection<?>> volume2 = c(p[7]);
					CollectionProducer<PackedCollection<?>> volume3 = c(p[8]);

					Factor<PackedCollection<?>> factor =
							envelope(linear(c(0.0), duration0, volume0, volume1))
									.andThenRelease(duration0, volume1, duration1.subtract(duration0), volume2)
									.andThenRelease(duration1, volume2, duration2.subtract(duration1), volume3).get();
					return sampling(OutputLine.sampleRate, MAX_SECONDS,
							() -> factor.getResultant(p[0]));
				}, audio, duration, p(d0), p(d1), p(d2), p(v0), p(v1), p(v2), p(v3));
			} else {
				return new DynamicCollectionProducer<>(shape(audio), args -> {
					PackedCollection<?> audioData = audio.get().evaluate();
					PackedCollection<?> dr = duration.get().evaluate();

					PackedCollection<?> out = env.evaluate(audioData.traverse(1), dr, d0, d1, d2, v0, v1, v2, v3);

					if (out.getShape().getTotalSize() == 1) {
						warn("Envelope produced a value with shape " +
								out.getShape().toStringDetail());
					}

					return out;
				}, false);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			ParameterizedLayerEnvelope.Filter filter = (ParameterizedLayerEnvelope.Filter) obj;

			if (filter.getAttack() != getAttack()) return false;
			if (filter.getSustain() != getSustain()) return false;
			if (filter.getRelease() != getRelease()) return false;
			if (filter.getVolume0() != getVolume0()) return false;
			if (filter.getVolume1() != getVolume1()) return false;
			if (filter.getVolume2() != getVolume2()) return false;
			if (filter.getVolume3() != getVolume3()) return false;
			return true;
		}

		@Override
		public int hashCode() {
			return List.of(getAttack(), getSustain(), getRelease()).hashCode();
		}
	}
}
