package com.almostrealism.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CompletionResult {
	private String id;
	private String object;
	private long created;
	private String model;
	private List<CompletionChoice> choices;
}

