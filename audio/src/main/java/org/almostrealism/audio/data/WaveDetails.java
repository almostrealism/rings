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

import java.util.HashMap;
import java.util.Map;

public class WaveDetails {
	private String identifier;

	private int sampleRate;
	private int channelCount;
	private int frameCount;
	private PackedCollection<?> data;

	private double freqSampleRate;
	private int freqChannelCount;
	private int freqBinCount;
	private int freqFrameCount;
	private PackedCollection<?> freqData;

	private Map<String, Double> similarities;

	public WaveDetails(String identifier) {
		this(identifier, -1);
	}

	public WaveDetails(String identifier, int sampleRate) {
		this.identifier = identifier;
		this.sampleRate = sampleRate;
		this.similarities = new HashMap<>();
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

	public double getFreqSampleRate() {
		return freqSampleRate;
	}

	public void setFreqSampleRate(double freqSampleRate) {
		this.freqSampleRate = freqSampleRate;
	}

	public int getFreqChannelCount() {
		return freqChannelCount;
	}

	public void setFreqChannelCount(int freqChannelCount) {
		this.freqChannelCount = freqChannelCount;
	}

	public int getFreqBinCount() {
		return freqBinCount;
	}

	public void setFreqBinCount(int freqBinCount) {
		this.freqBinCount = freqBinCount;
	}

	public int getFreqFrameCount() {
		return freqFrameCount;
	}

	public void setFreqFrameCount(int freqFrameCount) {
		this.freqFrameCount = freqFrameCount;
	}

	public PackedCollection<?> getFreqData() {
		return freqData;
	}

	public void setFreqData(PackedCollection<?> freqData) {
		this.freqData = freqData;
	}

	public Map<String, Double> getSimilarities() {
		return similarities;
	}

	public void setSimilarities(Map<String, Double> similarities) {
		this.similarities = similarities;
	}
}
