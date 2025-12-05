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

package org.almostrealism.rml.clip;

import org.almostrealism.ml.Tokenizer;
import org.almostrealism.rml.Bpe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClipTokenizer implements Tokenizer {

	private final Map<Integer, String> byteEncoder;
	private final Map<String, Integer> encoder;
	private final Map<List<String>, Integer> bpeRanks;
	private final Map<String, String> cache;
	private final Pattern pat;

	public ClipTokenizer(String bpePath) throws IOException {
		byteEncoder = Bpe.bytesToUnicode();
		encoder = new HashMap<>();
		bpeRanks = new HashMap<>();
		cache = new HashMap<>();
		cache.put("<|startoftext|>", "<|startoftext|>");
		cache.put("<|endoftext|>", "<|endoftext|>");

		pat = Pattern.compile("<\\|startoftext\\|>|<\\|endoftext\\|>|'s|'t|'re|'ve|'m|'ll|'d|[^\\s]+", Pattern.CASE_INSENSITIVE);

		List<String> vocab = loadVocab(bpePath, 1, 49152-256-2+1);

		for(int i = 0; i < vocab.size(); i++) {
			bpeRanks.put(Arrays.asList(vocab.get(i).split("")), i);
			encoder.put(vocab.get(i), i);
		}
	}

	public String bpe(String token) {
		if (cache.containsKey(token)) {
			return cache.get(token);
		}

		List<String> word = new ArrayList<>(Arrays.asList(token.substring(0, token.length() - 1).split("")));
		word.add(token.substring(token.length() - 1) + "</w>");

		Set<List<String>> pairs = Bpe.getPairs(word);

		if (pairs.isEmpty()) {
			return token + "</w>";
		}

		while (true) {
			List<String> bigram = Collections.min(pairs, Comparator.comparingInt(pair -> bpeRanks.getOrDefault(pair, Integer.MAX_VALUE)));
			if (!bpeRanks.containsKey(bigram)) {
				break;
			}

			String first = bigram.get(0), second = bigram.get(1);
			List<String> newWord = new ArrayList<>();
			int i = 0;
			while (i < word.size()) {
				int j = word.subList(i, word.size()).indexOf(first);
				if (j == -1) {
					newWord.addAll(word.subList(i, word.size()));
					break;
				}
				newWord.addAll(word.subList(i, i + j));
				i += j;

				if (word.get(i).equals(first) && i < word.size() - 1 && word.get(i + 1).equals(second)) {
					newWord.add(first + second);
					i += 2;
				} else {
					newWord.add(word.get(i));
					i += 1;
				}
			}
			word = newWord;
			if (word.size() == 1) {
				break;
			}
			pairs = Bpe.getPairs(word);
		}

		String wordStr = String.join(" ", word);
		cache.put(token, wordStr);
		return wordStr;
	}

	@Override
	public int[] encodeAsInt(String text) {
		List<Integer> bpeTokens = new ArrayList<>();
		text = Bpe.whitespaceClean(text.toLowerCase());

		Matcher m = pat.matcher(text);

		while (m.find()) {
			String token = m.group();
			// Convert token to UTF-8 and use byteEncoder
			byte[] utf8Bytes = token.getBytes(StandardCharsets.UTF_8);
			StringBuilder sb = new StringBuilder();
			for (byte b : utf8Bytes) {
				sb.append(byteEncoder.get((int) b));
			}
			token = sb.toString();

			for (String bpeToken : bpe(token).split(" ")) {
				bpeTokens.add(encoder.get(bpeToken));
			}
		}

		if (bpeTokens.size() > 75) {
			bpeTokens = bpeTokens.subList(0, 75);
		}

		bpeTokens.add(0, 49406);
		while (bpeTokens.size() < 77) {
			bpeTokens.add(49407);
		}

		return bpeTokens.stream().mapToInt(Integer::intValue).toArray();
	}

	@Override
	public String decodeAsInt(int[] tokens) {
		throw new UnsupportedOperationException();
	}

	protected static List<String> loadVocab(String filePath, int start, int end) throws IOException {
		return Files.lines(Paths.get(filePath))
				.map(line -> line.trim() + "</w>")
				.skip(start)
				.limit(end - start)
				.collect(Collectors.toList());
	}
}
