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

package org.almostrealism.audio.arrange;

import io.almostrealism.cycle.Setup;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

public class AutomationManager implements Setup, CellFeatures {
	private ConfigurableGenome genome;
	private TimeCell clock;
	private DoubleSupplier measureDuration;
	private int sampleRate;

	private PackedCollection<?> scale;
	private double r = 4.0;

	public AutomationManager(ConfigurableGenome genome, TimeCell clock,
							 DoubleSupplier measureDuration, int sampleRate) {
		this.genome = genome;
		this.clock = clock;
		this.measureDuration = measureDuration;
		this.sampleRate = sampleRate;
		this.scale = PackedCollection.factory().apply(1);
	}

	protected Producer<PackedCollection<?>> time() {
		return divide(clock.frame(), cp(scale));
	}

	public Producer<PackedCollection<?>> getAggregatedValue() {
		return multiply(getMainValue(), multiply(getShortPeriodValue(), getLongPeriodValue()));
	}

	public Producer<PackedCollection<?>> getMainValue() {
		return c(0.03 * r).multiply(time()).pow(c(3.8)).multiply(c(0.1));
	}

	public Producer<PackedCollection<?>> getLongPeriodValue() {
		return c(0.7).add(sin(multiply(time(), c(r)))).multiply(c(0.3));
	}

	public Producer<PackedCollection<?>> getShortPeriodValue() {
		return c(1.0).add(sin(multiply(time(), c(4.0 * r))).multiply(c(-0.08)));
	}

	@Override
	public Supplier<Runnable> setup() {
		OperationList setup = new OperationList("AutomationManager Setup");
		setup.add(() -> () -> {
			scale.set(0, measureDuration.getAsDouble() * sampleRate);
		});
		return setup;
	}
}
