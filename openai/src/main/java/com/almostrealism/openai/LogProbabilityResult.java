package com.almostrealism.openai;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class LogProbabilityResult {
	List<String> tokens;
	List<Double> tokenLogprobs;
	List<Map<String, Double>> topLogprobs;
	List<Integer> textOffset;
}

