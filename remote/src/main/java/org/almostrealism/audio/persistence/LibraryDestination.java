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
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.api.Audio;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.io.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class LibraryDestination implements ConsoleFeatures {
	public static final String TEMP = "temp";
	public static final String SAMPLES = "samples";

	private String prefix;
	private int index;
	private boolean append;

	public LibraryDestination(String prefix) {
		this(prefix, false);
	}

	public LibraryDestination(String prefix, boolean append) {
		if (prefix.contains("/")) {
			this.prefix = prefix;
		} else {
			this.prefix = SystemUtils.getLocalDestination(prefix);
		}

		this.append = append;
	}

	protected String nextFile() {
		return prefix + "_" + index++ + ".bin";
	}

	protected Iterator<String> files() {
		return new Iterator<>() {
			int idx = 0;

			@Override
			public boolean hasNext() {
				return new File(prefix + "_" + idx + ".bin").exists();
			}

			@Override
			public String next() {
				return prefix + "_" + idx++ + ".bin";
			}
		};
	}

	public Supplier<InputStream> in() {
		Iterator<String> all = files();

		return () -> {
			try {
				if (!all.hasNext())
					return null;

				return new FileInputStream(all.next());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public Supplier<OutputStream> out() {
		return () -> {
			try {
				File f = new File(nextFile());
				while (append && f.exists()) {
					f = new File(nextFile());
				}

				return new FileOutputStream(f);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public void load(AudioLibrary library) {
		try {
			AudioLibraryPersistence.loadLibrary(library, in());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(AudioLibrary library) {
		try {
			AudioLibraryPersistence.saveLibrary(library, out());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<Audio.AudioLibraryData> load() {
		Supplier<InputStream> in = in();
		InputStream input = in.get();

		List<Audio.AudioLibraryData> result = new ArrayList<>();

		try {
			while (input != null) {
				result.add(Audio.AudioLibraryData.newBuilder().mergeFrom(input).build());
				input = in.get();
			}

			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void save(Audio.AudioLibraryData data) {
		try {
			data.writeTo(out().get());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Path getTemporaryPath() {
		Path p = SystemUtils.getLocalDestination().resolve(TEMP);
		return SystemUtils.ensureDirectoryExists(p);
	}

	public File getTemporaryFile(String key, String extension) {
		if (key.contains("/")) {
			key = Base64.getEncoder().encodeToString(key.getBytes());
		}

		return temporary(getTemporaryPath().resolve(key + "." + extension).toFile());
	}

	public OutputStream getTemporaryDestination(String key, String extension) {
		try {
			return new FileOutputStream(getTemporaryFile(key, extension));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public File getTemporaryWave(String key, WaveData data) {
		try {
			File f = getTemporaryFile(key, "wav");

			if (!f.exists()) {
				WavFile.write(data, f);
			}

			return f;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void delete() {
		files().forEachRemaining(f -> new File(f).delete());
		index = 0;
	}

	public void cleanup() {
		try {
			clean(getTemporaryPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected File temporary(File f) {
		f.deleteOnExit();
		return f;
	}

	protected void clean(Path directory) {
		Path root = directory.getRoot();
		if (root != null && root.equals(directory)) {
			throw new IllegalArgumentException();
		}

		File dir = directory.toFile();
		if (!dir.isDirectory() || dir.getPath().length() < 3) {
			throw new IllegalArgumentException();
		}

		for (File f : Objects.requireNonNull(dir.listFiles())) {
			f.delete();
		}
	}

	public static Path getDefaultLibraryRoot() {
		Path p = SystemUtils.getLocalDestination().resolve(SAMPLES);
		return SystemUtils.ensureDirectoryExists(p);
	}
}
