/*
 * Copyright 2021 Michael Murray
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

package org.almostrealism.audio.data;

import org.almostrealism.graph.temporal.DefaultWaveCellData;
import org.almostrealism.hardware.MemoryData;
import org.almostrealism.hardware.mem.MemoryBankAdapter;
import io.almostrealism.relation.Evaluable;

/**
 * A collection of {@link DefaultWaveCellData}s of a fixed length, that is contiguous in
 * RAM and usable for kernel methods.
 *
 * @author  Michael Murray
 */
public class PolymorphicAudioDataBank extends MemoryBankAdapter<PolymorphicAudioData> {
	public PolymorphicAudioDataBank(int count) {
		super(DefaultWaveCellData.SIZE, count, delegateSpec ->
				new PolymorphicAudioData(delegateSpec.getDelegate(), delegateSpec.getOffset()));
	}

	// TODO  Need to respect CacheLevel, but the parent constructor that does
	//       respect cache level does this init stuff that we don't want
	public PolymorphicAudioDataBank(int count, MemoryData delegate, int delegateOffset, CacheLevel cacheLevel) {
		super(DefaultWaveCellData.SIZE, count, delegateSpec ->
						new PolymorphicAudioData(delegateSpec.getDelegate(), delegateSpec.getOffset()),
				delegate, delegateOffset);
	}

	public PolymorphicAudioDataBank range(int offset, int length) {
		if (offset * getAtomicMemLength() >= getMemLength()) {
			throw new IllegalArgumentException("Range extends beyond the length of this bank");
		}

		return new PolymorphicAudioDataBank(length, this, offset * getAtomicMemLength(), null);
	}

	public static PolymorphicAudioDataBank fromProducer(Evaluable<PolymorphicAudioData> producer, int count) {
		PolymorphicAudioDataBank bank = new PolymorphicAudioDataBank(count);
		for (int i = 0; i < bank.getCount(); i++) {
			bank.set(i, producer.evaluate());
		}

		return bank;
	}
}
