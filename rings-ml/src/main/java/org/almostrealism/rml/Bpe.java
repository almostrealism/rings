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

package org.almostrealism.rml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Bpe {

	public static Set<List<String>> getPairs(List<String> word) {
		Set<List<String>> pairs = new HashSet<>();
		String prevChar = word.get(0);
		for (int i = 1; i < word.size(); i++) {
			String c = word.get(i);
			pairs.add(Arrays.asList(prevChar, c));
			prevChar = c;
		}
		return pairs;
	}

	public static Map<Integer, String> bytesToUnicode() {
		List<Integer> bs = new ArrayList<>();
		for (int i = 33; i <= 126; i++) {
			bs.add(i);
		}
		for (int i = 161; i <= 172; i++) {
			bs.add(i);
		}
		for (int i = 174; i <= 255; i++) {
			bs.add(i);
		}

		List<String> cs = new ArrayList<>();
		for (Integer b : bs) {
			cs.add(Character.toString((char) b.intValue()));
		}

		int n = 0;
		for (int b = 0; b < 256; b++) {
			if (!bs.contains(b)) {
				bs.add(b);
				cs.add(Character.toString((char) (n + 65536)));
				n++;
			}
		}

		Map<Integer, String> map = new HashMap<>();
		for (int i = 0; i < bs.size(); i++) {
			map.put(bs.get(i), cs.get(i));
		}
		return map;
	}

	public static String whitespaceClean(String text) {
		return text.replaceAll("\\s+", " ").trim();
	}
}
