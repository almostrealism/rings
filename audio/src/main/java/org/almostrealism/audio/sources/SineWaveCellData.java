/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.sources;

import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.BaseAudioData;

public interface SineWaveCellData extends BaseAudioData {
	default Scalar notePosition() { return get(3); }
	default Scalar noteLength() { return get(4); }
	default Scalar phase() { return get(5); }
	default Scalar depth() { return get(6); }

	default Producer<PackedCollection<?>> getNotePosition() {
		return cp(notePosition().range(shape(1)));
	}

	default void setNotePosition(double notePosition) {
		notePosition().setValue(notePosition);
	}

	default Producer<PackedCollection<?>> getNoteLength() {
		return cp(noteLength().range(shape(1)));
	}

	default void setNoteLength(double noteLength) {
		noteLength().setValue(noteLength);
	}

	default Producer<PackedCollection<?>> getPhase() {
		return cp(phase().range(shape(1)));
	}

	default void setPhase(double phase) {
		phase().setValue(phase);
	}

	default Producer<PackedCollection<?>> getDepth() {
		return p(depth().range(shape(1)));
	}

	default void setDepth(double depth) { depth().setValue(depth); }
}
