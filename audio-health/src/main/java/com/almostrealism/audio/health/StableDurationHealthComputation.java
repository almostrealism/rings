/*
 * Copyright 2021 Michael Murray
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

package com.almostrealism.audio.health;

import io.almostrealism.code.OperationAdapter;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.hardware.AcceleratedComputationOperation;
import org.almostrealism.hardware.HardwareException;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.hardware.ProducerCache;
import org.almostrealism.hardware.computations.Loop;
import org.almostrealism.organs.Organ;
import org.almostrealism.util.CodeFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The {@link StableDurationHealthComputation} is a {@link HealthComputationAdapter} which
 * computes a health score based on the duration that an {@link Organ} can be used before
 * a min or max clip value is reached.
 * 
 * @author  Michael Murray
 */
public class StableDurationHealthComputation extends SilenceDurationHealthComputation implements CellFeatures {
	public static boolean enableLoop = true;

	private long max = standardDuration;
	private int iter;

	private boolean encounteredSilence;
	
	public StableDurationHealthComputation() {
		super(6);
		addSilenceListener(() -> encounteredSilence = true);
		setBatchSize(enableLoop ? OutputLine.sampleRate : 1);
	}

	public void setBatchSize(int iter) {
		this.iter = iter;
	}
	
	public void setMaxDuration(long sec) { this.max = (int) (sec * OutputLine.sampleRate); }

	/**
	 * This setting impacts all health computations, even though it is not a static method.
	 */
	@Override
	public void setStandardDuration(int sec) {
		standardDuration = (int) (sec * OutputLine.sampleRate);
	}

	@Override
	public double computeHealth(Organ<Scalar> organ) {
		super.init();

		encounteredSilence = false;

//		TODO  Restore average amplitude computation
//		AverageAmplitude avg = new AverageAmplitude();
//		meter.addListener(avg);

		OrganRunner runner = null;
		Runnable start;
		Runnable iterate;

		try {
			runner = new OrganRunner(organ, iter);
			start = runner.get();
			iterate = runner.getContinue();

			long l;

			l:
			for (l = 0; l < max; l = l + iter) {
				try {
					(l == 0 ? start : iterate).run();
				} catch (HardwareException e) {
					System.out.println(e.getProgram());
					throw e;
				}

				checkForSilence(getMeter());

				if (getMeter().getClipCount() > 0) {
					System.out.print("C");
					if (enableVerbose) System.out.println();
				}

				if (encounteredSilence) {
					System.out.print("S");
					if (enableVerbose) System.out.println();
				}

				// If clipping or silence occurs, report the health score
				if (getMeter().getClipCount() > 0 || encounteredSilence) break l;

				if (enableVerbose && (l + iter) % (OutputLine.sampleRate / 10) == 0) {
					double v = l + iter;
					System.out.println("StableDurationHealthComputation: " + v / OutputLine.sampleRate + " seconds");
				} else if (!enableVerbose && (l + iter) % (OutputLine.sampleRate * 5) == 0) {
					System.out.print(">");
				}
			}

			// Report the health score as a combination of
			// the percentage of the expected duration
			// elapsed and the time it takes to reach the
			// average value
//			return ((double) l) / standardDuration -
//					((double) avg.framesUntilAverage()) / standardDuration;
			return (double) l / standardDuration;
		} finally {
//			meter.removeListener(avg);
			((WaveOutput) getMeter().getForwarding()).write().get().run();
			((WaveOutput) getMeter().getForwarding()).reset();
			getMeter().reset();
			runner.destroy();

			ProducerCache.destroyEvaluableCache();
		}
	}

	private class AverageAmplitude implements Consumer<Scalar> {
		private List<Scalar> values = new ArrayList<>();

		@Override
		public void accept(Scalar s) {
			values.add(s);
		}

		public double average() {
			return values.stream().mapToDouble(Scalar::getValue).map(Math::abs).average().orElse(0.0);
		}

		public int framesUntilAverage() {
			double avg = average();
			for (int i = 0; i < values.size(); i++) {
				if (Math.abs(values.get(i).getValue()) >= avg) return i;
			}

			return -1; // The mean value theorem states this should never happen
		}
	}
}
