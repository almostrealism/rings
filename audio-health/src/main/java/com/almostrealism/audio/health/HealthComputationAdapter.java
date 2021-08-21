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
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.AudioMeter;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.filter.AudioCellAdapter;
import org.almostrealism.audio.sources.SineWaveCell;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.computations.DefaultEnvelopeComputation;
import org.almostrealism.audio.sources.WavCell;
import org.almostrealism.graph.Receptor;
import org.almostrealism.optimize.HealthComputation;

public abstract class HealthComputationAdapter implements AudioHealthComputation {
	public static boolean enableGenerator = false;
	public static int standardDuration = (int) (60 * OutputLine.sampleRate);

	private AudioMeter meter;
	private WaveOutput out;
	private Supplier<String> debugFile;
	
	protected void init() { }

	@Override
	public Receptor<Scalar> getMonitor() { return getMeter(); }
	
	protected AudioMeter getMeter() {
		if (meter != null) return meter;

		meter = new AudioMeter();
		out = new WaveOutput(() ->
				Optional.ofNullable(debugFile).map(Supplier::get).map(File::new).orElse(null),
				standardDuration, 24);
		meter.setForwarding(out);
		return meter;
	}

	public void setDebugOutputFile(String file) { setDebugOutputFile(() -> file); }
	public void setDebugOutputFile(Supplier<String> file) { this.debugFile = file; }

	@Override
	public void reset() {
		AudioHealthComputation.super.reset();
		meter.reset();
		out.reset();
	}
}
