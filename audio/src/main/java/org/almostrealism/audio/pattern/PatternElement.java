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

package org.almostrealism.audio.pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.arrange.AudioSceneContext;
import org.almostrealism.audio.notes.NoteAudioContext;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PatternElement implements CodeFeatures {
	private PatternNote note;
	private double position;

	private NoteDurationStrategy durationStrategy;
	private double noteDuration;

	private ScaleTraversalStrategy scaleTraversalStrategy;
	private List<Double> scalePositions;

	private PatternDirection direction;
	private int repeatCount;
	private double repeatDuration;

	private PackedCollection<?> automationParameters;

	public PatternElement() {
		this(null, 0.0);
	}

	public PatternElement(PatternNote note, double position) {
		setNote(note);
		setPosition(position);
		setDurationStrategy(NoteDurationStrategy.NONE);
		setScaleTraversalStrategy(ScaleTraversalStrategy.CHORD);
		setDirection(PatternDirection.FORWARD);
		setRepeatCount(1);
		setRepeatDuration(1);
	}

	public PatternNote getNote() { return note; }
	public void setNote(PatternNote note) {
		this.note = note;
	}

	public double getPosition() {
		return position;
	}
	public void setPosition(double position) {
		this.position = position;
	}

	public NoteDurationStrategy getDurationStrategy() { return durationStrategy; }
	public void setDurationStrategy(NoteDurationStrategy durationStrategy) {
		this.durationStrategy = durationStrategy;
	}

	public ScaleTraversalStrategy getScaleTraversalStrategy() {
		return scaleTraversalStrategy;
	}

	public void setScaleTraversalStrategy(ScaleTraversalStrategy scaleTraversalStrategy) {
		this.scaleTraversalStrategy = scaleTraversalStrategy;
	}

	public double getNoteDuration(DoubleUnaryOperator timeForDuration,
								  double position, double nextPosition,
								  double originalDurationSeconds) {
		return durationStrategy.getLength(timeForDuration, position, nextPosition,
							originalDurationSeconds, getNoteDurationSelection());
	}

	public double getNoteDurationSelection() { return noteDuration; }
	public void setNoteDurationSelection(double noteDuration) { this.noteDuration = noteDuration; }

	public List<Double> getScalePositions() { return scalePositions; }
	public void setScalePosition(List<Double> scalePositions) { this.scalePositions = scalePositions; }

	public PatternDirection getDirection() {
		return direction;
	}
	public void setDirection(PatternDirection direction) {
		this.direction = direction;
	}

	public int getRepeatCount() {
		return repeatCount;
	}
	public void setRepeatCount(int repeatCount) {
		this.repeatCount = repeatCount;
	}

	public double getRepeatDuration() {
		return repeatDuration;
	}
	public void setRepeatDuration(double repeatDuration) {
		this.repeatDuration = repeatDuration;
	}

	public PackedCollection<?> getAutomationParameters() {
		return automationParameters;
	}

	public void setAutomationParameters(PackedCollection<?> automationParameters) {
		this.automationParameters = automationParameters;
	}

	@JsonIgnore
	public void setTuning(KeyboardTuning tuning) {
		this.note.setTuning(tuning);
	}

	public List<Double> getPositions() {
		return IntStream.range(0, repeatCount)
				.mapToObj(i -> position + (i * repeatDuration))
				.collect(Collectors.toList());
	}

	public List<RenderedNoteAudio> getNoteDestinations(boolean melodic, double offset,
													   AudioSceneContext context,
													   NoteAudioContext audioContext) {
		return getScaleTraversalStrategy()
				.getNoteDestinations(this, melodic, offset,
									context, audioContext);
	}

	public Producer<PackedCollection<?>> getNoteAudio(boolean melodic, KeyPosition<?> target,
													  double position, double nextNotePosition,
													  Producer<PackedCollection<?>> automationLevel,
													  DoubleFunction<NoteAudioProvider> audioSelection,
													  DoubleUnaryOperator timeForDuration) {
		KeyPosition<?> k = melodic ? target : null;

		double originalDuration = getNote().getDuration(target, audioSelection);

		if (getDurationStrategy() == NoteDurationStrategy.NONE) {
			return getNote().getAudio(k, originalDuration, automationLevel, audioSelection);
		} else {
			return getNote().getAudio(k,
					getNoteDuration(timeForDuration, position,
						nextNotePosition, originalDuration),
					automationLevel, audioSelection);
		}
	}

	public boolean isPresent(double start, double end) {
		for (int i = 0; i < repeatCount; i++) {
			double pos = getPosition() + i * repeatDuration;
			if (pos >= start && pos < end) return true;
		}

		return false;
	}
}
