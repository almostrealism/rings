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

package org.almostrealism.audio.health;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.time.Temporal;
import org.almostrealism.time.TemporalRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The {@link StableDurationHealthComputation} is a {@link HealthComputationAdapter} which
 * computes a health score based on the duration that a {@link Temporal} can be used before
 * a min or max clip value is reached.
 * 
 * @author  Michael Murray
 */
public class StableDurationHealthComputation extends SilenceDurationHealthComputation implements CellFeatures {
	public static boolean enableOutput = true;
	public static boolean enableLoop = true;
	public static boolean enableTimeout = true;
	private static long timeout = 40 * 60 * 1000l;
	private static long timeoutInterval = 5000;

	private long max = standardDuration;
	private int iter;

	private boolean encounteredSilence;

	private TemporalRunner runner;

	private Thread timeoutTrigger;
	private boolean endTimeoutTrigger;
	private long startTime;
	private Scalar abortFlag;
	
	public StableDurationHealthComputation(int channels) {
		super(channels, 6);
		addSilenceListener(() -> encounteredSilence = true);
		setBatchSize(enableLoop ? OutputLine.sampleRate / 2 : 1);
	}

	public void setBatchSize(int iter) {
		this.iter = iter;
	}
	
	public void setMaxDuration(long sec) { this.max = (int) (sec * OutputLine.sampleRate); }

	/**
	 * This setting impacts all health computations, even though it is not a static method.
	 */
	public static void setStandardDuration(int sec) {
		standardDuration = (int) (sec * OutputLine.sampleRate);
	}

	public void setTarget(TemporalCellular target) {
		if (getTarget() == null) {
			super.setTarget(target);
			this.abortFlag = new Scalar(0.0);
			this.runner = new TemporalRunner(target, iter);
		} else if (getTarget() != target) {
			throw new IllegalArgumentException("Health computation cannot be reused");
		}
	}

	protected void startTimeoutTrigger() {
		if (timeoutTrigger != null) {
			try {
				Thread.sleep(timeoutInterval + 100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (timeoutTrigger != null) {
			throw new IllegalArgumentException();
		}

		timeoutTrigger = new Thread(() -> {
			w: while (true) {
				try {
					Thread.sleep(timeoutInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
					endTimeoutTrigger = true;
				}

				if (!endTimeoutTrigger && isTimeout()) {
					if (enableVerbose) System.out.println("Trigger timeout");

					abortFlag.setValue(1.0);

					if (enableVerbose) {
						System.out.println("Timeout flag set");
					} else {
						System.out.print("T");
					}

					endTimeoutTrigger = true;
				}

				if (endTimeoutTrigger) {
					timeoutTrigger = null;
					break w;
				}
			}
		});

		endTimeoutTrigger = false;
		timeoutTrigger.start();
	}

	protected void endTimeoutTrigger() {
		endTimeoutTrigger = true;
		abortFlag.setValue(0.0);
	}

	protected boolean isTimeout() {
		return enableTimeout && System.currentTimeMillis() - startTime > timeout;
	}

	@Override
	public AudioHealthScore computeHealth() {
		encounteredSilence = false;
		OperationList.setAbortFlag(abortFlag);

//		TODO  Restore average amplitude computation
//		AverageAmplitude avg = new AverageAmplitude();

		double score = 0.0;
		double errorMultiplier = 1.0;

		Runnable start;
		Runnable iterate;

		try {
			start = runner.get();
			iterate = runner.getContinue();

			startTime = System.currentTimeMillis();
			if (enableTimeout) startTimeoutTrigger();

			long l;

			l:
			for (l = 0; l < max && !isTimeout(); l = l + iter) {
				(l == 0 ? start : iterate).run();

				if ((int) getWaveOut().getCursor().getCursor() != l + iter) {
					if (enableVerbose) {
						System.out.println("StableDurationHealthComputation: Cursor out of sync (" +
								(int) getWaveOut().getCursor().getCursor() + " != " + (l + iter) + ")");
						System.exit(1);
					} else {
						System.out.print("N");
					}

					// TODO  This should just throw an exception: working around it should no longer be necessary
					errorMultiplier *= 0.55;
					break l;
				}

				getMeasures().forEach(m -> {
					checkForSilence(m);

					if (m.getClipCount() > 0) {
						System.out.print("C");
						if (enableVerbose) System.out.println();
					}

					if (encounteredSilence) {
						System.out.print("S");
						if (enableVerbose) System.out.println();
					}
				});

				// If clipping or silence occurs, report the health score
				if (getMeasures().stream().anyMatch(m -> m.getClipCount() > 0) || encounteredSilence) break l;

				if (enableVerbose && (l + iter) % (OutputLine.sampleRate / 10) == 0) {
					double v = l + iter;
					System.out.println("StableDurationHealthComputation: " + v / OutputLine.sampleRate + " seconds");
				} else if (!enableVerbose && (l + iter) % (OutputLine.sampleRate * 20) == 0) {
					System.out.print(">");
				}
			}

			if (isTimeout())
				errorMultiplier *= 0.75;

			// Report the health score as a combination of
			// the percentage of the expected duration
			// elapsed and the time it takes to reach the
			// average value
//			return ((double) l) / standardDuration -
//					((double) avg.framesUntilAverage()) / standardDuration;
			score = (double) (l + iter) * errorMultiplier / (standardDuration + iter);

			if (enableVerbose)
				System.out.println("\nStableDurationHealthComputation: Score computed after " + (System.currentTimeMillis() - startTime) + " msec");
		} finally {
			endTimeoutTrigger();

			if (enableOutput && score > 0) {
				if (enableVerbose)
					System.out.println("StableDurationHealthComputation: Cursor = " + getWaveOut().getCursor().getCursor());

				getWaveOut().write().get().run();
				if (getStems() != null) getStems().forEach(s -> s.write().get().run());
			}

			getWaveOut().reset();
			if (getStems() != null) getStems().forEach(WaveOutput::reset);
 			reset();
		}

		return new AudioHealthScore(score,
				Optional.ofNullable(getOutputFile()).map(File::getPath).orElse(null),
				Optional.ofNullable(getStemFiles()).map(l -> l.stream().map(File::getPath).sorted().collect(Collectors.toList())).orElse(null));
	}

	@Override
	public void reset() {
		abortFlag.setValue(0.0);
		super.reset();
		getTarget().reset();
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
