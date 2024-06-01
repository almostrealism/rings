/*
 * Copyright 2024 Michael Murray
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
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.jni.NativeCompiler;
import org.almostrealism.hardware.metal.MetalProgram;
import org.almostrealism.time.computations.MultiOrderFilter;
import org.almostrealism.util.TestFeatures;
import org.almostrealism.util.TestSettings;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MultiOrderFilterTest implements TestFeatures {
	static {
		NativeCompiler.enableInstructionSetMonitoring = !TestSettings.skipLongTests;
		MetalProgram.enableProgramMonitoring = !TestSettings.skipLongTests;
	}

	@Test
	public void lowPass() throws IOException {
		WaveData data = WaveData.load(new File("Library/Snare Gold 1.wav"));

		MultiOrderFilter filter = MultiOrderFilter.createLowPass(
									cp(data.getCollection()), c(2000),
									data.getSampleRate(), 40);

		PackedCollection<?> result = filter.get().evaluate();
		WaveData output = new WaveData(result, data.getSampleRate());
		output.save(new File("results/multi-order-low-pass.wav"));
	}

	@Test
	public void highPass() throws IOException {
		WaveData data = WaveData.load(new File("Library/Snare Gold 1.wav"));

		MultiOrderFilter filter = MultiOrderFilter.createHighPass(
				cp(data.getCollection()), c(3000),
				data.getSampleRate(), 40);

		PackedCollection<?> result = filter.get().evaluate();
		WaveData output = new WaveData(result, data.getSampleRate());
		output.save(new File("results/multi-order-high-pass.wav"));
	}
}
