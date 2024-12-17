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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioServer implements HttpHandler, CodeFeatures {
	public static double defaultLiveDuration = 180.0;

	private HttpServer server;

	private FrequencyCache<String, HttpAudioHandler> handlers;
	private Map<String, SharedMemoryOutputLine> lines;

	public AudioServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 20);
		handlers = new FrequencyCache<>(100, 0.6f);
		handlers.setEvictionListener((key, value) -> {
			value.destroy();
			if (lines.containsKey(key)) {
				lines.remove(key);
			}
		});

		lines = new HashMap<>();
	}

	public void start() throws IOException {
		server.createContext("/", this);
		server.start();
	}

	public BufferedAudioPlayer addLiveStream(String channel) {
		return addLiveStream(channel, Collections.emptyList());
	}

	public BufferedAudioPlayer addLiveStream(String channel, OutputLine out) {
		return addLiveStream(channel, Collections.emptyList(), out);
	}

	public BufferedAudioPlayer addLiveStream(String channel, List<String> channelNames) {
		return addLiveStream(channel, channelNames, new SharedMemoryOutputLine());
	}

	public BufferedAudioPlayer addLiveStream(String channel, List<String> channelNames,
											 OutputLine out) {
		int maxFrames = (int) (out.getSampleRate() * defaultLiveDuration);

		// Ensure that the player buffer size is a multiple
		// of the size of the OutputLine buffer
		maxFrames = maxFrames / out.getBufferSize();
		maxFrames *= out.getBufferSize();

		return addLiveStream(channel, channelNames, maxFrames, out);
	}

	public BufferedAudioPlayer addLiveStream(String channel, List<String> channelNames,
											int maxFrames, OutputLine out) {
		if (maxFrames % out.getBufferSize() != 0) {
			throw new IllegalArgumentException();
		}

		BufferedAudioPlayer player = new BufferedAudioPlayer(channelNames,
										out.getSampleRate(), maxFrames);
		addLiveStream(channel, player, out);
		return player;
	}

	public void addLiveStream(String channel, BufferedAudioPlayer player, OutputLine out) {
		BufferedOutputScheduler scheduler = player.deliver(out);
		scheduler.start();

		addStream(channel, new BufferedOutputControl(scheduler));
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

	public static void main(String[] args) throws IOException {
		AudioServer server = new AudioServer(7799);
		server.start();
		WaveData data = WaveData.load(new File("Library/organ.wav"));
		server.addStream("test", AudioProcessor.fromWave(data),
				data.getCollection().getMemLength(), data.getSampleRate());
	}
}

