/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.arrange;

import org.almostrealism.audio.tone.Scale;
import org.almostrealism.collect.PackedCollection;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

public class AudioSceneContext {
	private int measures;
	private int frames;
	private List<Integer> channels;

	private DoubleToIntFunction frameForPosition;
	private DoubleUnaryOperator timeForDuration;
	private DoubleFunction<Scale<?>> scaleForPosition;
	private double activityBias;

	private PackedCollection<?> destination;

	private List<ChannelSection> sections;

	public int getMeasures() {
		return measures;
	}

	public void setMeasures(int measures) {
		this.measures = measures;
	}

	public int getFrames() {
		return frames;
	}

	public void setFrames(int frames) {
		this.frames = frames;
	}

	public List<Integer> getChannels() {
		return channels;
	}

	public void setChannels(List<Integer> channels) {
		this.channels = channels;
	}

	public DoubleToIntFunction getFrameForPosition() {
		return frameForPosition;
	}

	public void setFrameForPosition(DoubleToIntFunction frameForPosition) {
		this.frameForPosition = frameForPosition;
	}

	public DoubleUnaryOperator getTimeForDuration() {
		return timeForDuration;
	}

	public void setTimeForDuration(DoubleUnaryOperator timeForDuration) {
		this.timeForDuration = timeForDuration;
	}

	public DoubleFunction<Scale<?>> getScaleForPosition() {
		return scaleForPosition;
	}

	public void setScaleForPosition(DoubleFunction<Scale<?>> scaleForPosition) {
		this.scaleForPosition = scaleForPosition;
	}

	public double getActivityBias() { return activityBias; }
	public void setActivityBias(double activityBias) { this.activityBias = activityBias; }

	public PackedCollection<?> getDestination() {
		return destination;
	}

	public void setDestination(PackedCollection<?> destination) {
		this.destination = destination;
	}

	public Supplier<PackedCollection<?>> getIntermediateDestination() {
		return () -> new PackedCollection<>(frames);
	}

	public List<ChannelSection> getSections() {
		return sections;
	}

	public void setSections(List<ChannelSection> sections) {
		this.sections = sections;
	}

	public ChannelSection getSection(double measure) {
		if (sections == null || sections.isEmpty()) return null;

		return sections.stream()
				.filter(s -> s.getPosition() <= measure && measure < (s.getPosition() + s.getLength()))
				.findFirst()
				.orElse(null);
	}
}