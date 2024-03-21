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

package com.almostrealism.audio;

import com.almostrealism.audio.api.Audio;
import org.almostrealism.audio.AudioLibrary;
import org.almostrealism.audio.data.WaveDetails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AudioLibraryPersistence {
	public static int batchSize = Integer.MAX_VALUE / 2;

	public static void saveLibrary(AudioLibrary library, String filePrefix) {
		try {
			saveLibrary(library, new LibraryDestination(filePrefix).out());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static void saveLibrary(AudioLibrary library, Supplier<OutputStream> out) throws IOException {
		Audio.AudioLibraryData.Builder data = Audio.AudioLibraryData.newBuilder();
		List<WaveDetails> details = new ArrayList<>(library.getDetails());

		int byteCount = 0;

		for (int i = 0; i < details.size(); i++) {
			if (byteCount > batchSize) {
				OutputStream o = out.get();

				try {
					data.build().writeTo(o);
					data = Audio.AudioLibraryData.newBuilder();
					byteCount = 0;
					o.flush();
				} finally {
					o.close();
				}
			}

			Audio.WaveDetailData d = encode(details.get(i));
			byteCount += d.getSerializedSize();
			data.putInfo(d.getIdentifier(), d);
		}
	}

	public static Audio.WaveDetailData encode(WaveDetails details) {
		return Audio.WaveDetailData.newBuilder()
				.setIdentifier(details.getIdentifier())
				.setSampleRate(details.getSampleRate())
				.setChannelCount(details.getChannelCount())
				.setFrameCount(details.getFrameCount())
				.setData(CollectionEncoder.encode(details.getData()))
				.setFftSampleRate(details.getFftSampleRate())
				.setFftChannelCount(details.getFftChannelCount())
				.setFftFrameCount(details.getFftFrameCount())
				.setFftData(CollectionEncoder.encode(details.getFftData()))
				.build();
	}

	public static class LibraryDestination {
		private String prefix;
		private int index;

		public LibraryDestination(String prefix) {
			this.prefix = prefix;
		}

		protected String nextFile() {
			return prefix + "_" + index++ + ".bin";
		}

		public Supplier<InputStream> in() {
			return () -> {
				try {
					File f = new File(nextFile());
					if (!f.exists()) return null;

					return new FileInputStream(f);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			};
		}

		public Supplier<OutputStream> out() {
			return () -> {
				try {
					return new FileOutputStream(nextFile());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			};
		}
	}
}
