/*
 * Copyright 2023 Michael Murray
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

import org.almostrealism.Ops;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.KernelizedEvaluable;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;

// TODO  It would be better to use this in PatternLayerManager
public class PatternAudio implements CellFeatures {
	private static ContextSpecific<KernelizedEvaluable<PackedCollection<?>>> add;

	static {
		add = new DefaultContextSpecific<>(() -> Ops.o().add(
				Ops.o().v(1, 0),
				Ops.o().v(1, 1)).get());
	}

	private int sampleRate;
	private double bpm, beats, scale;
	private PackedCollection<?> data;

	public PatternAudio(double bpm, double beats) {
		this(bpm, beats, 4);
	}

	public PatternAudio(double bpm, double beats, double scale) {
		this(bpm, beats, scale, OutputLine.sampleRate);
	}

	public PatternAudio(double bpm, double beats, double scale, int sampleRate) {
		this.bpm = bpm;
		this.beats = beats;
		this.scale = scale;
		this.sampleRate = sampleRate;
		this.data = new PackedCollection((int) (bpm(bpm).l(beats) * sampleRate));
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public double getBpm() {
		return bpm;
	}

	public double getBeats() {
		return beats;
	}

	public double getScale() {
		return scale;
	}

	public PackedCollection getData() {
		return data;
	}

	public void push(PatternElement e) {
		for (int i = 0; i < e.getRepeatCount(); i++) {
			write(e.getNote().getAudio(), e.getPosition() + e.getRepeatDuration() * i);
		}
	}

	protected void write(PackedCollection<?> data, double position) {
		write(data, (int) (bpm(bpm).l(position * scale) * sampleRate), data.getShape().size(0));
	}

	protected void write(PackedCollection<?> data, int offset, int length) {
		PackedCollection dest = this.data.delegate(offset, length).traverse(1);
		add.getValue().into(dest).evaluate(dest, data.traverse(1));
	}
}
