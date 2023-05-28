/*
 * Copyright 2023 Michael Murray
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

import io.almostrealism.relation.Producer;
import org.almostrealism.collect.CollectionFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.heredity.Factor;

import java.util.function.Supplier;

public class EnvelopeSection implements Supplier<Factor<PackedCollection<?>>>, CollectionFeatures {
	private Supplier<Producer<PackedCollection<?>>> time;
	private Producer<PackedCollection<?>> start;
	private Supplier<Factor<PackedCollection<?>>> lastEnvelope;
	private Factor<PackedCollection<?>> envelope;

	public EnvelopeSection(Supplier<Producer<PackedCollection<?>>> time,
						   Factor<PackedCollection<?>> envelope) {
		this(time, null, null, envelope);
	}

	public EnvelopeSection(Supplier<Producer<PackedCollection<?>>> time,
						   Producer<PackedCollection<?>> start,
						   Supplier<Factor<PackedCollection<?>>> lastEnvelope,
						   Factor<PackedCollection<?>> envelope) {
		this.time = time;
		this.start = start;
		this.lastEnvelope = lastEnvelope;
		this.envelope = envelope;
	}

	public EnvelopeSection andThen(Producer<PackedCollection<?>> start, Factor<PackedCollection<?>> envelope) {
		return new EnvelopeSection(time, start, this, envelope);
	}

	@Override
	public Factor<PackedCollection<?>> get() {
		if (lastEnvelope == null) {
			return envelope;
		} else {
			return in -> greaterThanConditional(time.get(), start,
					envelope.getResultant(in),
					lastEnvelope.get().getResultant(in));
		}
	}
}
