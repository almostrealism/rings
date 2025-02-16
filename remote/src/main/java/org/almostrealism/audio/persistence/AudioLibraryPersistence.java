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

package org.almostrealism.audio.persistence;

import org.almostrealism.audio.AudioLibrary;
import org.almostrealism.audio.api.Audio;
import org.almostrealism.audio.data.WaveDetails;
import org.almostrealism.io.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AudioLibraryPersistence {
	public static int batchSize = Integer.MAX_VALUE / 2;

	public static Consumer<WaveDetails> saveWaveDetails(String destination) {
		return details -> {
			try {
				saveWaveDetails(details, destination);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static void saveWaveDetails(WaveDetails details, String destination) throws IOException {
		File f = new File(destination);
		if (f.isDirectory()) {
			f = new File(f, details.getIdentifier() + ".bin");
		}

		encode(details, true).writeTo(new FileOutputStream(f));
	}

	public static WaveDetails loadWaveDetails(String source) throws IOException {
		return decode(Audio.WaveDetailData.newBuilder().mergeFrom(new FileInputStream(source)).build());
	}

	public static void saveLibrary(AudioLibrary library, String dataPrefix) {
		try {
			saveLibrary(library, new LibraryDestination(dataPrefix).out());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void saveLibrary(AudioLibrary library, Supplier<OutputStream> out) throws IOException {
		saveLibrary(library, false, out);
	}

	public static void saveLibrary(AudioLibrary library, boolean includeAudio, Supplier<OutputStream> out) throws IOException {
		Audio.AudioLibraryData.Builder data = Audio.AudioLibraryData.newBuilder();
		List<WaveDetails> details = new ArrayList<>(library.getDetails());

		int byteCount = 0;

		for (int i = 0; i < details.size(); i++) {
			if (byteCount > batchSize || i == details.size() - 1) {
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

			Audio.WaveDetailData d = encode(details.get(i), includeAudio);
			byteCount += d.getSerializedSize();
			data.putInfo(d.getIdentifier(), d);
		}
	}

	public static AudioLibrary loadLibrary(File root, int sampleRate, String dataPrefix) {
		try {
			return loadLibrary(root, sampleRate, new LibraryDestination(dataPrefix).in());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void loadLibrary(AudioLibrary library, String dataPrefix) {
		try {
			loadLibrary(library, new LibraryDestination(dataPrefix).in());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static AudioLibrary loadLibrary(File root, int sampleRate, Supplier<InputStream> in) throws IOException {
		return loadLibrary(AudioLibrary.load(root, sampleRate), in);
	}

	public static AudioLibrary loadLibrary(AudioLibrary library, Supplier<InputStream> in) throws IOException {
		InputStream input = in.get();

		while (input != null) {
			Audio.AudioLibraryData data = Audio.AudioLibraryData.newBuilder().mergeFrom(input).build();

			for (Audio.WaveDetailData d : data.getInfoMap().values()) {
				library.include(decode(d));
			}

			input = in.get();
		}

		return library;
	}

	public static Audio.WaveDetailData encode(WaveDetails details, boolean includeAudio) {
		Audio.WaveDetailData.Builder data = Audio.WaveDetailData.newBuilder()
				.setIdentifier(details.getIdentifier())
				.setSampleRate(details.getSampleRate())
				.setChannelCount(details.getChannelCount())
				.setFrameCount(details.getFrameCount())
				.setFreqSampleRate(details.getFreqSampleRate())
				.setFreqBinCount(details.getFreqBinCount())
				.setFreqChannelCount(details.getFreqChannelCount())
				.setFreqFrameCount(details.getFreqFrameCount())
				.setFreqData(CollectionEncoder.encode(details.getFreqData()))
				.setFeatureSampleRate(details.getFeatureSampleRate())
				.setFeatureBinCount(details.getFeatureBinCount())
				.setFeatureChannelCount(details.getFeatureChannelCount())
				.setFeatureFrameCount(details.getFeatureFrameCount())
				.setFeatureData(CollectionEncoder.encode(details.getFeatureData()))
				.putAllSimilarities(details.getSimilarities());
		if (includeAudio) data.setData(CollectionEncoder.encode(details.getData()));
		return data.build();
	}

	public static WaveDetails decode(Audio.WaveDetailData data) {
		WaveDetails details = new WaveDetails(data.getIdentifier());
		details.setSampleRate(data.getSampleRate());
		details.setChannelCount(data.getChannelCount());
		details.setFrameCount(data.getFrameCount());
		details.setFreqSampleRate(data.getFreqSampleRate());
		details.setFreqBinCount(data.getFreqBinCount());
		details.setFreqChannelCount(data.getFreqChannelCount());
		details.setFreqFrameCount(data.getFreqFrameCount());
		details.setFreqData(CollectionEncoder.decode(data.getFreqData()));
		details.setFeatureSampleRate(data.getFeatureSampleRate());
		details.setFeatureBinCount(data.getFeatureBinCount());
		details.setFeatureChannelCount(data.getFeatureChannelCount());
		details.setFeatureFrameCount(data.getFeatureFrameCount());
		if (data.hasFeatureData()) details.setFeatureData(CollectionEncoder.decode(data.getFeatureData()));
		details.getSimilarities().putAll(data.getSimilaritiesMap());
		if (data.hasData()) details.setData(CollectionEncoder.decode(data.getData()));
		return details;
	}

	public static class LibraryDestination {
		private String prefix;
		private int index;

		public LibraryDestination(String prefix) {
			if (prefix.contains("/")) {
				this.prefix = prefix;
			} else {
				this.prefix = SystemUtils.getLocalDestination(prefix);
			}
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
