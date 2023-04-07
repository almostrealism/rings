package com.almostrealism.openai;

import com.almostrealism.aware.Model;
import com.almostrealism.aware.PromptModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

public class GPTModel extends PromptModel {
	private ObjectMapper mapper;
	private OpenAI api;

	public GPTModel() { }

	public void init() {
		mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);


		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(chain -> {
					Request request = chain.request()
							.newBuilder()
							.header("Authorization", "Bearer " + System.getProperty("OPENAI_API_KEY"))
							.build();
					return chain.proceed(request);
				})
				.readTimeout(20, TimeUnit.SECONDS)
				.build();

		Retrofit retrofit = new Retrofit.Builder()
				.client(client)
				.baseUrl("https://api.openai.com/")
				.addConverterFactory(JacksonConverterFactory.create(mapper))
				.addCallAdapterFactory(RxJava3CallAdapterFactory.create())
				.build();

		this.api = retrofit.create(OpenAI.class);
	}

	@Override
	public String generate(String input) {
		CompletionRequest completionRequest = CompletionRequest.builder()
				.prompt(input).echo(false).n(1).maxTokens(200)
				.frequencyPenalty(0.5).build();

		try {
			System.out.println(mapper.writeValueAsString(completionRequest));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		return api.createCompletion("text-davinci-003", completionRequest)
						.blockingFirst().getChoices().get(0).getText();

	}
}
