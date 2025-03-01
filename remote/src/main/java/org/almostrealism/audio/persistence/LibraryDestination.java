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
import java.util.Base64;
import java.util.Objects;
import java.util.function.Supplier;

public class LibraryDestination implements ConsoleFeatures {
	public static final String TEMP = "temp";

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
}
