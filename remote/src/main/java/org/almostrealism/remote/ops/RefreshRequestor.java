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

package org.almostrealism.remote.ops;

import io.grpc.stub.StreamObserver;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.remote.RemoteAccessKey;
import org.almostrealism.remote.api.Generation;
import org.almostrealism.remote.api.GeneratorGrpc;
import org.almostrealism.util.KeyUtils;

import java.util.List;

public class RefreshRequestor implements StreamObserver<Generation.Status> {
	private final RemoteAccessKey key;
	private final GeneratorGrpc.GeneratorStub generator;
	private StreamObserver<Generation.RefreshRequest> requestStream;
	private final Runnable end;

	private final WaveDataPublisher publisher;
	private final Receiver deliver;

	public RefreshRequestor(RemoteAccessKey key, GeneratorGrpc.GeneratorStub generator, Receiver deliver, Runnable end) {
		this.key = key;
		this.generator = generator;
		this.end = end;

		this.deliver = deliver;
		this.publisher = new WaveDataPublisher();
	}

	public void submit(String requestId, String generatorId, List<WaveData> sources) {
		ensureRequestStream();

		System.out.println("RefreshRequestor: Publishing " + sources.size() + " source waves");
		for (int i = 0; i < sources.size(); i++) {
			WaveData source = sources.get(i);
			boolean last = i == sources.size() - 1;

			publisher.publish(source, audio -> {
				Generation.SourceData data = Generation.SourceData.newBuilder()
						.setName("") // TODO
						.setSourceId(KeyUtils.generateKey()) // TODO
						.setSegment(audio)
						.build();

				Generation.RefreshRequest request = Generation.RefreshRequest.newBuilder()
						.setAccessKey(key())
						.setRequestId(requestId)
						.setGeneratorId(generatorId)
						.setSource(data)
						.setIsFinal(last && audio.getIsFinal())
						.build();

				if (requestStream != null)
					requestStream.onNext(request);
			});
		}

		System.out.println("RefreshRequestor: Done publishing source waves");
	}

	protected Generation.AccessKey key() {
		return Generation.AccessKey.newBuilder()
				.setKey(key.getKey())
				.setUserId(key.getUserId())
				.setToken(key.getToken())
				.build();
	}

	protected synchronized void ensureRequestStream() {
		if (requestStream == null) {
			System.out.println("RefreshRequestor: Creating request stream...");
			requestStream = generator.refresh(this);
			System.out.println("RefreshRequestor: Request stream created");
		}
	}

	@Override
	public void onNext(Generation.Status output) {
		if (output.getState() == Generation.State.FINISHED) {
			System.out.println("RefreshRequestor: Refresh completed");
			deliver.receive(output.getRequestId(), true);
		} else if (output.getState() == Generation.State.FAILED){
			System.out.println("RefreshRequestor: Refresh failed");
			deliver.receive(output.getRequestId(), false);
		}
	}

	@Override
	public void onError(Throwable e) {
		e.printStackTrace();
		requestStream = null;
		System.out.println("RefreshRequestor: Running end callback");
		end.run();
	}

	@Override
	public void onCompleted() {
		System.out.println("RefreshRequestor: Completed");
		requestStream.onCompleted();
		requestStream = null;
		end.run();
	}

	public void destroy() {
		if (requestStream != null) {
			requestStream.onCompleted();
			requestStream = null;
		}
	}

	public interface Receiver {
		void receive(String requestId, boolean success);
	}
}
