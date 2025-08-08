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

import io.almostrealism.cycle.Setup;
import io.almostrealism.lifecycle.Destroyable;
import org.almostrealism.audio.data.ChannelInfo;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ProjectedGenome;
import org.almostrealism.time.Frequency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SceneSectionManager implements Setup, Destroyable {
	public static final IntPredicate DEFAULT_REPEAT_CHANNELS = c -> c != 5;

	private List<SceneSection> sections;
	private OperationList setup;

	private ProjectedGenome genome;
	private int channels;

	private Supplier<Frequency> tempo;
	private DoubleSupplier measureDuration;
	private int sampleRate;

	private List<Integer> wetChannels;

	public SceneSectionManager(ProjectedGenome genome, int channels, Supplier<Frequency> tempo,
							   DoubleSupplier measureDuration, int sampleRate) {
		this.sections = new ArrayList<>();
		this.setup = new OperationList("SceneSectionManager Setup");
		this.genome = genome;
		this.channels = channels;
		this.tempo = tempo;
		this.measureDuration = measureDuration;
		this.sampleRate = sampleRate;
		this.wetChannels = new ArrayList<>();
	}

	public ProjectedGenome getGenome() {
		return genome;
	}

	public List<Integer> getWetChannels() { return wetChannels; }
	public void setWetChannels(List<Integer> wetChannels) { this.wetChannels = wetChannels; }

	public List<SceneSection> getSections() { return Collections.unmodifiableList(sections); }

	public List<ChannelSection> getChannelSections(ChannelInfo channel) {
		return sections.stream().map(s -> s.getChannelSection(channel)).collect(Collectors.toList());
	}

	public SceneSection addSection(int position, int length) {
		DefaultChannelSectionFactory channelFactory = new DefaultChannelSectionFactory(genome, channels,
																		c -> getWetChannels().contains(c),
																		DEFAULT_REPEAT_CHANNELS,
																		tempo, measureDuration, length, sampleRate);
		SceneSection s = SceneSection.createSection(position, length, channels, () -> channelFactory.createSection(position));
		sections.add(s);
		setup.add(channelFactory.setup());
		return s;
	}

	public void removeSection(int index) {
		sections.remove(index).destroy();
		setup.remove(index);
		genome.removeChromosome(index);
	}

	@Override
	public Supplier<Runnable> setup() { return setup; }

	@Override
	public void destroy() {
		Destroyable.super.destroy();
		sections.forEach(SceneSection::destroy);
		sections.clear();
	}
}
