package com.almostrealism.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionChoice {
	private String text;
	private Integer index;
	private LogProbabilityResult logprobs;
	private String finish_reason;
}

