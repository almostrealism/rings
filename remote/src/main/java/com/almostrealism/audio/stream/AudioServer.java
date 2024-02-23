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
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AudioProcessor;
import org.almostrealism.io.Console;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class AudioServer implements HttpHandler, CodeFeatures {
	private HttpServer server;

	private FrequencyCache<String, AudioStreamHandler> handlers;

	public AudioServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 20);
		handlers = new FrequencyCache<>(100, 0.6f);
		handlers.setEvictionListener((key, value) -> value.destroy());
	}

	public void start() throws IOException {
		server.createContext("/", this);
		server.start();
	}

	public void addStream(String channel, AudioProcessor source, int totalFrames, int sampleRate) {
		handlers.put(channel, new AudioStreamHandler(source, totalFrames, sampleRate));
	}

	public boolean containsStream(String channel) {
		return handlers.containsKey(channel);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		String channel = path.substring(1);
		AudioStreamHandler handler = handlers.get(channel);
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

