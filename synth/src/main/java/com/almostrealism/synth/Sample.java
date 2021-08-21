/*
 * Copyright 2020 Michael Murray
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

package com.almostrealism.synth;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.graph.Source;
import io.almostrealism.relation.Producer;
import org.almostrealism.hardware.DynamicProducerForMemoryData;

// TODO  Rename
// TODO  Extend AudioSample
// TODO  Implement Resource
// TODO  Could JavaAudioSample be a subclass?
public class Sample implements Source<Scalar> {
	private int i;
	private ScalarBank data;
	
	public Sample(byte data[]) {
		this.data = new ScalarBank(data.length);
		
		for (int i = 0; i < data.length; i++) {
			this.data.get(i).setValue(((double) data[i]) / Byte.MAX_VALUE);
		}
	}
	
	public Sample(ScalarBank data) {
		this.data = data;
	}

	@Override
	public Producer<Scalar> next() {
		return new DynamicProducerForMemoryData<>(args -> isDone() ? new Scalar(0) : data.get(i++));
	}

	@Override
	public boolean isDone() { return i >= data.getCount(); };
}
