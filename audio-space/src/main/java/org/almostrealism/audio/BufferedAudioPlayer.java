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

package org.almostrealism.audio;

import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.temporal.WaveCell;
import org.almostrealism.graph.temporal.WaveCellData;

import java.io.IOException;
import java.util.List;
import java.util.function.DoubleConsumer;

public class BufferedAudioPlayer extends AudioPlayerBase implements CellFeatures {
	private WaveData data;
	private PackedCollection<?> loopDuration;

	private CellList cells;

	public BufferedAudioPlayer(List<String> channelNames,
							   int sampleRate, int bufferFrames) {
		super(channelNames);
		this.data = new WaveData(new PackedCollection<>(bufferFrames).traverseEach(), sampleRate);
		this.loopDuration = new PackedCollection<>(1);
	}

	public void load(String file) {
		setFileString(file);
		update();
	}

	public BufferedOutputScheduler deliver(OutputLine out) {
		if (out.getSampleRate() != data.getSampleRate()) {
			throw new UnsupportedOperationException();
		}

		if (cells == null) {
			cells = w(p(loopDuration), data);
		} else {
			warn("Attempting to deliver to an already active player");
		}

		return cells.buffer(out);
	}

	protected void update() {
		try (WavFile in = WavFile.openWavFile(getFile())) {
			long inRate = in.getSampleRate();

			if (inRate != data.getSampleRate()) {
				warn("Sample rate " + inRate + " != " + data.getSampleRate());
				return;
			}

			// TODO  Merge channels
			double result[][] = new double[in.getNumChannels()][(int) in.getFramesRemaining()];
			in.readFrames(result, (int) in.getFramesRemaining());

			int frames = Math.min(result[0].length, this.data.getCollection().getMemLength());
			double duration = frames / (double) data.sampleRate();
			data.getCollection().setMem(0, result[0], 0, frames);
			loopDuration.setMem(duration);
		} catch (IOException e) {
			warn("Could not load " + getFileString() + " to player", e);
		}
	}

	@Override
	public boolean play() {
		return true;
	}

	@Override
	public boolean stop() {
		return false;
	}

	@Override
	public boolean isPlaying() {
		return true;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setVolume(double volume) {

	}

	@Override
	public void seek(double time) {

	}

	@Override
	public double getCurrentTime() {
		if (cells == null) {
			return 0;
		}

		WaveCellData wd = ((WaveCell) cells.get(0)).getData();
		double frames = wd.waveIndex().toDouble() + wd.wavePosition().toDouble();
		return frames / data.getSampleRate();
	}

	@Override
	public double getTotalDuration() {
		return loopDuration.toDouble();
	}

	@Override
	public void addTimeListener(DoubleConsumer listener) {

	}

	@Override
	public void destroy() {
		this.data.destroy();
		this.loopDuration.destroy();
	}
}
