/*
 * Copyright 2024 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.audio.stream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.filter.AudioProcessor;
import org.almostrealism.collect.PackedCollection;

import java.io.IOException;

public class AudioStreamHandler implements HttpHandler, CodeFeatures {
	public static double bufferDuration = 1.0;

	private PackedCollection<?> buffer;
	private int totalFrames;
	private int sampleRate;
	private Runnable update;

	public AudioStreamHandler(AudioProcessor audioProcessor,
							  int totalFrames, int sampleRate) {
		this.totalFrames = totalFrames;
		this.sampleRate = sampleRate;
		this.buffer = new PackedCollection<>((int) (sampleRate * bufferDuration));
		this.update = audioProcessor.process(cp(buffer), null).get();
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		exchange.getResponseHeaders().add("Content-Type", "audio/wav");
		exchange.sendResponseHeaders(200, 0);

		try (WavFile wav = WavFile.newWavFile(exchange.getResponseBody(), 2, totalFrames, 24, sampleRate)) {
			for (int pos = 0; pos < totalFrames; pos += buffer.getMemLength()) {
				update.run();

				for (int i = 0; (pos + i) < totalFrames; i++) {
					double value = buffer.toArray(i, 1)[0];
					wav.writeFrames(new double[][]{{value}, {value}}, 1);
				}
			}
		} catch (IOException e) {
			warn(e.getMessage());
		}
	}
}
