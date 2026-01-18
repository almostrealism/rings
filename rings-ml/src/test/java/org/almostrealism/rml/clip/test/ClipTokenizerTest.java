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

package org.almostrealism.rml.clip.test;

import org.almostrealism.rml.clip.ClipTokenizer;
import org.junit.Test;

import java.io.IOException;

public class ClipTokenizerTest {
	@Test
	public void tokenize() throws IOException {
		String text = "Hello, my name is bob";

		ClipTokenizer tokenizer = new ClipTokenizer("src/main/resources/openai/bpe_simple_vocab_16e6.txt");
		int[] tokens = tokenizer.encodeAsInt(text);

		for (int i = 0; i < tokens.length; i++) {
			System.out.println(tokens[i]);
		}
	}
}

