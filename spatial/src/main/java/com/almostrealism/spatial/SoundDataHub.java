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

package com.almostrealism.spatial;

import org.almostrealism.audio.AudioScene;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundDataHub implements ConsoleFeatures {
	private static ExecutorService executor = Executors.newSingleThreadExecutor();
	private static SoundDataHub current;

	private List<SoundDataListener> listeners;
	private SoundDataListener.PlayMode lastMode;

	public SoundDataHub() {
		this.listeners = new ArrayList<>();
	}

	public synchronized void addListener(SoundDataListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException();
		}

		this.listeners.add(listener);
	}

	public synchronized void removeListener(SoundDataListener listener) {
		this.listeners.remove(listener);
	}

	public synchronized void updateChannels(List<String> channels) {
		listeners.forEach(l -> l.updatedChannels(channels));
	}

	public synchronized void selected(SoundData data) {
		listeners.forEach(l -> l.selected(data));
	}

	public synchronized void published(int index, SoundData data) {
		listeners.forEach(l -> l.published(index, data));
	}

	public synchronized void setPlayMode(SoundDataListener.PlayMode mode) {
		listeners.forEach(l -> l.setPlayMode(mode));
		lastMode = mode;
	}

	public SoundDataListener.PlayMode getPlayMode() { return lastMode; }

	public synchronized void play() {
		listeners.forEach(SoundDataListener::play);
	}

	public synchronized void pause() {
		listeners.forEach(SoundDataListener::pause);
	}

	public synchronized void seek(double time) {
		listeners.forEach(l -> l.seek(time));
	}

	public synchronized void togglePlay() {
		listeners.forEach(SoundDataListener::togglePlay);
	}

	public static SoundDataHub getCurrent() {
		if (current == null) current = new SoundDataHub();

		return new SoundDataHub() {
			@Override
			public synchronized void selected(SoundData data) {
				executor.submit(() -> {
					try {
						current.selected(data);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			@Override
			public synchronized void published(int index, SoundData data) {
				executor.submit(() -> {
					try {
						current.published(index, data);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			@Override
			public synchronized void setPlayMode(SoundDataListener.PlayMode mode) {
				executor.submit(() -> current.setPlayMode(mode));
			}

			@Override
			public SoundDataListener.PlayMode getPlayMode() {
				return current.getPlayMode();
			}

			@Override
			public void addListener(SoundDataListener listener) {
				if (listener == null) {
					throw new IllegalArgumentException();
				}

				executor.submit(() -> {
							try {
								current.addListener(listener);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
				);
			}

			@Override
			public void removeListener(SoundDataListener listener) {
				executor.submit(() -> current.removeListener(listener));
			}

			@Override
			public void updateChannels(List<String> channels) {
				executor.submit(() -> current.updateChannels(channels));
			}

			@Override
			public void play() {
				executor.submit(() -> current.play());
			}

			@Override
			public void pause() {
				executor.submit(() -> current.pause());
			}

			public void seek(double time) {
				executor.submit(() -> current.seek(time));
			}

			@Override
			public void togglePlay() {
				executor.submit(() -> current.togglePlay());
			}
		};
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}
}
