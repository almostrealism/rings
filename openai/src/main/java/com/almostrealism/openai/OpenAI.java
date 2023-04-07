package com.almostrealism.openai;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OpenAI {

	@POST("/v1/engines/{engine_id}/completions")
	Observable<CompletionResult> createCompletion(@Path("engine_id") String engineId, @Body CompletionRequest request);

}

