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

package com.almostrealism.remote;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.almostrealism.audio.generative.GenerationProvider;
import org.almostrealism.audioml.DiffusionGenerationProvider;
import org.almostrealism.audio.generative.LocalResourceManager;

import java.io.File;
import java.io.IOException;

public class RemoteGenerationServer {
	public static final int DEFAULT_PORT = 6565;

	private Server server;

	public RemoteGenerationServer(GenerationProvider provider) {
		this(provider, DEFAULT_PORT);
	}

	public RemoteGenerationServer(GenerationProvider provider, int port) {
		this(provider, ServerBuilder.forPort(port));
	}

	public RemoteGenerationServer(GenerationProvider provider, ServerBuilder<?> serverBuilder) {
		this.server = serverBuilder.addService(new RemoteGenerationService(provider)).build();
	}

	public void start() throws IOException {
		server.start();
		System.out.println("RemoteGenerationServer: Started");
	}

	public static void main(String args[]) {
		String root = args[0];

		GenerationProvider provider = new DiffusionGenerationProvider(
				new LocalResourceManager(
							new File(root + "remote-models"),
							new File(root + "remote-audio")));
		RemoteGenerationServer server = new RemoteGenerationServer(provider);

		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
