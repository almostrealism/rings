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

package com.almostrealism.replicator;

import org.almostrealism.space.SurfaceGroup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LayeredReplicant<T extends Replicant> extends SurfaceGroup<T> {
	private final List<T> layers;

	public LayeredReplicant() {
		layers = new ArrayList<>();
	}

	public void addReplicant(T r) {
		if (layers.size() > 0) {
			layers.get(layers.size() - 1).addSurface(r);
		}

		layers.add(r);
	}

	public T getReplicant(int index) { return layers.get(index); }

	public int length() { return layers.size(); }

	@Override
	public Iterator<T> iterator() { return layers.get(0).iterator(); }
}
