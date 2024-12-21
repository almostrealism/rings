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

package com.almostrealism.audio.stream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.almostrealism.util.FrequencyCache;
import org.almostrealism.CodeFeatures;
import org.almostrealism.audio.AudioScene;
import org.almostrealism.audio.BufferedAudioPlayer;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AudioProcessor;
import org.almostrealism.audio.line.BufferedOutputScheduler;
import org.almostrealism.audio.line.SharedMemoryOutputLine;
import org.almostrealism.io.Console;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioServer implements HttpHandler, CodeFeatures {
	public static double defaultLiveDuration = 180.0;

	private HttpServer server;

	private FrequencyCache<String, HttpAudioHandler> handlers;
	private Map<String, SharedMemoryOutputLine> lines;
	private Map<String, BufferedAudioPlayer> players;

	public AudioServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 20);
		handlers = new FrequencyCache<>(100, 0.6f);
		handlers.setEvictionListener((key, value) -> {
			value.destroy();
		});

		lines = new HashMap<>();
		players = new HashMap<>();
	}

	public void start() throws IOException {
		server.createContext("/", this);
		server.start();
	}

	public int getPort() { return server.getAddress().getPort();}

	public BufferedAudioPlayer addLiveStream(String channel, int playerCount) {
		return addLiveStream(channel, playerCount, Collections.emptyList());
	}

	public BufferedAudioPlayer addLiveStream(String channel, int playerCount, OutputLine out) {
		return addLiveStream(channel, playerCount, Collections.emptyList(), out);
	}

	public BufferedAudioPlayer addLiveStream(String channel, int playerCount, List<String> channelNames) {
		return addLiveStream(channel, playerCount, channelNames, new SharedMemoryOutputLine());
	}

	public BufferedAudioPlayer addLiveStream(String channel, int playerCount,
											 List<String> channelNames,
											 OutputLine out) {
		int maxFrames = (int) (out.getSampleRate() * defaultLiveDuration);

		// Ensure that the player buffer size is a multiple
		// of the size of the OutputLine buffer
		maxFrames = maxFrames / out.getBufferSize();
		maxFrames *= out.getBufferSize();

		return addLiveStream(channel, playerCount, channelNames, maxFrames, out);
	}

	public BufferedAudioPlayer addLiveStream(String channel, int playerCount, List<String> channelNames,
											 int maxFrames, OutputLine out) {
		if (maxFrames % out.getBufferSize() != 0) {
			throw new IllegalArgumentException();
		}

		BufferedAudioPlayer player = new BufferedAudioPlayer(playerCount, channelNames,
													out.getSampleRate(), maxFrames);
		addLiveStream(channel, player, out);
		return player;
	}

	public void addLiveStream(String channel, BufferedAudioPlayer player, OutputLine out) {
		BufferedOutputScheduler scheduler = addPlayer(channel, player, out);
		scheduler.start();

		addStream(channel, new BufferedOutputControl(scheduler));
	}

	public BufferedOutputScheduler addPlayer(String channel, BufferedAudioPlayer player, OutputLine out) {
		players.put(channel, player);
		return player.deliver(out);
	}

	public String addStream(String key, WaveData data) {
		key = Base64.getEncoder().encodeToString(key.getBytes());

		if (containsStream(key)) {
			return key;
		}

		addStream(key, AudioProcessor.fromWave(data),
				data.getCollection().getMemLength(), data.getSampleRate());
		return key;
	}

	public void addStream(String channel, AudioProcessor source,
						  	int totalFrames, int sampleRate) {
		addStream(channel, new AudioStreamHandler(source, totalFrames, sampleRate));
	}

	public void addStream(String channel, HttpAudioHandler handler) {
		if (containsStream(channel)) {
			throw new IllegalArgumentException("Stream already exists");
		}

		handlers.put(channel, handler);
	}

	public HttpAudioHandler getStream(String channel) {
		return handlers.get(channel);
	}

	public BufferedAudioPlayer getPlayer(String channel) {
		return players.get(channel);
	}

	public boolean containsStream(String channel) {
		return handlers.containsKey(channel);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String channel = path.substring(1);
		HttpHandler handler = handlers.get(channel);
		if (handler == null) {
			exchange.sendResponseHeaders(404, 0);
			exchange.getResponseBody().close();
			return;
		}

		handler.handle(exchange);
	}

	@Override
	public Console console() {
		return AudioScene.console;
	}
}

