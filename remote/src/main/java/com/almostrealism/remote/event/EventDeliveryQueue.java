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

package com.almostrealism.remote.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.almostrealism.io.ConsoleFeatures;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class EventDeliveryQueue<T extends AbstractEvent> implements ConsoleFeatures {
	private ConcurrentLinkedQueue<T> events;
	private ScheduledExecutorService executor;
	private ObjectMapper mapper;
	private String deliveryUri;

	public EventDeliveryQueue(String deliveryUri) {
		this.events = new ConcurrentLinkedQueue<>();
		this.executor = Executors.newScheduledThreadPool(1);
		this.mapper = new ObjectMapper();
		this.deliveryUri = deliveryUri;
	}

	public void start() {
		executor.scheduleAtFixedRate(this::deliverAll, 10,
				10, java.util.concurrent.TimeUnit.SECONDS);
	}

	public void addEvent(T e) {
		events.add(e);
	}

	protected void deliverAll() {
		while (deliver(events.poll()));
	}

	protected boolean deliver(T event) {
		if (event == null) {
			return false;
		}

		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(deliveryUri))
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(event)))
					.build();
			HttpResponse<String> response = HttpClient.newHttpClient()
					.send(request, HttpResponse.BodyHandlers.ofString());

			log("Sent " + event.getName() + " event (" + response.statusCode() + ")");
		} catch (Exception e) {
			warn(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
		}

		return true;
	}
}
