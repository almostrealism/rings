/*
 * Copyright 2022 Michael Murray
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
import org.almostrealism.audio.data.ParameterFunction;
import org.almostrealism.audio.data.ParameterSet;
import org.almostrealism.audio.notes.ListNoteSource;
import org.almostrealism.audio.notes.PatternNote;
import org.almostrealism.audio.notes.PatternNoteSource;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.util.KeyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatternElementFactory {
	public static NoteDurationStrategy CHORD_STRATEGY = NoteDurationStrategy.FIXED;

	private String id;
	private String name;
	private List<PatternNoteSource> sources;
	private boolean melodic;

	private ParameterizedPositionFunction noteSelection;
	private ParameterFunction noteLengthSelection;
	private ParameterizedEnvelope envelope;

	private ChordPositionFunction chordNoteSelection;

	private ParameterizedPositionFunction repeatSelection;

	public PatternElementFactory() {
		this(new PatternNoteSource[0]);
	}

	public PatternElementFactory(String name) {
		this(name, new PatternNoteSource[0]);
	}

	public PatternElementFactory(PatternNote... notes) {
		this(null, notes);
	}

	public PatternElementFactory(PatternNoteSource... sources) {
		this(null, sources);
	}

	public PatternElementFactory(String name, PatternNote... notes) {
		this(name, new ListNoteSource(notes));
	}

	public PatternElementFactory(String name, PatternNoteSource... sources) {
		setId(KeyUtils.generateKey());
		setName(name);
		setSources(new ArrayList<>());
		getSources().addAll(List.of(sources));
		initSelectionFunctions();
	}

	public void initSelectionFunctions() {
		noteSelection = ParameterizedPositionFunction.random();
		noteLengthSelection = ParameterFunction.random();
		envelope = ParameterizedEnvelope.random();
		chordNoteSelection = ChordPositionFunction.random();
		repeatSelection = ParameterizedPositionFunction.random();
	}

	public String getId() { return id; }
	public void setId(String id) { this.id = id; }

	public String getName() { return name; }
	public void setName(String name) { this.name = name; }

	public List<PatternNote> getAllNotes() {
		return sources.stream().map(PatternNoteSource::getNotes).flatMap(List::stream).collect(Collectors.toList());
	}

	public List<PatternNoteSource> getSources() { return sources; }
	public void setSources(List<PatternNoteSource> sources) {
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

	@Deprecated
	public ParameterizedPositionFunction getScalePositionSelection() {
		return null;
	}

	@Deprecated
	public void setScalePositionSelection(ParameterizedPositionFunction scalePositionSelection) {
	}

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

	@JsonIgnore
	public List<PatternNote> getValidNotes() {
		return getAllNotes().stream().filter(PatternNote::isValid).collect(Collectors.toList());
	}

	// TODO  This should take instruction for whether to apply note duration, relying just on isMelodic limits its use
	public Optional<PatternElement> apply(ElementParity parity, double position, double scale, double bias, int depth, boolean repeat, ParameterSet params) {
		if (parity == ElementParity.LEFT) {
			position -= scale;
		} else if (parity == ElementParity.RIGHT) {
			position += scale;
		}
		
		List<PatternNote> notes = getValidNotes();

		if (notes.isEmpty()) return Optional.empty();

		double note = noteSelection.apply(params, position, scale) + bias;
		while (note > 1) note -= 1;
		if (note < 0.0) return Optional.empty();

		PatternNote choice = envelope.apply(notes.get((int) (note * notes.size())));
		PatternElement element = new PatternElement(choice, position);
		element.setScalePosition(chordNoteSelection.applyAll(params, position, scale, depth));
		element.setNoteDurationSelection(noteLengthSelection.power(2.0, 3, -1).apply(params));
		element.setDurationStrategy(isMelodic() ?
				(depth > 1 ? CHORD_STRATEGY : NoteDurationStrategy.FIXED) :
					NoteDurationStrategy.NONE);

		// System.out.println("PatternElementFactory: duration = " + element.getNoteDurationSelection());

		double r = repeatSelection.apply(params, position, scale);

		if (!repeat || r <= 0) {
			element.setRepeatCount(1);
		} else {
			int c;
			for (c = 0; r < 1.0 & c < 4; c++) {
				r *= 2;
			}

			element.setRepeatCount(c);
		}

		element.setRepeatDuration(scale / 2.0);
		return Optional.of(element);
	}
}
