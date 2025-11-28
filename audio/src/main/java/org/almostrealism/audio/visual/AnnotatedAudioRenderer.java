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

package org.almostrealism.audio.visual;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGB;
import org.almostrealism.time.AcceleratedTimeSeries;

import java.io.File;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * @deprecated This class depends on APIs that have been removed from ar-common v0.71
 *             (RealizableImage.of and AcceleratedTimeSeries.getTypeAt).
 */
@Deprecated
public class AnnotatedAudioRenderer implements CellFeatures {
	private double sampleRate;
	private PackedCollection wav;
	private AcceleratedTimeSeries annotation;
	private IntFunction<RGB[]> typeColors;
	private double annotationFrequency;
	private double ampGain;

	public AnnotatedAudioRenderer(File wav, AcceleratedTimeSeries annotation, IntFunction<RGB[]> typeColors, double annotationFrequency) throws IOException {
		this(wav, annotation, typeColors, annotationFrequency, 1.0);
	}

	public AnnotatedAudioRenderer(File wav, AcceleratedTimeSeries annotation, IntFunction<RGB[]> typeColors, double annotationFrequency, double ampGain) throws IOException {
		this(0, null, annotation, typeColors, annotationFrequency, ampGain);
		loadWav(wav);
	}

	public AnnotatedAudioRenderer(double sampleRate, PackedCollection wav, AcceleratedTimeSeries annotation, IntFunction<RGB[]> typeColors, double annotationFrequency, double ampGain) {
		this.sampleRate = sampleRate;
		this.wav = wav;
		this.annotation = annotation;
		this.typeColors = typeColors;
		this.annotationFrequency = annotationFrequency;
		this.ampGain = ampGain;
	}

	/**
	 * @deprecated RealizableImage.of has been removed from ar-common v0.71
	 */
	@Deprecated
	public Object render(double w, double h) {
		throw new UnsupportedOperationException("RealizableImage.of has been removed from ar-common v0.71");
	}

	/**
	 * @deprecated AcceleratedTimeSeries.getTypeAt has been removed from ar-common v0.71
	 */
	@Deprecated
	public void printAnnotationStats() {
		throw new UnsupportedOperationException("AcceleratedTimeSeries.getTypeAt has been removed from ar-common v0.71");
	}

	private void loadWav(File f) throws IOException {
		try (WavFile in = WavFile.openWavFile(f)) {
			sampleRate = in.getSampleRate();
			double data[][] = new double[in.getNumChannels()][(int) in.getFramesRemaining()];
			in.readFrames(data, (int) in.getFramesRemaining());

			wav = new PackedCollection(data[0].length).traverse(1);
			IntStream.range(0, wav.getCount()).forEach(i -> wav.setMem(i, data[0][i]));
		}
	}
}
