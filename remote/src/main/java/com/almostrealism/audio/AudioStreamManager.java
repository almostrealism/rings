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

package com.almostrealism.audio;

import com.almostrealism.audio.stream.AudioServer;
import com.almostrealism.audio.stream.HttpAudioHandler;
import com.almostrealism.audio.stream.OutputLineDelegationHandler;
import com.almostrealism.audio.stream.SharedPlayerConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.BufferedAudioPlayer;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.SampleMixer;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.DelegatedOutputLine;
import org.almostrealism.audio.line.SharedMemoryOutputLine;
import org.almostrealism.io.Console;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.util.KeyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AudioStreamManager implements ConsoleFeatures {
	public static final int PORT = 7799;

	public static double defaultLiveDuration = 180.0;
	public static int defaultPlayerCount = 9;

	private Map<String, BufferedAudioPlayer> players;
	public AudioServer server;

	public AudioStreamManager() throws IOException {
		this.players = new HashMap<>();
		this.server = new AudioServer(PORT);
	}

	public void start() throws IOException {
		server.start();
	}

	public AudioServer getServer() {
		return server;
	}

	public SampleMixer getMixer(String stream) {
		BufferedAudioPlayer player = getPlayer(stream);
		if (player == null) return null;

		return player.getMixer();
	}

	public BufferedAudioPlayer getPlayer(String channel) {
		return players.get(channel);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount) {
		return addPlayer(channel, playerCount, Collections.emptyList());
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount, OutputLine out) {
		return addPlayer(channel, playerCount, Collections.emptyList(), out);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount, List<String> channelNames) {
		DelegatedOutputLine line = new DelegatedOutputLine();
		server.addStream(channel, new OutputLineDelegationHandler(line));
		return addPlayer(channel, playerCount, channelNames, line);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 List<String> channelNames,
										 OutputLine out) {
		int maxFrames = (int) (out.getSampleRate() * defaultLiveDuration);

		// Ensure that the player buffer size is a multiple
		// of the size of the OutputLine buffer
		maxFrames = maxFrames / out.getBufferSize();
		maxFrames *= out.getBufferSize();

		return addPlayer(channel, playerCount, channelNames, maxFrames, out);
	}

	public BufferedAudioPlayer addPlayer(String channel, int playerCount,
										 List<String> channelNames,
										 int maxFrames, OutputLine out) {
		if (maxFrames % out.getBufferSize() != 0) {
			throw new IllegalArgumentException();
		}

		BufferedAudioPlayer player = new BufferedAudioPlayer(playerCount, channelNames,
				out.getSampleRate(), maxFrames);
		addPlayerScheduler(channel, player, out);
		return player;
	}

	public void addPlayerScheduler(String channel, BufferedAudioPlayer player, OutputLine out) {
		BufferedOutputScheduler scheduler = addPlayer(channel, player, out);
		scheduler.start();
	}

	public BufferedOutputScheduler addPlayer(String channel, BufferedAudioPlayer player, OutputLine out) {
		players.put(channel, player);
		return player.deliver(out);
	}

	public SharedPlayerConfig addPlayer(SharedPlayerConfig config) {
		String location = config.getLocation();
		if (config.getStream() == null) {
			config.setStream(KeyUtils.generateKey());
		}

		SharedMemoryOutputLine sharedOutput = new SharedMemoryOutputLine(location);
		addPlayer(config.getStream(), defaultPlayerCount, sharedOutput);
		return config;
	}

	public HttpAudioHandler addPlayerHandler() {
		return exchange -> {
			if (Objects.equals("POST", exchange.getRequestMethod())) {
				exchange.getResponseHeaders().add("Content-Type", "application/json");
				exchange.sendResponseHeaders(200, 0);

				try (OutputStream out = exchange.getResponseBody();
						 InputStream inputStream = exchange.getRequestBody()) {
					ObjectMapper objectMapper = new ObjectMapper();
					SharedPlayerConfig config =
							objectMapper.readValue(inputStream, SharedPlayerConfig.class);
					objectMapper.writeValue(out, addPlayer(config));
				} catch (IOException e) {
					warn("Could not create player", e);
				}
			} else {
				exchange.sendResponseHeaders(405, 0);
			}
		};
	}

	@Override
	public Console console() { return AudioScene.console; }
}
