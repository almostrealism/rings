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

package org.almostrealism.audio.health;

import org.almostrealism.optimize.HealthScore;

import java.io.File;

public class AudioHealthScore implements HealthScore {
	private double score;
	private String output;

	public AudioHealthScore() { this(0.0, null); }

	public AudioHealthScore(double score) {
		this(score, null);
	}

	public AudioHealthScore(double score, String output) {
		this.score = score;
		this.output = output;
	}

	@Override
	public double getScore() { return score; }

	public void setScore(double score) { this.score = score; }

	public String getOutput() { return output; }

	public void setOutput(String output) { this.output = output; }
}
