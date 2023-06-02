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

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.color.RGB;
import org.almostrealism.color.RealizableImage;
import org.almostrealism.time.AcceleratedTimeSeries;

import java.io.File;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class AnnotatedAudioRenderer implements CellFeatures {
	private double sampleRate;
	private PackedCollection<Scalar> wav;
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

	public AnnotatedAudioRenderer(double sampleRate, PackedCollection<Scalar> wav, AcceleratedTimeSeries annotation, IntFunction<RGB[]> typeColors, double annotationFrequency, double ampGain) {
		this.sampleRate = sampleRate;
		this.wav = wav;
		this.annotation = annotation;
		this.typeColors = typeColors;
		this.annotationFrequency = annotationFrequency;
		this.ampGain = ampGain;
	}

	public RealizableImage render(double w, double h) {
		double scale = wav.getCount() / w;
		return new RealizableImage(coords -> {
			double pos = coords.getY() / h;
			int index = (int) (scale * coords.getX());
			double loc = snapToGrid(scale * coords.getX() / sampleRate);

			int type = (int) annotation.valueAt(loc).getValue();

			if (pos < ampGain * Math.pow(wav.get(index).getValue(), 2.0)) {
				return typeColors.apply(type)[0];
			} else {
				return typeColors.apply(type)[1];
			}
		}, new Pair(w, h));
	}

	private double snapToGrid(double loc) {
		double pos = 0;
		while (true) {
			if (pos + annotationFrequency > loc) return pos;
			pos += annotationFrequency;
		}
	}

	private void loadWav(File f) throws IOException {
		WavFile in = WavFile.openWavFile(f);
		sampleRate = in.getSampleRate();
		double data[][] = new double[in.getNumChannels()][(int) in.getFramesRemaining()];
		in.readFrames(data, (int) in.getFramesRemaining());

		wav = Scalar.scalarBank(data[0].length);
		IntStream.range(0, wav.getCount()).forEach(i -> wav.set(i, data[0][i]));
	}
}
