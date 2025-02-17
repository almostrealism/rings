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

package org.almostrealism.audio.health;

import io.almostrealism.lifecycle.Destroyable;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.TemporalCellular;
import org.almostrealism.optimize.HealthComputation;

import java.util.List;
import java.util.function.Consumer;

public interface AudioHealthComputation<T extends TemporalCellular>
		extends HealthComputation<T, AudioHealthScore>, Destroyable {
	Receptor<PackedCollection<?>> getOutput();

	List<? extends Receptor<PackedCollection<?>>> getStems();

	List<? extends Receptor<PackedCollection<?>>> getMeasures();

	void setWaveDetailsProcessor(Consumer<WaveDetails> processor);
}
