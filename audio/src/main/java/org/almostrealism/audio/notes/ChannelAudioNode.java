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

import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.pattern.NoteAudioChoice;
import org.almostrealism.audio.pattern.PatternElement;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChannelAudioNode implements NoteAudioNode {
	private AudioScene<?> scene;
	private int channel;

	private List<NoteAudioNode> children;

	public ChannelAudioNode(AudioScene<?> scene, int channel) {
		this.scene = scene;
    	this.channel = channel;
	}

	public int getChannel() {
		return channel;
	}

	@Override
	public String getName() {
		return scene.getChannelNames().get(getChannel());
	}

	public void setChoices(List<NoteAudioChoice> choices) {
		this.children = choices.stream()
				.map(AudioChoiceNode::new)
				.collect(Collectors.toUnmodifiableList());
	}

	public void setPatternElements(Map<NoteAudioChoice, List<PatternElement>> elements) {
		for (NoteAudioNode node : children) {
			AudioChoiceNode child = (AudioChoiceNode) node;
			child.setPatternElements(elements.get(child.getChoice()));
		}
	}

	public List<NoteAudioNode> getChildren() {
		return children;
	}
}