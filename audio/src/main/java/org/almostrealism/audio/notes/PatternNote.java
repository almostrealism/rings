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

package org.almostrealism.audio.notes;

import io.almostrealism.relation.Evaluable;
import io.almostrealism.relation.Producer;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.tone.KeyPosition;
import org.almostrealism.audio.tone.KeyboardTuning;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PatternNote extends PatternNoteAudioAdapter {
	private PatternNote delegate;
	private NoteAudioFilter filter;

	private List<PatternNoteLayer> layers;

	public PatternNote() { }

	public PatternNote(List<PatternNoteLayer> layers) {
		this.layers = layers;
	}

	public PatternNote(double... noteAudioSelections) {
		this(new ArrayList<>());

		for (double noteAudioSelection : noteAudioSelections) {
			addLayer(noteAudioSelection);
		}
	}

	public PatternNote(PatternNote delegate, NoteAudioFilter filter) {
		this.delegate = delegate;
		this.filter = filter;
	}

	public void addLayer(double noteAudioSelection) {
		layers.add(new PatternNoteLayer(noteAudioSelection));
	}

	public List<PatternNoteLayer> getLayers() {
		return layers;
	}

	public void setTuning(KeyboardTuning tuning) {
		if (delegate == null) {
			layers.forEach(l -> l.setTuning(tuning));
		} else {
			delegate.setTuning(tuning);
		}
	}

	@Override
	protected PatternNoteAudio getDelegate() {
		return delegate;
	}

	@Override
	protected NoteAudioFilter getFilter() {
		return filter;
	}

	@Override
	public double getDuration(KeyPosition<?> target, DoubleFunction<NoteAudioProvider> audioSelection) {
		if (delegate != null) return delegate.getDuration(target, audioSelection);
		return layers.stream().mapToDouble(l -> l.getDuration(target, audioSelection)).max().orElse(0.0);
	}

	@Override
	public int getSampleRate(KeyPosition<?> target, DoubleFunction<NoteAudioProvider> audioSelection) {
		return OutputLine.sampleRate;
	}

	@Override
	public Producer<PackedCollection<?>> getAudio(KeyPosition<?> target,
												  DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() != null) return super.getAudio(target, audioSelection);
		return combineLayers(target, audioSelection);
	}

	protected Producer<PackedCollection<?>> computeAudio(KeyPosition<?> target, double noteDuration,
														 DoubleFunction<NoteAudioProvider> audioSelection) {
		if (getDelegate() != null) {
			return super.computeAudio(target, noteDuration, audioSelection);
		}

		return combineLayers(target, audioSelection);
	}

	protected Producer<PackedCollection<?>> combineLayers(KeyPosition<?> target,
														  DoubleFunction<NoteAudioProvider> audioSelection) {
		return () -> {
			List<Evaluable<PackedCollection<?>>> layerAudio =
					layers.stream()
							.map(l -> l.getAudio(target, audioSelection).get())
							.collect(Collectors.toList());
			int frames[] = IntStream.range(0, layerAudio.size())
					.map(i -> (int) (layers.get(i).getDuration(target, audioSelection) *
							layers.get(i).getSampleRate(target, audioSelection)))
					.toArray();

			return args -> {
				int totalFrames = (int) (getDuration(target, audioSelection) * getSampleRate(target, audioSelection));

				PackedCollection<?> dest = PackedCollection.factory().apply(totalFrames);
				for (int i = 0; i < layerAudio.size(); i++) {
					PackedCollection<?> audio = layerAudio.get(i).evaluate(args);
					int f = Math.min(frames[i], totalFrames);

					PatternNoteAudio.sum.sum(dest.range(shape(f)), audio.range(shape(f)));
				}

				return dest;
			};
		};
	}

	@Override
	protected NoteAudioProvider getProvider(KeyPosition<?> target,
											DoubleFunction<NoteAudioProvider> audioSelection) {
		throw new UnsupportedOperationException();
	}
}
