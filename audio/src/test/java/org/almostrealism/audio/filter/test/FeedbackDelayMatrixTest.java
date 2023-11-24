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

package org.almostrealism.audio.filter.test;

import io.almostrealism.kernel.KernelPreferences;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.filter.DelayNetwork;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class FeedbackDelayMatrixTest extends AudioPassFilterTest {
	@Test
	public void reverb() throws IOException {
		KernelPreferences.optimizeForMetal();

		WavFile f = WavFile.openWavFile(new File("Library/Snare Perc DD.wav"));
		DelayNetwork verb = new DelayNetwork(0.001, 512, 1.5, (int) f.getSampleRate(), true);
		runFilter("reverb", f, verb, true, (int) (f.getSampleRate() * 6));
	}
}
