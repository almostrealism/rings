package com.almostrealism.openai;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionRequest {
	private String model;
	private String prompt;
	private Integer maxTokens;
	private Double temperature;
	private Double topP;
	private Integer n;
	private Boolean stream;
	private Integer logprobs;
	private Boolean echo;
	private List<String> stop;
	private Double presencePenalty;
	private Double frequencyPenalty;
	private Integer bestOf;
}

