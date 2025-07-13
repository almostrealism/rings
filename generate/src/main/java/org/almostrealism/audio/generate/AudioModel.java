/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.generate;

import org.almostrealism.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;

public class AudioModel {
	private String id;
	private String name;
	private double duration;
	private boolean pattern;
	private List<String> textConditions;

	public AudioModel() {
		this(null);
	}

	public AudioModel(String name) {
		this(name, new ArrayList<>());
	}

	public AudioModel(String name, List<String> textConditions) {
		setId(KeyUtils.generateKey());
		setName(name);
		setDuration(1);
		setPattern(false);
		setTextConditions(textConditions);
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public double getDuration() { return duration; }
	public void setDuration(double duration) { this.duration = duration; }

	public boolean isPattern() { return pattern; }
	public void setPattern(boolean pattern) { this.pattern = pattern; }

	public List<String> getTextConditions() { return textConditions; }
	public void setTextConditions(List<String> textConditions) { this.textConditions = textConditions; }
}
