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

package org.almostrealism.audio.visual.test;

import org.almostrealism.audio.visual.AnnotatedAudioRenderer;
import org.almostrealism.color.RGB;
import org.almostrealism.texture.ImageCanvas;
import org.almostrealism.time.AcceleratedTimeSeries;
import org.almostrealism.time.TemporalScalar;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class AnnotatedAudioRendererTest {
	@Test
	public void render() throws IOException {
		AcceleratedTimeSeries a = new AcceleratedTimeSeries(50);
		IntStream.range(0, 16).forEach(i -> a.add(new TemporalScalar(i, i > 6 ? 1.0 : 0.0)));

		try (FileOutputStream out = new FileOutputStream("results/annotations.bin")) {
			a.persist(out);
		}

		AcceleratedTimeSeries annotation = new AcceleratedTimeSeries(50);

		try (FileInputStream in = new FileInputStream("results/annotations.bin")) {
			annotation.load(in);
		}

		RGB blue[] = new RGB[] { new RGB(0.0, 0.0, 1.0), new RGB(0.0, 0.0, 0.5) };
		RGB white[] = new RGB[] { new RGB(1.0, 1.0, 1.0), new RGB(0.0, 0.0, 0.0) };

		IntFunction<RGB[]> types = i -> {
			if (i == 1) {
				return blue;
			} else {
				return white;
			}
		};

		AnnotatedAudioRenderer renderer = new AnnotatedAudioRenderer(new File("results/mix-test.wav"), annotation, types, 1.0, 5.0);
		ImageCanvas.encodeImageFile(renderer.render(1000, 100).get().evaluate(),
				new File("results/image-test.jpg"), ImageCanvas.JPEGEncoding);
	}
}
