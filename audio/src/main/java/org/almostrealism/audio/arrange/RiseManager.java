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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.CellList;
import org.almostrealism.audio.sources.BufferDetails;
import org.almostrealism.audio.sources.StatelessSource;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;
import org.almostrealism.time.Frequency;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class RiseManager implements Setup, CellFeatures {
	public static final double riseDuration = 100;

	private OperationList setup;
	private StatelessSource generator;
	private ConfigurableGenome genome;

	private Supplier<Frequency> tempo;
	private DoubleSupplier measureDuration;
	private int sampleRate;

	public RiseManager(ConfigurableGenome genome, Supplier<Frequency> tempo,
					   DoubleSupplier measureDuration, int sampleRate) {
		this.setup = new OperationList("RiseManager Setup");
		this.genome = genome;
		this.tempo = tempo;
		this.measureDuration = measureDuration;
		this.sampleRate = sampleRate;
	}

	public ConfigurableGenome getGenome() {
		return genome;
	}

	@Override
	public Supplier<Runnable> setup() { return setup; }

	public CellList getRise() {
		// TODO
		this.generator.generate(new BufferDetails(sampleRate, riseDuration),
								c(0.0, 0.0, 0.0), time -> c(1.0));
		throw new UnsupportedOperationException();
	}
}
