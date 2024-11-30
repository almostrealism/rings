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

import org.almostrealism.algebra.Scalar;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.graph.TimeCell;
import org.almostrealism.graph.temporal.WaveCell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;

public class BufferedAudioPlayer extends AudioPlayerBase implements CellFeatures {
	private WaveData data;
	private PackedCollection<?> loopDuration;
	private List<DoubleConsumer> timeListeners;

	private CellList cells;
	private TimeCell clock;
	private Scalar level;

	private double volume;
	private double duration;
	private boolean playing;

	private long waitTime;
	private Thread monitor;
	private boolean stopped;

	public BufferedAudioPlayer(List<String> channelNames,
							   int sampleRate, int bufferFrames) {
		super(channelNames);
		this.data = new WaveData(new PackedCollection<>(bufferFrames).traverseEach(), sampleRate);
		this.loopDuration = new PackedCollection<>(1);
		this.timeListeners = new ArrayList<>();
		this.volume = 1.0;
	}

	public synchronized void load(String file) {
		setFileString(file);
		update();

		if (monitor == null) {
			monitor = new Thread(() -> {
				while (!stopped) {
					try {
						for (DoubleConsumer listener : timeListeners) {
							listener.accept(getCurrentTime());
						}
						
						Thread.sleep(waitTime);
					} catch (Exception e) {
						warn("Error in scheduled job", e);
					}
				}
			}, "BufferedAudioPlayer Monitor");
			monitor.start();
		}
	}

	public BufferedOutputScheduler deliver(OutputLine out) {
		if (out.getSampleRate() != data.getSampleRate()) {
			throw new UnsupportedOperationException();
		}

		if (cells == null) {
			cells = w(p(loopDuration), data);
			clock = ((WaveCell) cells.get(0)).getClock();
			level = ((WaveCell) cells.get(0)).getData().amplitude();

			long bufferDuration = out.getBufferSize() * 1000L / data.getSampleRate();
			waitTime = bufferDuration / 4;
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
			duration = frames / (double) data.sampleRate();
			data.getCollection().setMem(0, result[0], 0, frames);
			log("Loaded " + frames + " frames and set duration to " + duration);
			updateLevel();
		} catch (IOException e) {
			warn("Could not load " + getFileString() + " to player", e);
		}
	}

	protected void updateLevel() {
		if (cells == null) return;

		if (playing) {
			level.setMem(volume);
			loopDuration.setMem(duration);
		} else {
			level.setMem(0.0);
			loopDuration.setMem(0.0);
		}
	}

	@Override
	public boolean play() {
		playing = true;
		updateLevel();
		return true;
	}

	@Override
	public boolean stop() {
		playing = false;
		updateLevel();
		return true;
	}

	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public boolean isReady() {
		return cells != null;
	}

	@Override
	public void setVolume(double volume) {
		this.volume = volume;
		updateLevel();
	}

	protected double getWavePosition() {
		return clock.getFrame();
	}

	protected void setWavePosition(double frames) {
		clock.setFrame(frames);
	}

	@Override
	public void seek(double time) {
		if (cells == null) {
			return;
		}

		if (time < 0.0) time = 0.0;
		if (time > getTotalDuration()) time = getTotalDuration();
		setWavePosition(time * data.getSampleRate());
	}

	@Override
	public double getCurrentTime() {
		if (cells == null) {
			return 0;
		}

		return getWavePosition() / data.getSampleRate();
	}

	@Override
	public double getTotalDuration() {
		return loopDuration.toDouble();
	}

	@Override
	public void addTimeListener(DoubleConsumer listener) {
		timeListeners.add(listener);
	}

	@Override
	public void destroy() {
		this.stopped = true;
		this.monitor = null;
		this.data.destroy();
		this.loopDuration.destroy();
	}
}
