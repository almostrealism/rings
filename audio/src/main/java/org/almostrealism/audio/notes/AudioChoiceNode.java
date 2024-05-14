/*
 * Copyright 2024 Michael Murray
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

package org.almostrealism.audio.notes;

import org.almostrealism.audio.pattern.NoteAudioChoice;
import org.almostrealism.audio.pattern.PatternElement;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AudioChoiceNode implements NoteAudioNode {
	private NoteAudioChoice choice;

	private List<PatternElement> patternElements;
	private List<NoteAudioNode> children;

	public AudioChoiceNode(NoteAudioChoice choice) {
		this.choice = choice;
	}

	@Override
	public String getName() {
		return choice.getName();
	}

	public List<PatternElement> getPatternElements() {
		return patternElements;
	}

	public void setPatternElements(List<PatternElement> patternElements) {
		this.patternElements = patternElements;
		children = null;
	}

	protected void initChildren() {
		if (children != null) return;

		if (getPatternElements() == null) {
			children = Collections.emptyList();
			return;
		}

		NoteAudioContext audioContext = new NoteAudioContext(choice.getValidNotes(), null);
		children = getPatternElements()
				.stream()
				.map(PatternElement::getNote)
				.mapToDouble(PatternNote::getNoteAudioSelection)
				.mapToObj(audioContext::selectAudio)
				.map(AudioProviderNode::new)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public Collection<NoteAudioNode> getChildren() {
		initChildren();
		return children;
	}
}
