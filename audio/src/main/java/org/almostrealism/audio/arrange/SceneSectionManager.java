/*
 * Copyright 2022 Michael Murray
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
import org.almostrealism.hardware.OperationList;
import org.almostrealism.heredity.ConfigurableGenome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SceneSectionManager implements Setup {
	private List<SceneSection> sections;
	private OperationList setup;

	private ConfigurableGenome genome;
	private int channels;

	private DoubleSupplier measureDuration;
	private int sampleRate;

	public SceneSectionManager(ConfigurableGenome genome, int channels, DoubleSupplier measureDuration, int sampleRate) {
		this.sections = new ArrayList<>();
		this.setup = new OperationList("SceneSectionManager Setup");
		this.genome = genome;
		this.channels = channels;
		this.measureDuration = measureDuration;
		this.sampleRate = sampleRate;
	}

	public ConfigurableGenome getGenome() {
		return genome;
	}

	public List<SceneSection> getSections() { return Collections.unmodifiableList(sections); }

	public List<ChannelSection> getChannelSections(int channel) {
		return sections.stream().map(s -> s.getChannelSection(channel)).collect(Collectors.toList());
	}

	public SceneSection addSection(int position, int length) {
		DefaultChannelSectionFactory channelFactory = new DefaultChannelSectionFactory(genome, channels, measureDuration, length, sampleRate);
		SceneSection s = SceneSection.createSection(position, length, channels, () -> channelFactory.createSection(position));
		sections.add(s);
		setup.add(channelFactory.setup());
		return s;
	}

	public void removeSection(int index) {
		sections.remove(index);
		setup.remove(index);
		genome.removeChromosome(index);
	}

	@Override
	public Supplier<Runnable> setup() { return setup; }
}
