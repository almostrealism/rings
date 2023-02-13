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

package org.almostrealism.audio.sources;

import io.almostrealism.relation.Provider;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.graph.temporal.BaseAudioData;

public interface SineWaveCellData extends BaseAudioData {
	default Scalar notePosition() { return get(3); }
	default Scalar noteLength() { return get(4); }
	default Scalar phase() { return get(5); }
	default Scalar depth() { return get(6); }

	default Provider<Scalar> getNotePosition() { return new Provider<>(notePosition()); }
	default void setNotePosition(double notePosition) { notePosition().setValue(notePosition); }

	default Provider<Scalar> getNoteLength() { return new Provider<>(noteLength()); }
	default void setNoteLength(double noteLength) { noteLength().setValue(noteLength); }

	default Provider<Scalar> getPhase() { return new Provider<>(phase()); }
	default void setPhase(double phase) { phase().setValue(phase); }

	default Provider<Scalar> getDepth() { return new Provider<>(depth()); }
	default void setDepth(double depth) { depth().setValue(depth); }
}
