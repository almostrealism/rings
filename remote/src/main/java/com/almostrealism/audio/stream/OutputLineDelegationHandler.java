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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.line.DelegatedOutputLine;
import org.almostrealism.audio.line.SharedMemoryOutputLine;
import org.almostrealism.io.ConsoleFeatures;
import org.almostrealism.util.KeyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class OutputLineDelegationHandler implements HttpAudioHandler, ConsoleFeatures {
	private DelegatedOutputLine line;

	public OutputLineDelegationHandler(DelegatedOutputLine line) {
		this.line = line;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		if (Objects.equals("POST", exchange.getRequestMethod())) {
			exchange.getResponseHeaders().add("Content-Type", "application/json");

			try (OutputStream out = exchange.getResponseBody();
				 InputStream inputStream = exchange.getRequestBody()) {
				ObjectMapper objectMapper = new ObjectMapper();
				SharedPlayerConfig config = objectMapper.readValue(inputStream, SharedPlayerConfig.class);
				if (config.getStream() == null) {
					config.setStream(KeyUtils.generateKey());
				}

				OutputLine last = line.getDelegate();

				// Set up the new line
				String location = config.getLocation() + "/" + config.getStream();
				log("Initializing shared memory @ " + location);

				line.setDelegate(new SharedMemoryOutputLine(location));

				// Provide the configuration details to the client
				byte[] responseBytes = objectMapper.writeValueAsBytes(config);
				exchange.sendResponseHeaders(200, responseBytes.length);
				out.write(responseBytes);

				if (last != null) last.destroy();
			} catch (Exception e) {
				String errorMessage = "{\"error\": \"Could not update player\"}";
				exchange.sendResponseHeaders(400, errorMessage.getBytes().length);
				try (OutputStream out = exchange.getResponseBody()) {
					out.write(errorMessage.getBytes());
				}

				warn("Could not update player", e);
			}
		} else {
			exchange.sendResponseHeaders(405, 0);
		}
	}
}
