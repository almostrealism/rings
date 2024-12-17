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

package com.almostrealism.audio.stream;

import org.almostrealism.audio.BufferedAudioPlayer;
import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.line.SharedMemoryOutputLine;

import java.io.IOException;

public class AudioSharedMemory implements CellFeatures {
	public static void main(String args[]) throws InterruptedException, IOException {
		new AudioSharedMemory().run();
	}

	public void run() throws IOException, InterruptedException {
		boolean alt = false;

		SharedMemoryOutputLine out = new SharedMemoryOutputLine();
		AudioServer server = new AudioServer(7799);
		BufferedAudioPlayer player = server.addLiveStream("live", out);
		player.load("Library/RAW_IU_ARCHE_B.wav");

		server.start();
		player.play();
		System.out.println("Server started");

		while (true) {
			Thread.sleep(2000);
//			log("Read position = " + out.getReadPosition());
//			log("Read position / 256 = " + out.getReadPosition() / 256 + " (" + out.getReadPosition() % 256 + ")");
//			alt = !alt;
//			player.load(alt ? "Library/RAW_IU_TOP_15.wav" : "Library/RAW_IU_ARCHE_B.wav");
		}
	}
}
