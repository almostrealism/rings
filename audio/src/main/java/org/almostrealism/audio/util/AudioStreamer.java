/*
 * Copyright 2023 Michael Murray
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

package org.almostrealism.audio.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class AudioStreamer {
	public static void main(String[] args) throws Exception {
		while (true) {
			try {
				Socket socket = new Socket("localhost", 6780);
				DataInputStream din = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				System.out.println("Established stream...");

				int frames = 100;
				long total = 0;

				while (true) {
					int count = din.readInt();

					for (int i = 0; i < count; i++) {
						double value = Math.sin(2 * Math.PI * (total / (double) frames));
						dos.writeFloat((float) (0.1 * value));
						total = total % frames + 1;
					}

					dos.flush();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
				Thread.sleep(10000);
			}
		}
	}

	public static void server(String[] args) throws Exception {
		ServerSocket serverSocket = new ServerSocket(6780);

		while (true) {
			Socket clientSocket = serverSocket.accept();
			DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());

			Random random = new Random();
			for (int i = 0; i < 1024; i++) {
				float value = random.nextFloat();
				dos.writeFloat(value);
			}

			dos.flush();
			dos.close();
			clientSocket.close();
		}
	}
}

