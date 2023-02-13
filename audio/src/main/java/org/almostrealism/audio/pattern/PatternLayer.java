/*
 * Copyright 2022 Michael Murray
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

package org.almostrealism.audio.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PatternLayer {
	private PatternFactoryChoice choice;
	private List<PatternElement> elements;
	private PatternLayer child;

	public PatternLayer() { this(null, new ArrayList<>()); }

	public PatternLayer(List<PatternElement> elements) {
		this(null, elements);
	}

	public PatternLayer(PatternFactoryChoice choice, List<PatternElement> elements) {
		this.choice = choice;
		this.elements = elements;
	}

	public PatternFactoryChoice getChoice() {
		return choice;
	}

	public void setChoice(PatternFactoryChoice node) {
		this.choice = node;
	}

	public List<PatternElement> getElements() {
		return elements;
	}

	public void setElements(List<PatternElement> elements) {
		this.elements = elements;
	}

	public List<PatternElement> getAllElements(double start, double end) {
		List<PatternElement> result = new ArrayList<>(elements.stream()
				.filter(e -> e.getPosition() >= start && e.getPosition() < end)
				.collect(Collectors.toList()));
		if (child != null) result.addAll(child.getAllElements(start, end));
		return result;
	}

	public PatternLayer getChild() { return child; }

	public void setChild(PatternLayer child) { this.child = child; }

	public PatternLayer getTail() {
		if (child == null) return this;
		return child.getTail();
	}

	public PatternLayer getLastParent() {
		if (child == null) return null;
		if (child.getChild() == null) return this;
		return child.getLastParent();
	}

	public int depth() {
		if (child == null) return 1;
		return child.depth() + 1;
	}

	public void trim(double duration) {
		trim(0.0, duration);
	}

	public void trim(double start, double end) {
		Iterator<PatternElement> itr = elements.iterator();
		while (itr.hasNext()) {
			PatternElement e = itr.next();
			if (e.getPosition() < start || e.getPosition() >= end) itr.remove();
		}
	}
}
