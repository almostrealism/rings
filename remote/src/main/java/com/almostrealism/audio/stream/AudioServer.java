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
import org.almostrealism.audio.data.WaveData;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class AudioServer {
	private WaveData audioData;

	public AudioServer(WaveData audioData) {
		this.audioData = audioData;
	}

	public void startServer() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.createContext("/audio", new HttpHandler() {
			@Override
			public void handle(HttpExchange exchange) throws IOException {
				exchange.getResponseHeaders().add("Content-Type", "audio/wav");
//				TODO  Create and stream WavFile
//				exchange.sendResponseHeaders(200, audioData.length);
//				OutputStream os = exchange.getResponseBody();
//				os.write(audioData);
//				os.close();
			}
		});
		server.start();

		System.out.println("Server is listening on port 8000");
	}

	public static void main(String[] args) throws IOException {
		AudioServer server = new AudioServer(WaveData.load(new File("Library/organ.wav")));
		server.startServer();
	}
}

