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

package org.almostrealism.audio.synth;

import org.almostrealism.time.Frequency;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OvertoneSeries implements Iterable<Frequency> {
	private int subCount, superCount;
	private Frequency root;

	public OvertoneSeries(int subCount, int superCount) {
		this.subCount = subCount;
		this.superCount = superCount;
		setRoot(new Frequency(1.0));
	}

	public void setRoot(Frequency f) { this.root = f; }

	@Override
	public Iterator<Frequency> iterator() {
		List<Frequency> l = new ArrayList<>();

		for (int i = subCount; i > 0; i--) {
			l.add(new Frequency(root.asHertz() / Math.pow(2, i)));
		}

		l.add(new Frequency(root.asHertz()));

		for (int i = 1; i <= superCount; i++) {
			l.add(new Frequency(root.asHertz() * Math.pow(2, i)));
		}

		return l.iterator();
	}

	public int total() { return subCount + superCount + 1; }
}
