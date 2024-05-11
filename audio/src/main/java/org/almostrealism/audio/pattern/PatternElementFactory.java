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
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.filter.ParameterizedFilterEnvelope;
import org.almostrealism.audio.filter.ParameterizedVolumeEnvelope;
import org.almostrealism.audio.notes.ListNoteSource;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.NoteAudioSource;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatternElementFactory implements ConsoleFeatures {
	public static boolean enableVolumeEnvelope = true;
	public static boolean enableFilterEnvelope = true;
	public static boolean enableScaleNoteLength = true;
	public static boolean enableRegularizedNoteLength = false;

	public static NoteDurationStrategy CHORD_STRATEGY = NoteDurationStrategy.FIXED;
	public static double noteLengthFactor = 0.5;

	public static int[] REPEAT_DIST;

	private String id;
	private String name;
	private List<NoteAudioSource> sources;
	private boolean melodic;

	private ParameterizedPositionFunction noteSelection;
	private ParameterFunction noteLengthSelection;
	private ParameterizedVolumeEnvelope volumeEnvelope;
	private ParameterizedFilterEnvelope filterEnvelope;

	private ChordPositionFunction chordNoteSelection;

	private ParameterizedPositionFunction repeatSelection;

	public PatternElementFactory() {
		this(new NoteAudioSource[0]);
	}

	public PatternElementFactory(String name) {
		this(name, new NoteAudioSource[0]);
	}

	public PatternElementFactory(NoteAudioProvider... notes) {
		this(null, notes);
	}

	public PatternElementFactory(NoteAudioSource... sources) {
		this(null, sources);
	}

	public PatternElementFactory(String name, NoteAudioProvider... notes) {
		this(name, new ListNoteSource(notes));
	}

	public PatternElementFactory(String name, NoteAudioSource... sources) {
		setId(KeyUtils.generateKey());
		setName(name);
		setSources(new ArrayList<>());
		getSources().addAll(List.of(sources));
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		noteSelection = ParameterizedPositionFunction.random();
		noteLengthSelection = ParameterFunction.random();
		volumeEnvelope = ParameterizedVolumeEnvelope.random();
		filterEnvelope = ParameterizedFilterEnvelope.random();
		chordNoteSelection = ChordPositionFunction.random();
		repeatSelection = ParameterizedPositionFunction.random();
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	@JsonIgnore
	public List<NoteAudioProvider> getAllNotes() {
		return sources.stream()
				.map(NoteAudioSource::getNotes)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	public List<NoteAudioSource> getSources() { return sources; }
	public void setSources(List<NoteAudioSource> sources) {
		this.sources = sources;
	}

	public ParameterizedPositionFunction getNoteSelection() {
		return noteSelection;
	}
	public void setNoteSelection(ParameterizedPositionFunction noteSelection) {
		this.noteSelection = noteSelection;
	}

	public ParameterFunction getNoteLengthSelection() { return noteLengthSelection; }
	public void setNoteLengthSelection(ParameterFunction noteLengthSelection) { this.noteLengthSelection = noteLengthSelection; }

	public ParameterizedVolumeEnvelope getVolumeEnvelope() { return volumeEnvelope; }
	public void setVolumeEnvelope(ParameterizedVolumeEnvelope volumeEnvelope) { this.volumeEnvelope = volumeEnvelope; }

	public ParameterizedFilterEnvelope getFilterEnvelope() { return filterEnvelope; }
	public void setFilterEnvelope(ParameterizedFilterEnvelope filterEnvelope) { this.filterEnvelope = filterEnvelope; }

	public ChordPositionFunction getChordNoteSelection() {
		return chordNoteSelection;
	}
	public void setChordNoteSelection(ChordPositionFunction chordNoteSelection) {
		this.chordNoteSelection = chordNoteSelection;
	}

	public ParameterizedPositionFunction getRepeatSelection() {
		return repeatSelection;
	}
	public void setRepeatSelection(ParameterizedPositionFunction repeatSelection) {
		this.repeatSelection = repeatSelection;
	}

	public boolean isMelodic() { return melodic; }
	public void setMelodic(boolean melodic) { this.melodic = melodic; }

	public void setTuning(KeyboardTuning tuning) {
		getSources().forEach(n -> n.setTuning(tuning));
	}

	public boolean checkResourceUsed(String canonicalPath) {
		return getSources().stream().anyMatch(s -> s.checkResourceUsed(canonicalPath));
	}

	@JsonIgnore
	public List<NoteAudioProvider> getValidNotes() {
		return getAllNotes().stream().filter(NoteAudioProvider::isValid).collect(Collectors.toList());
	}

	// TODO  This should take instruction for whether to apply note duration, relying just on isMelodic limits its use
	public Optional<PatternElement> apply(ElementParity parity, double position, double scale, double bias,
										  ScaleTraversalStrategy scaleTraversalStrategy,
										  int depth, boolean repeat, ParameterSet params) {
		if (parity == ElementParity.LEFT) {
			position -= scale;
		} else if (parity == ElementParity.RIGHT) {
			position += scale;
		}

		double note = noteSelection.apply(params, position, scale) + bias;
		while (note > 1) note -= 1;
		if (note < 0.0) return Optional.empty();

		PatternNote choice = new PatternNote(note);
		if (enableFilterEnvelope && melodic) choice = filterEnvelope.apply(params, choice);
		if (enableVolumeEnvelope) choice = volumeEnvelope.apply(params, choice);

		PatternElement element = new PatternElement(choice, position);
		element.setScalePosition(chordNoteSelection.applyAll(params, position, scale, depth));
		element.setDurationStrategy(isMelodic() ?
				(scaleTraversalStrategy == ScaleTraversalStrategy.CHORD ?
						CHORD_STRATEGY : NoteDurationStrategy.FIXED) :
					NoteDurationStrategy.NONE);

		double ls = scale > 1.0 ? 1.0 : scale;

		if (enableScaleNoteLength) {
			if (enableRegularizedNoteLength) {
				element.setNoteDurationSelection(ls * noteLengthSelection.power(2.0, 2, -2).apply(params));
			} else {
				element.setNoteDurationSelection(ls * noteLengthFactor * noteLengthSelection.positive().apply(params));
			}
		} else {
			element.setNoteDurationSelection(noteLengthSelection.power(2.0, 3, -3).apply(params));
		}

		double r = repeatSelection.apply(params, position, scale);

		if (!repeat || r <= 0) {
			element.setRepeatCount(1);
		} else {
			int c;
			for (c = 0; r < 3 & c < 6; c++) {
				r *= 1.8;
			}

			element.setRepeatCount(c);
		}

		if (REPEAT_DIST != null) {
			if (repeat) {
				REPEAT_DIST[element.getRepeatCount()]++;
			} else {
				REPEAT_DIST[0]++;
			}
		}

		element.setScaleTraversalStrategy(scaleTraversalStrategy);
		element.setRepeatDuration(ls * noteLengthFactor);
		return Optional.of(element);
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}
}
