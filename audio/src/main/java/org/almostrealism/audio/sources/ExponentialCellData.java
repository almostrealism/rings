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

package org.almostrealism.audio.sources;

import io.almostrealism.relation.Provider;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.collect.TraversalPolicy;

public class ExponentialCellData extends PackedCollection<Scalar> {
	public ExponentialCellData() {
		super(new TraversalPolicy(5, 2), 1, delegateSpec ->
				new Scalar(delegateSpec.getDelegate(), delegateSpec.getOffset()));
	}

	protected Scalar notePosition() { return get(0); }
	protected Scalar noteLength() { return get(1); }
	protected Scalar inputScale() { return get(2); }
	protected Scalar outputScale() { return get(3); }
	protected Scalar depth() { return get(4); }

	public Provider<Scalar> getNotePosition() { return new Provider<>(notePosition()); }
	public void setNotePosition(double notePosition) { notePosition().setValue(notePosition); }

	public Provider<Scalar> getNoteLength() { return new Provider<>(noteLength()); }
	public void setNoteLength(double noteLength) { notePosition().setValue(noteLength); }

	public Provider<Scalar> getInputScale() { return new Provider<>(inputScale()); }
	public void setInputScale(double scale) { inputScale().setValue(scale); }

	public Provider<Scalar> getOutputScale() { return new Provider<>(outputScale()); }
	public void setOutputScale(double scale) { outputScale().setValue(scale); }

	public Provider<Scalar> getDepth() { return new Provider<>(depth()); }
	public void setDepth(double depth) { depth().setValue(depth); }
}
