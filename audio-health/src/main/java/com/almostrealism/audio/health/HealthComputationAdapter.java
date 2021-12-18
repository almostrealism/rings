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

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.AudioMeter;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.TemporalCellular;

public abstract class HealthComputationAdapter implements AudioHealthComputation<TemporalCellular> {
	public static final int MEASURE_COUNT = 2;
	public static int standardDuration = (int) (75 * OutputLine.sampleRate);

	private TemporalCellular target;

	private AudioMeter outputMeter;
	private WaveOutput out;
	private Supplier<String> outputFileSupplier;
	private File outputFile;

	private List<AudioMeter> measures;

	public TemporalCellular getTarget() { return target; }

	@Override
	public void setTarget(TemporalCellular target) { this.target = target; }

	@Override
	public synchronized Receptor<Scalar> getOutput() {
		if (outputMeter != null) return outputMeter;

		outputMeter = new AudioMeter();
		out = new WaveOutput(() ->
				Optional.ofNullable(outputFileSupplier).map(s -> {
					outputFile = new File(s.get());
					return outputFile;
				}).orElse(null),
				(int) (1.5 * standardDuration), 24);
		outputMeter.setForwarding(out);
		return outputMeter;
	}

	@Override
	public List<AudioMeter> getMeasures() {
		if (measures != null) return measures;
		measures = IntStream.range(0, MEASURE_COUNT).mapToObj(i -> new AudioMeter()).collect(Collectors.toList());
		configureMeasures(measures);
		return measures;
	}

	protected void configureMeasures(List<AudioMeter> measures) { }

	public void setOutputFile(String file) { setOutputFile(() -> file); }
	public void setOutputFile(Supplier<String> file) { this.outputFileSupplier = file; }

	protected File getOutputFile() { return outputFile; }

	@Override
	public void reset() {
		AudioHealthComputation.super.reset();
		outputMeter.reset();
		measures.forEach(AudioMeter::reset);
		out.reset();
	}
}
