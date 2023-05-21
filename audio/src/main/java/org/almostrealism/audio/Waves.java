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

package org.almostrealism.audio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.almostrealism.relation.Producer;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.SegmentList;
import org.almostrealism.audio.data.WaveDataProviderList;
import org.almostrealism.audio.sequence.TempoAware;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.hardware.OperationList;
import org.almostrealism.time.Frequency;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Waves implements TempoAware, CellFeatures {
	private WaveSet source;
	private int pos = -1, len = -1;
	private String sourceName;

	private RoutingChoices choices;
	private List<Waves> children;

	public Waves() { this(null); }

	public Waves(String sourceName) { this(sourceName, null, -1, -1); }

	public Waves(String sourceName, WaveSet source) {
		this(sourceName, source, -1, -1);
	}

	public Waves(String sourceName, WaveSet source, int pos, int len) {
		this.sourceName = sourceName;
		this.pos = pos;
		this.len = len;
		this.source = source;

		this.choices = new RoutingChoices();
		this.children = new ArrayList<>();
	}

	public List<Waves> getChildren() { return children; }
	public void setChildren(List<Waves> children) { this.children = children; }

	public int getPos() { return pos; }
	public void setPos(int pos) { this.pos = pos; }

	public int getLen() { return len; }
	public void setLen(int len) { this.len = len; }

	public String getSourceName() { return sourceName; }
	public void setSourceName(String sourceName) { this.sourceName = sourceName; }

	public WaveSet getSource() { return source; }
	public void setSource(WaveSet source) { this.source = source; }

	public RoutingChoices getChoices() { return choices; }
	public void setChoices(RoutingChoices choices) { this.choices = choices; }

	@Override
	public void setBpm(double bpm) {
		if (source != null) source.setBpm(bpm);
		getChildren().forEach(c -> c.setBpm(bpm));
	}

	public SegmentList getSegments(int src, Producer<Scalar> x, Producer<Scalar> y, Producer<Scalar> z) {
		if (!choices.getChoices().contains(src)) return new SegmentList(new ArrayList<>());

		if (isLeaf()) {
			WaveDataProviderList provider = source.create(x, y, z);
			return new SegmentList(provider.getProviders()
					.stream()
					.map(p -> (PackedCollection<?>) ((pos >= 0 && len >= 0) ? p.get().getCollection() :
							p.get().getCollection().range(shape(len).traverse(1), pos)))
					.collect(Collectors.toList()), provider.setup());
		} else {
			List<SegmentList> lists = getChildren().stream().map(c -> c.getSegments(src, x, y, z)).collect(Collectors.toList());
			List<PackedCollection<?>> segments = lists.stream().map(SegmentList::getSegments).flatMap(List::stream).collect(Collectors.toList());
			OperationList setup = lists.stream().map(SegmentList::setup).collect(OperationList.collector());
			return new SegmentList(segments, setup);
		}
	}

	public PackedCollection<?> getSegmentChoice(int src, double decision, double x, double y, double z) {
		SegmentList segments = getSegments(src, v(x), v(y), v(z));
		if (segments.isEmpty()) return null;

		return segments.getSegments().get((int) (decision * segments.getSegments().size()));
	}

	@JsonIgnore
	public boolean isLeaf() { return source != null || (pos > -1 && len > -1); }

	public void addFiles(RoutingChoices choices, File... files) { addFiles(choices, List.of(files)); }

	public void addFiles(RoutingChoices choices, Collection<File> files) {
		if (isLeaf()) throw new UnsupportedOperationException();

		files.stream().map(file -> {
					try {
						Waves waves = Waves.loadAudio(file, w -> w.getSampleRate() == OutputLine.sampleRate);
						waves.setChoices(choices);
						return waves;
					} catch (UnsupportedOperationException | IOException e) {
						return null;
					}
				}).filter(Objects::nonNull)
//				.map(wav -> {
//					Waves waves = new Waves(wav.getSourceName());
//					waves.getChoices().getChoices().addAll(choices.getChoices());
//					waves.getChildren().add(wav);
//					return waves;
//				})
				.forEach(this.getChildren()::add);
	}

	public void addSplits(Collection<File> files, double bpm, double silenceThreshold, Set<Integer> choices, Double... splits) {
		addSplits(files, bpm(bpm), silenceThreshold, choices, splits);
	}

	public void addSplits(Collection<File> files, Frequency bpm, double silenceThreshold, Set<Integer> choices, Double... splits) {
		if (isLeaf()) throw new UnsupportedOperationException();

		List<Double> sizes = List.of(splits);

		files.stream().map(file -> {
					try {
						Waves wav = Waves.loadAudio(file, w -> w.getSampleRate() == OutputLine.sampleRate);
						wav.getChoices().getChoices().addAll(choices);
						return wav;
					} catch (UnsupportedOperationException | IOException e) {
						return null;
					}
				}).filter(Objects::nonNull).flatMap(wav ->
						sizes.stream()
								.map(beats -> bpm.l(beats) * OutputLine.sampleRate)
								.mapToDouble(duration -> duration)
								.mapToInt(frames -> (int) frames)
								.mapToObj(f -> wav.split(f, silenceThreshold)))
				.forEach(this.getChildren()::add);
	}

	public Waves split(int frames, double silenceThreshold) {
		if (!isLeaf()) throw new UnsupportedOperationException();

		Waves waves = new Waves(sourceName);
		waves.getChoices().getChoices().addAll(getChoices().getChoices());
		int pos = this.pos < 0 ? 0 : this.pos;
		int len = this.len < 0 ? source.getCount() : this.len;
		IntStream.range(0, len / frames)
				.mapToObj(i -> {
					Waves w = new Waves(sourceName, source, pos + i * frames, frames);
					w.getChoices().getChoices().addAll(getChoices().getChoices());
					return w;
				})
				.filter(w -> w.getSegments(0, null, null, null).getSegments().get(0).length() > (silenceThreshold * frames))
				.forEach(w -> waves.getChildren().add(w));
		return waves;
	}

	public String asJson() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}

	public void store(File f) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
			out.write(asJson());
			out.flush();
		}
	}

	public static Waves load(File f) throws IOException {
		return new ObjectMapper().readValue(f.toURI().toURL(), Waves.class);
	}

	public static Waves loadAudio(File f) throws IOException {
		return loadAudio(f, v -> true);
	}

	public static Waves loadAudio(File f, Predicate<WavFile> validator) throws IOException {
		WavFile w = WavFile.openWavFile(f);
		if (w.getNumFrames() >= Integer.MAX_VALUE) throw new UnsupportedOperationException();
		if (!validator.test(w)) throw new IOException();

		double data[][] = new double[w.getNumChannels()][(int) w.getNumFrames()];
		w.readFrames(data, (int) w.getFramesRemaining());

		return new Waves(f.getCanonicalPath(), new WaveSet(new FileWaveDataProvider(f.getCanonicalPath())));
	}

	public static boolean isValid(File f, Predicate<WavFile> validator) {
		try {
			validate(f, validator);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static void validate(File f, Predicate<WavFile> validator) throws IOException {
		WavFile w = WavFile.openWavFile(f);
		if (w.getNumFrames() >= Integer.MAX_VALUE) throw new UnsupportedOperationException();
		if (!validator.test(w)) throw new IOException();
	}
}
