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

package com.almostrealism.remote.ops;

public class GenerateRequest {
	private String requestId;
	private String generatorId;
	private int count;
	private boolean complete;
	private boolean error;

	public GenerateRequest() { }

	public GenerateRequest(String requestId, String generatorId, int count) {
		this.requestId = requestId;
		this.generatorId = generatorId;
		this.count = count;
	}

	public String getRequestId() { return requestId; }
	public void setRequestId(String requestId) { this.requestId = requestId; }

	public String getGeneratorId() { return generatorId; }
	public void setGeneratorId(String generatorId) { this.generatorId = generatorId; }

	public int getCount() { return count; }
	public void setCount(int count) { this.count = count; }

	public boolean isComplete() { return complete; }
	public void setComplete(boolean complete) { this.complete = complete; }

	public boolean isError() { return error; }
	public void setError(boolean error) { this.error = error; }
}
