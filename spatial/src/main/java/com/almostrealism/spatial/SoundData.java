/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial;

import org.almostrealism.audio.data.FileWaveDataProvider;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.data.WaveDataProvider;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.notes.NoteAudioProvider;
import org.almostrealism.audio.persistence.LibraryDestination;
import org.almostrealism.io.Console;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SoundData {
	private LibraryDestination library;
	private String file;
	private List<String> stemFiles;

	private String key;
	private WaveData data;

	public SoundData() { this(null); }

	public SoundData(String file) {
		this(file, (List<String>) null);
	}

	public SoundData(String file, List<String> stemFiles) {
		setFile(file);
		setStemFiles(stemFiles);
	}

	protected SoundData(LibraryDestination library,
						String key, WaveData data) {
		setLibrary(library);
		setKey(key);
		setData(data);
	}

	public LibraryDestination getLibrary() { return library; }
	public void setLibrary(LibraryDestination library) { this.library = library; }

	public String getFile() {
		if (file == null && getData() != null) {
			file = Optional.ofNullable(library.getTemporaryWave(getKey(), getData()))
					.map(File::getAbsolutePath)
					.orElse(null);
		}

		return file;
	}

	public void setFile(String file) { this.file = file; }

	public List<String> getStemFiles() { return stemFiles; }
	public void setStemFiles(List<String> stemFiles) { this.stemFiles = stemFiles; }

	public String getKey() { return key; }
	public void setKey(String key) { this.key = key; }

	public WaveData getData() { return data; }
	public void setData(WaveData data) { this.data = data; }

	public WaveDataProvider toProvider() {
		return new FileWaveDataProvider(getFile());
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SoundData soundData)) return false;

		if (getKey() != null && soundData.getKey() != null) {
			return Objects.equals(getKey(), soundData.getKey()) &&
					Objects.equals(getStemFiles(), soundData.getStemFiles());
		}

		return Objects.equals(getFile(), soundData.getFile());
	}

	@Override
	public int hashCode() {
		return getKey() == null ? Objects.hash(getFile()) : Objects.hashCode(getKey());
	}

	public static SoundData create(WaveDataProvider provider) {
		if (provider == null) {
			return null;
		}

		if (provider instanceof FileWaveDataProvider) {
			return new SoundData(((FileWaveDataProvider) provider).getResourcePath());
		}

		// TODO  There are probable better solutions for other providers
		return new SoundData(null, provider.getKey(), null);
	}

	public static SoundData create(LibraryDestination library, NoteAudioProvider note) {
		if (note.getProvider() == null) {
			return null;
		}

		try {
			WaveData data = note.getProvider().get();
			if (data == null) return null;

			if (data.getSampleRate() == OutputLine.sampleRate) {
				return new SoundData(library, note.getProvider().getKey(), data);
			} else {
				Console.root().features(SoundData.class)
						.warn("Sample rate of " + data.getSampleRate() +
							" does not match required sample rate of " + OutputLine.sampleRate);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static SoundData create(LibraryDestination library, String key, WaveData data) {
		return new SoundData(library, key, data);
	}
}
