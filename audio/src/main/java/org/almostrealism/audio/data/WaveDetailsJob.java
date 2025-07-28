/*
 * Copyright 2025 Michael Murray
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

package org.almostrealism.audio.data;

import java.util.concurrent.CompletableFuture;

public class WaveDetailsJob {
	private WaveDataProvider target;
	private boolean persistent;
	private double priority;

	private CompletableFuture<WaveDetails> future;

	public WaveDetailsJob(WaveDataProvider target, boolean persistent,
						  double priority) {
		this.target = target;
		this.persistent = persistent;
		this.priority = priority;
		this.future = new CompletableFuture<>();
	}

	public WaveDataProvider getTarget() { return target; }
	public boolean isPersistent() { return persistent; }

	public void setPriority(double priority) { this.priority = priority;}
	public double getPriority() { return priority; }

	public void complete(WaveDetails details) {
		future.complete(details);
	}

	public CompletableFuture<WaveDetails> getFuture() { return future; }

	public void await() { future.join(); }
}
