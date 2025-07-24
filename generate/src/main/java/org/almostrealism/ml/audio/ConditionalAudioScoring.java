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

package org.almostrealism.ml.audio;

import ai.onnxruntime.OrtException;
import io.almostrealism.collect.TraversalPolicy;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.collect.PackedCollection;
import org.almostrealism.io.Console;
import org.almostrealism.ml.StateDictionary;
import org.almostrealism.persistence.Asset;
import org.almostrealism.persistence.AssetGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConditionalAudioScoring extends ConditionalAudioSystem {

	public ConditionalAudioScoring(String modelsPath) throws OrtException, IOException {
		this(new AssetGroup(new Asset(new File(modelsPath + "/conditioners.onnx")),
						new Asset(new File(modelsPath + "/encoder.onnx")),
						new Asset(new File(modelsPath + "/decoder.onnx")),
						new Asset(new File(modelsPath + "/dit.onnx"))),
				new StateDictionary(modelsPath + "/weights"));
	}

	public ConditionalAudioScoring(AssetGroup onnxAssets, StateDictionary ditStates)
			throws OrtException, IOException {
		super(onnxAssets, ditStates, true);
	}

	public double computeScore(String prompt, WaveData audio) {
		return computeScore(tokenize(prompt), audio);
	}

	public double computeScore(long promptTokenIds[], WaveData audio) {
		return computeScore(promptTokenIds, audio.getData(), audio.getDuration());
	}

	public double computeScore(long promptTokenIds[], PackedCollection<?> audio, double duration) {
		// 1. Process tokens through conditioners
		Map<String, PackedCollection<?>> conditionerOutputs = runConditioners(promptTokenIds, duration);

		// 2. Get audio latent
		PackedCollection<?> audioLatent = getAutoencoder().encode(cp(audio)).evaluate();

		// 3. Run forward pass with attention extraction
		PackedCollection<?> output = getDitModel().forward(
				audioLatent, pack(0.0),
				conditionerOutputs.get("cross_attention_input"),
				conditionerOutputs.get("global_cond"));

		// 4. Extract and aggregate attention weights
		Map<Integer, PackedCollection<?>> activations = getDitModel().getAttentionActivations();
		Map<Integer, Double> scores = computeAttentionLayerScores(activations);
		log("First score: " + scores.get(0) + ", Last score: " + scores.get(scores.size() - 1));
		return scores.values().stream().mapToDouble(v -> v).average().orElse(0.0);
	}

	public Map<Integer, Double> computeAttentionLayerScores(Map<Integer, PackedCollection<?>> attentionWeights) {
		Map<Integer, Double> layerScores = new HashMap<>();
		attentionWeights.forEach((layer, weights) -> {
			// Attention weights shape: [batch, num_heads, seq_len_audio, seq_len_text]
			// Aggregate by taking mean across heads and max across audio sequence
			layerScores.put(layer, computeLayerAttentionScore(weights));
		});
		return layerScores;
	}

	private double computeLayerAttentionScore(PackedCollection<?> attentionWeights) {
		TraversalPolicy shape = attentionWeights.getShape();
		double[] weights = attentionWeights.toArray();
		
		// Assuming shape [batch, num_heads, seq_len_audio, seq_len_text]
		int batch = shape.length(0);
		int numHeads = shape.length(1);
		int seqLenAudio = shape.length(2);
		int seqLenText = shape.length(3);
		
		double maxAttention = 0.0;
		double sumAttention = 0.0;
		int total = 0;
		
		for (int b = 0; b < batch; b++) {
			for (int h = 0; h < numHeads; h++) {
				for (int a = 0; a < seqLenAudio; a++) {
					double audioPositionMax = 0.0;
					for (int t = 0; t < seqLenText; t++) {
						int idx = b * numHeads * seqLenAudio * seqLenText +
									h * seqLenAudio * seqLenText +
									a * seqLenText + t;
						audioPositionMax = Math.max(audioPositionMax, weights[idx]);
						sumAttention += weights[idx];
						total++;
					}
					maxAttention = Math.max(maxAttention, audioPositionMax);
				}
			}
		}
		
//		return maxAttention;
		return sumAttention / total;
	}

	public static void main(String args[]) throws IOException, OrtException {
		if (args.length < 3) {
			System.out.println("Usage: java AudioGenerator <models_path> <prompt> <input_file> [additional_inputs...]");
			return;
		}

		String modelsPath = args[0];
		String prompt = args[1];

		List<String> inputs = new ArrayList<>();
		for (int i = 2; i < args.length; i++) {
			inputs.add(args[i]);
		}

		try (ConditionalAudioScoring scoring = new ConditionalAudioScoring(modelsPath)) {
			for (String in : inputs) {
				try (WaveData wave = WaveData.load(new File(in))) {
					if (wave.getSampleRate() != scoring.getAutoencoder().getSampleRate()) {
						throw new IllegalArgumentException();
					}

					Console.root().features(ConditionalAudioScoring.class)
							.log(in + " -> score = " + scoring.computeScore(prompt, wave));
				}
			}
		}
	}
}
