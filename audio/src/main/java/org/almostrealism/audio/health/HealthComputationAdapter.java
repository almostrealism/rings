/*
 * Copyright 2025 Michael Murray
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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.almostrealism.audio.AudioMeter;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.Receptor;
import org.almostrealism.heredity.TemporalCellular;

public abstract class HealthComputationAdapter implements AudioHealthComputation<TemporalCellular> {
	public static final int MEASURE_COUNT = 2;
	public static int standardDurationSeconds = 230;
	public static int standardDurationFrames = standardDurationSeconds * OutputLine.sampleRate;

	private TemporalCellular target;
	private int channels;

	private WaveOutput out;
	private Supplier<String> outputFileSupplier;
	private IntFunction<String> stemFileSupplier;
	private File outputFile;
	private Map<Integer, File> stemFiles;
	private Consumer<WaveDetails> detailsProcessor;

	private List<AudioMeter> measures;
	private List<WaveOutput> stems;

	public HealthComputationAdapter(int channels) {
		this.channels = channels;
		this.stemFiles = new HashMap<>();
	}

	public TemporalCellular getTarget() { return target; }

	@Override
	public void setTarget(TemporalCellular target) { this.target = target; }

	protected WaveOutput getWaveOut() { return out; }

	@Override
	public synchronized Receptor<PackedCollection<?>> getOutput() {
		if (out == null) {
			out = new WaveOutput(() ->
					Optional.ofNullable(outputFileSupplier).map(s -> {
						outputFile = new File(s.get());
						return outputFile;
					}).orElse(null), 24, standardDurationFrames);
		}

		return out;
	}

	@Override
	public List<AudioMeter> getMeasures() {
		if (measures != null) return measures;
		measures = IntStream.range(0, MEASURE_COUNT).mapToObj(i -> new AudioMeter()).collect(Collectors.toList());
		configureMeasures(measures);
		return measures;
	}

	@Override
	public List<WaveOutput> getStems() {
		if (stems == null && stemFileSupplier != null) {
			stems = IntStream.range(0, channels).mapToObj(i ->
				new WaveOutput(() -> {
							File f = new File(stemFileSupplier.apply(i));
							stemFiles.put(i, f);
							return f;
						}, 24, HealthComputationAdapter.standardDurationFrames)).collect(Collectors.toList());
		}

		return stems;
	}

	protected void configureMeasures(List<AudioMeter> measures) { }

	public void setStemFile(IntFunction<String> file) { this.stemFileSupplier = file; }

	public void setOutputFile(String file) { setOutputFile(() -> file); }
	public void setOutputFile(Supplier<String> file) { this.outputFileSupplier = file; }

	protected File getOutputFile() { return outputFile; }

	protected Collection<File> getStemFiles() { return stemFiles.values(); }

	public Consumer<WaveDetails> getWaveDetailsProcessor() { return detailsProcessor; }

	@Override
	public void setWaveDetailsProcessor(Consumer<WaveDetails> detailsProcessor) {
		this.detailsProcessor = detailsProcessor;
	}

	@Override
	public void reset() {
		AudioHealthComputation.super.reset();
		out.reset(); // TODO  Why is this called twice?
		if (stems != null) stems.forEach(WaveOutput::reset);
		measures.forEach(AudioMeter::reset);
		out.reset();
	}

	@Override
	public void destroy() {
		AudioHealthComputation.super.destroy();
		if (out != null) out.destroy();
		if (stems != null) stems.forEach(WaveOutput::destroy);
	}

	public static void setStandardDuration(int sec) {
		standardDurationSeconds = sec;
		standardDurationFrames = standardDurationSeconds * OutputLine.sampleRate;
	}

	public static File getAuxFile(File file, String suffix) {
		if (file == null) return null;
		return new File(file.getParentFile(), file.getName().replace(".wav", "." + suffix));
	}
}
