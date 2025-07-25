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
	private boolean silent;
	private boolean persistent;

	private double freqSampleRate;
	private int freqChannelCount;
	private int freqBinCount;
	private int freqFrameCount;
	private PackedCollection<?> freqData;

	private double featureSampleRate;
	private int featureChannelCount;
	private int featureBinCount;
	private int featureFrameCount;
	private PackedCollection<?> featureData;

	private Map<String, Double> similarities;

	public WaveDetails() {
		this(null);
	}

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

	public boolean isSilent() { return silent; }
	public void setSilent(boolean silent) { this.silent = silent; }

	public boolean isPersistent() { return persistent; }
	public void setPersistent(boolean persistent) {
		this.persistent = persistent;
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

	public double getFeatureSampleRate() {
		return featureSampleRate;
	}

	public void setFeatureSampleRate(double featureSampleRate) {
		this.featureSampleRate = featureSampleRate;
	}

	public int getFeatureChannelCount() {
		return featureChannelCount;
	}

	public void setFeatureChannelCount(int featureChannelCount) {
		this.featureChannelCount = featureChannelCount;
	}

	public int getFeatureBinCount() {
		return featureBinCount;
	}

	public void setFeatureBinCount(int featureBinCount) {
		this.featureBinCount = featureBinCount;
	}

	public int getFeatureFrameCount() {
		return featureFrameCount;
	}

	public void setFeatureFrameCount(int featureFrameCount) {
		this.featureFrameCount = featureFrameCount;
	}

	public PackedCollection<?> getFeatureData() {
		return featureData;
	}

	public void setFeatureData(PackedCollection<?> featureData) {
		this.featureData = featureData;
	}

	public Map<String, Double> getSimilarities() {
		return similarities;
	}

	public void setSimilarities(Map<String, Double> similarities) {
		this.similarities = similarities;
	}
}
