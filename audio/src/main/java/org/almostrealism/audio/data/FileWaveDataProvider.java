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

package org.almostrealism.audio.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.almostrealism.audio.WavFile;
import org.almostrealism.audio.WaveOutput;
import org.almostrealism.hardware.ctx.ContextSpecific;
import org.almostrealism.hardware.ctx.DefaultContextSpecific;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FileWaveDataProvider extends WaveDataProviderAdapter {

	private String resourcePath;

	public FileWaveDataProvider() { }

	public FileWaveDataProvider(String resourcePath) {
		setResourcePath(resourcePath);
	}

	@JsonIgnore
	@Override
	public String getKey() { return getResourcePath(); }

	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		clearKey(resourcePath);
		this.resourcePath = resourcePath;
	}

	@JsonIgnore
	@Override
	public int getSampleRate() {
		try {
			WavFile w = WavFile.openWavFile(new File(resourcePath));
			long count = w.getNumFrames();
			if (count > Integer.MAX_VALUE) throw new UnsupportedOperationException();
			if (w.getSampleRate() > Integer.MAX_VALUE) throw new UnsupportedOperationException();
			return (int) w.getSampleRate();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	@Override
	public int getCount() {
		try {
			long count = WavFile.openWavFile(new File(resourcePath)).getNumFrames();
			if (count > Integer.MAX_VALUE) throw new UnsupportedOperationException();
			return (int) count;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@JsonIgnore
	@Override
	public double getDuration() {
		try {
			return WavFile.openWavFile(new File(resourcePath)).getDuration();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected WaveData load() {
		try {
			if (WaveOutput.enableVerbose)
				System.out.println("WaveDataProvider: Loading " + resourcePath);

			return WaveData.load(new File(resourcePath));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
