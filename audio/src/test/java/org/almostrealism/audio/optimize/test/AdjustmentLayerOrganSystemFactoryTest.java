/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.optimize.test;

import org.almostrealism.time.TemporalRunner;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.Cells;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.ReceptorCell;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

public class AdjustmentLayerOrganSystemFactoryTest extends AudioSceneOptimizationTest {

	@Test
	public void compare() {
		dc(() -> {
			ReceptorCell outa = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-comp-a.wav")).get(0);
			Cells organa = cells(pattern(2, 2), Arrays.asList(a(p(new Scalar())), a(p(new Scalar()))), outa);
			organa.reset();

			ReceptorCell outb = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-comp-b.wav")).get(0);
			Cells organb = cells(pattern(2, 2), null, outb); // TODO
			organb.reset();

			Runnable organRunA = new TemporalRunner(organa, 8 * OutputLine.sampleRate).get();
			Runnable organRunB = new TemporalRunner(organb, 8 * OutputLine.sampleRate).get();

			organRunA.run();
			((WaveOutput) outa.getReceptor()).write().get().run();

			organRunB.run();
			((WaveOutput) outb.getReceptor()).write().get().run();
		});
	}

	@Test
	public void layered() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();

		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}

	@Test
	public void layeredRandom() {
		ReceptorCell out = (ReceptorCell) o(1, i -> new File("results/layered-organ-factory-rand-test.wav")).get(0);
		Cells organ = cells(pattern(2, 2), null, out); // TODO
		organ.reset();

		Runnable organRun = new TemporalRunner(organ, 8 * OutputLine.sampleRate).get();
		organRun.run();
		((WaveOutput) out.getReceptor()).write().get().run();
	}
}
