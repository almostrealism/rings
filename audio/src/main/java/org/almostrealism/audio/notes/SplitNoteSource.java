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

package org.almostrealism.audio.notes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SplitNoteSource implements PatternNoteSource, CellFeatures {
	public static final double defaultBpm = 120.0;

	private String source;
	private double duration;
	private double bpm;
	private List<PatternNote> notes;

	private KeyboardTuning tuning;
	private FileWaveDataProvider provider;
	private PackedCollection audio;

	public SplitNoteSource() { this(null, 0, defaultBpm); }

	public SplitNoteSource(String sourceFile, double duration) {
		this(sourceFile, duration, defaultBpm);
	}

	public SplitNoteSource(String sourceFile, double durationBeats, double bpm) {
		this.source = sourceFile;
		this.duration = durationBeats;
		this.bpm = bpm;
	}

	@Override
	public void setTuning(KeyboardTuning tuning) {
		this.tuning = tuning;

		if (notes != null) {
			for (PatternNote note : notes) {
				note.setTuning(tuning);
			}
		}
	}

	public String getOrigin() { return source; }

	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; notes = null; }

	public double getDuration() { return duration; }
	public void setDuration(double duration) { this.duration = duration; notes = null; }

	public double getBpm() { return bpm; }
	public void setBpm(double bpm) { this.bpm = bpm; notes = null; }

	@JsonIgnore
	private PackedCollection getAudio() {
		if (provider == null) {
			provider = new FileWaveDataProvider(source);
		}

		if (audio == null) {
			WaveData data = provider.get();
			if (data.getSampleRate() == OutputLine.sampleRate) {
				audio = provider.get().getCollection();
			}
		}

		return audio;
	}

	public List<PatternNote> getNotes() {
		if (notes == null) {
			PackedCollection<?> audio = getAudio();
			if (audio == null) return Collections.emptyList();

			double duration = getDuration() * 60.0 / getBpm();

			int frames = (int) (duration * OutputLine.sampleRate);
			int total = (int) (audio.getMemLength() / (duration * OutputLine.sampleRate));
			notes = IntStream.range(0, total)
					.mapToObj(i -> (Supplier<PackedCollection>) () ->
							new PackedCollection<>(shape(frames), 1, audio, i * frames))
					.map(PatternNote::new)
					.map(note -> {
						note.setTuning(tuning);
						return note;
					})
					.collect(Collectors.toList());
		}

		return notes;
	}
}
