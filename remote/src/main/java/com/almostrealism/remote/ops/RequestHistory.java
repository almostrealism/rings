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

import java.util.ArrayList;
import java.util.List;

public class RequestHistory {

	private List<GenerateRequest> generateRequests;

	public RequestHistory() {
		generateRequests = new ArrayList<>();
	}

	public List<GenerateRequest> getGenerateRequests() {
		return generateRequests;
	}

	public void setGenerateRequests(List<GenerateRequest> generateRequests) {
		this.generateRequests = generateRequests;
	}

	public boolean anyCompleted(String generatorId) {
		for (GenerateRequest request : generateRequests) {
			if (request.getGeneratorId().equals(generatorId) && request.isComplete()) {
				return true;
			}
		}

		return false;
	}

	public boolean anyRefreshing(String generatorId) {
		// TODO
		return false;
	}

	public boolean anyGenerating(String generatorId) {
		for (GenerateRequest request : generateRequests) {
			if (request.getGeneratorId().equals(generatorId) && !request.isComplete()) {
				return true;
			}
		}

		return false;
	}
}