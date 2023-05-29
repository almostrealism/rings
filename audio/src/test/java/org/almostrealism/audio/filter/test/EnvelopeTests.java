/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.filter.test;

import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.EnvelopeFeatures;
import org.almostrealism.audio.filter.EnvelopeSection;
import org.almostrealism.collect.PackedCollection;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class EnvelopeTests implements EnvelopeFeatures {
	@Test
	public void attack() throws IOException {
		WaveData.load(new File("Library/organ.wav"))
				.sample(attack(c(1.0)))
				.save(new File("results/attack-test.wav"));
	}

	@Test
	public void adsr() {
		double duration = 8.0;
		double attack = 0.5;
		double decay = 1.0;
		double sustain = 0.3;
		double release = 3.0;

		EnvelopeSection env = envelope(attack(c(attack)))
				.andThenDecay(c(attack), c(decay), c(sustain))
				.andThen(c(attack + decay), sustain(c(sustain)))
				.andThenRelease(c(duration), c(sustain), c(release), c(0.0));


		PackedCollection<?> data = new PackedCollection<>(10 * 44100);
		data = c(p(data.traverseEach())).add(c(1.0)).get().evaluate();

		new WaveData(data, 44100)
				.sample(env).save(new File("results/adsr-test.wav"));
	}
}
