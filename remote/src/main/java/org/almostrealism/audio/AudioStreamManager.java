/*
 * Copyright 2025 Michael Murray
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

import org.almostrealism.audio.line.AudioLine;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.DelegatedAudioLine;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.stream.AudioLineDelegationHandler;
import org.almostrealism.audio.stream.AudioServer;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AudioStreamManager implements ConsoleFeatures {
	public static final int PORT = 7799;

	public static double defaultLiveDuration = 180.0;

	private final Map<String, BufferedAudioPlayer> players;
	public AudioServer server;

	public AudioStreamManager() throws IOException {
		this.players = new HashMap<>();
		this.server = new AudioServer(PORT);
	}

	public void start() throws IOException { server.start(); }

	public AudioServer getServer() { return server; }

	public SampleMixer getMixer(String stream) {
		BufferedAudioPlayer player = getPlayer(stream);
		if (player == null) return null;

		return player.getMixer();
	}

	public BufferedAudioPlayer getPlayer(String channel) {
		return players.get(channel);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 OutputLine inputRecord) {
		DelegatedAudioLine line = new DelegatedAudioLine();
		server.addStream(channel, new AudioLineDelegationHandler(line));
		return addPlayer(channel, playerCount, line, inputRecord);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 AudioLine out, OutputLine inputRecord) {
		int maxFrames = (int) (out.getSampleRate() * defaultLiveDuration);

		// Ensure that the player buffer size is a multiple
		// of the size of the OutputLine buffer
		maxFrames = maxFrames / out.getBufferSize();
		maxFrames *= out.getBufferSize();

		return addPlayer(channel, playerCount, maxFrames, out, inputRecord);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 int maxFrames,
										 AudioLine out, OutputLine inputRecord) {
		if (maxFrames % out.getBufferSize() != 0) {
			throw new IllegalArgumentException();
		}

		BufferedAudioPlayer player = new BufferedAudioPlayer(playerCount, out.getSampleRate(), maxFrames);
		addPlayerScheduler(channel, player, out, inputRecord);
		return player;
	}

	public void addPlayerScheduler(String channel, BufferedAudioPlayer player,
								   AudioLine out, OutputLine inputRecord) {
		BufferedOutputScheduler scheduler = addPlayer(channel, player, out, inputRecord);
		scheduler.start();
	}

	public BufferedOutputScheduler addPlayer(String channel,
											 BufferedAudioPlayer player,
											 AudioLine out,
											 OutputLine inputRecord) {
		players.put(channel, player);
		return player.deliver(out, inputRecord);
	}

	@Override
	public Console console() { return AudioScene.console; }
}
