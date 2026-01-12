/*
 * Copyright 2023 Michael Murray
 * All Rights Reserved
 */

package org.almostrealism.audio.generative;

import org.almostrealism.audio.notes.NoteAudio;
import org.almostrealism.remote.RemoteAccessKey;
import org.almostrealism.remote.RemoteGenerationProvider;
import org.almostrealism.remote.ops.GenerateRequest;
import org.almostrealism.remote.ops.RefreshRequest;
import org.almostrealism.remote.ops.RequestHistory;
import org.almostrealism.audio.line.OutputLine;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class RemoteGenerationProviderAdapter implements GenerationProvider {
	private GenerationResourceManager resources;

	private String host;
	private int port;
	private RemoteGenerationProvider remote;
	private RequestHistory history;

	private Consumer<RequestHistory> requestCallback;

	public RemoteGenerationProviderAdapter(GenerationResourceManager resources,
										   RequestHistory history,
										   Consumer<RequestHistory> requestCallback) {
		this.resources = resources;
		this.history = history;
		this.requestCallback = requestCallback;
	}

	public void setHost(String host, int port) {
		if (remote != null) {
			remote.destroy();
		}

		this.host = host;
		this.port = port;
	}

	public RequestHistory getHistory() { return history; }

	@Override
	public boolean refresh(String requestId, String generatorId, List<NoteAudio> sources) {
		ensureRemote();

		RefreshRequest request = new RefreshRequest(requestId, generatorId, sources);
		history.getRefreshRequests().add(request);
		if (requestCallback != null) requestCallback.accept(history);

		try {
			boolean success = remote.refresh(requestId, generatorId, sources);
			request.setComplete(true);
			request.setError(!success);
			if (requestCallback != null) requestCallback.accept(history);
			return success;
		} catch (Exception e) {
			request.setError(true);
			if (requestCallback != null) requestCallback.accept(history);
			return false;
		}
	}

	@Override
	public GeneratorStatus getStatus(String id) {
		ensureRemote();
		return remote.getStatus(id);
	}

	@Override
	public List<NoteAudio> generate(String requestId, String generatorId, int count) {
		ensureRemote();

		GenerateRequest request = new GenerateRequest(requestId, generatorId, count);
		history.getGenerateRequests().add(request);
		if (requestCallback != null) requestCallback.accept(history);

		try {
			List<NoteAudio> result = remote.generate(requestId, generatorId, count);
			request.setComplete(true);
			if (result == null || result.isEmpty()) throw new RuntimeException();
			if (requestCallback != null) requestCallback.accept(history);
			return result;
		} catch (Exception e) {
			request.setError(true);
			if (requestCallback != null) requestCallback.accept(history);
			return null;
		}
	}

	public void retryRefresh(BiConsumer<String, Boolean> success) {
		ensureRemote();

		history.getRefreshRequests().forEach(request -> {
			if (!request.isComplete() || request.isError()) {
				boolean result = remote.refresh(request.getRequestId(), request.getGeneratorId(), request.getSources());
				success.accept(request.getGeneratorId(), result);
				request.setComplete(true);
				request.setError(!result);
				if (requestCallback != null) requestCallback.accept(history);
			}
		});
	}

	public void retryGenerate(BiConsumer<String, List<NoteAudio>> results) {
		ensureRemote();

		history.getGenerateRequests().forEach(request -> {
			if (!request.isComplete() || request.isError()) {
				List<NoteAudio> result = remote.generate(request.getRequestId(), request.getGeneratorId(), request.getCount());
				results.accept(request.getGeneratorId(), result);
				request.setComplete(true);
				request.setError(result == null || result.isEmpty());
				if (requestCallback != null) requestCallback.accept(history);
			}
		});
	}

	private synchronized void ensureRemote() {
		if (remote == null) {
			RemoteAccessKey key = RemoteAccessKey.load("rings-key.json");
			if (key == null) {
				System.out.println("RemoteGeneratorProviderAdapter: No key found");
				return;
			}

			if (host == null) {
				host = key.getHost();
				port = key.getPort();
			}

			if (host == null || port <= 0) {
				throw new IllegalStateException("Host and port must be set before using remote provider");
			}

			try {
				System.out.println("RemoteGenerationProviderAdapter: Creating provider for " + host + ":" + port);
				remote = new RemoteGenerationProvider(host, port, key, resources);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int getSampleRate() {
		return OutputLine.sampleRate;
	}
}
