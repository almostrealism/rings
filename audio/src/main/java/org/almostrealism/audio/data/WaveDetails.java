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

package org.almostrealism.audio.data;

import org.almostrealism.collect.PackedCollection;

public class WaveDetails {
	private String identifier;

	private int sampleRate;
	private int channelCount;
	private int frameCount;
	private PackedCollection<?> data;

	private int fftSampleRate;
	private int fftChannelCount;
	private int fftFrameCount;
	private PackedCollection<?> fftData;

	public WaveDetails(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

	public int getChannelCount() {
		return channelCount;
	}

	public void setChannelCount(int channelCount) {
		this.channelCount = channelCount;
	}

	public int getFrameCount() {
		return frameCount;
	}

	public void setFrameCount(int frameCount) {
		this.frameCount = frameCount;
	}

	public PackedCollection<?> getData() {
		return data;
	}

	public void setData(PackedCollection<?> data) {
		this.data = data;
	}

	public int getFftSampleRate() {
		return fftSampleRate;
	}

	public void setFftSampleRate(int fftSampleRate) {
		this.fftSampleRate = fftSampleRate;
	}

	public int getFftChannelCount() {
		return fftChannelCount;
	}

	public void setFftChannelCount(int fftChannelCount) {
		this.fftChannelCount = fftChannelCount;
	}

	public int getFftFrameCount() {
		return fftFrameCount;
	}

	public void setFftFrameCount(int fftFrameCount) {
		this.fftFrameCount = fftFrameCount;
	}

	public PackedCollection<?> getFftData() {
		return fftData;
	}

	public void setFftData(PackedCollection<?> fftData) {
		this.fftData = fftData;
	}
}
