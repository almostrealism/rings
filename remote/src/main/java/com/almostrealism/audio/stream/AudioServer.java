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

import com.sun.net.httpserver.HttpServer;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.filter.AudioProcessor;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class AudioServer {
	private HttpServer server;

	public AudioServer(int port) throws IOException {
		server = HttpServer.create(new InetSocketAddress(port), 0);
	}

	public void start() throws IOException {
		server.start();
	}

	public void addStream(String channel, AudioProcessor source, int totalFrames, int sampleRate) {
		server.createContext("/" + channel, new AudioStreamHandler(source, totalFrames, sampleRate));
	}

	public static void main(String[] args) throws IOException {
		AudioServer server = new AudioServer(7799);
		server.start();
		WaveData data = WaveData.load(new File("Library/organ.wav"));
		server.addStream("test", AudioProcessor.fromWave(data),
				data.getCollection().getMemLength(), data.getSampleRate());
	}
}

