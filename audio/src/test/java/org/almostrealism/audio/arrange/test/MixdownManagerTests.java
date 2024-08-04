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

package org.almostrealism.audio.arrange.test;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.arrange.GlobalTimeManager;
import org.almostrealism.audio.arrange.MixdownManager;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.time.Frequency;
import org.almostrealism.time.TemporalRunner;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MixdownManagerTests implements CellFeatures {
	private double duration = 180;
	private int sampleRate = OutputLine.sampleRate;

	protected void run(String name, GlobalTimeManager time, MixdownManager mixdown, CellList cells) {
		OperationList setup = new OperationList("AudioScene Setup");
		setup.add(mixdown.setup());
		setup.add(time.setup());

		WaveOutput stemOut = new WaveOutput(new File("results/" + name + "-stem.wav"));
		WaveOutput efxOut = new WaveOutput(new File("results/" + name + "-efx.wav"));
		WaveOutput mixOut = new WaveOutput(new File("results/" + name + "-mix.wav"));

		cells = mixdown.cells(cells, List.of(stemOut, efxOut), mixOut);
		cells.addRequirement(time::tick);

		TemporalRunner runner = new TemporalRunner(cells, (int) (duration * sampleRate));
		runner.get().run();
		stemOut.write().get().run();
		efxOut.write().get().run();
		mixOut.write().get().run();
	}

	@Test
	public void mixdown1() throws IOException {
		double measureDuration = Frequency.forBPM(120).l(4);

		GlobalTimeManager time = new GlobalTimeManager(
				measure -> (int) (measure * measureDuration * sampleRate));

		ConfigurableGenome genome = new ConfigurableGenome();
		MixdownManager mixdown = new MixdownManager(genome, 1, 3,
										time.getClock(), sampleRate);

		genome.assignTo(genome.getParameters().random());

		CellList cells = w(c(0.0), c(1.0),
				WaveData.load(new File("Library/Snare Gold 1.wav")));
		run("mixdown1", time, mixdown, cells);
	}
}
