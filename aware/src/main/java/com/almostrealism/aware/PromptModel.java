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

package com.almostrealism.aware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class PromptModel implements Model {
	public static final int PARALLEL = 4;

	private static final String DELEGATION = "You have " + PARALLEL +
			" employees. Write one paragraph to each of them explaining what they will be responsible for.";

	protected abstract String generate(String input);

	@Override
	public List<Task> subtasks(Task task) {
		StringBuffer prompt = new StringBuffer();
		prompt.append(task.getPrompt());
		prompt.append(" ");
		prompt.append(DELEGATION);
		prompt.append("\n");

		List<Task> subtasks = new ArrayList<>();

		String result = generate(prompt.toString());
		System.out.println("Response: ");
		System.out.println(result);
		System.out.println("------");

		Extract extract = new Extract(null, result.trim());

		w: while (!extract.isLast()) {
			extract = extractNext(extract.getRemaining(), List.of(":", "-"), "\n");
			if (extract == null) break w;

			subtasks.add(new Task(extract.getValue()));
		}

		return subtasks;
	}

	private Extract extractNext(String input, List<String> start, String end) {
		int startIdx = start.stream().mapToInt(s -> input.indexOf(s) + 1).filter(i -> i > 0).min().orElse(-1);
		if (startIdx == -1) {
			return null;
		}

		while (input.charAt(startIdx) == end.charAt(0)) {
			startIdx++;
		}

		int endIdx = input.indexOf(end, startIdx);

		if (endIdx == -1) {
			return new Extract(input.substring(startIdx).trim(), null);
		} else {
			return new Extract(input.substring(startIdx, endIdx).trim(), input.substring(endIdx).trim());
		}
	}

	private class Extract {
		private String value;
		private String remaining;

		public Extract(String value, String remaining) {
			this.value = value;
			this.remaining = remaining;
		}

		public String getValue() { return value; }
		public String getRemaining() { return remaining; }

		public boolean isLast() {
			return remaining == null;
		}
	}
}
