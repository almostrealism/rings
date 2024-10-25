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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PatternNoteNode implements NoteAudioNode {
	private NoteAudioChoice choice;
	private PatternNote note;

	private String name;
	private List<NoteAudioNode> children;

	public PatternNoteNode() { }

	public PatternNoteNode(NoteAudioChoice choice, PatternNote note) {
		this.choice = choice;
		this.note = note;
		initChildren();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		if (name != null) return name;
		if (choice == null) return null;
		return choice.getName() + " Note";
	}

	protected void initChildren() {
		if (children != null) return;

		if (note == null) {
			children = new ArrayList<>();
			return;
		}

		NoteAudioContext audioContext = new NoteAudioContext(choice.getValidNotes(), null);
		children = note.getProviders(null, audioContext::selectAudio)
				.stream()
				.filter(Objects::nonNull)
				.distinct()
				.map(AudioProviderNode::new)
				.collect(Collectors.toList());
	}

	public void setChildren(List<NoteAudioNode> children) {
		this.children = children;
	}

	@Override
	public Collection<NoteAudioNode> getChildren() {
		return children;
	}
}
