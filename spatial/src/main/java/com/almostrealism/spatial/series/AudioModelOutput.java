/*
 * Copyright 2025 Michael Murray
 * All Rights Reserved
 */

package com.almostrealism.spatial.series;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.almostrealism.collect.PackedCollection;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class AudioModelOutput extends SimpleTimeseries<AudioModelOutput> {

	private String modelName;
	private String conditions;
	private List<Double> embed;

	public AudioModelOutput() { }

	public AudioModelOutput(String key) {
		this(key, null, null);
	}

	public AudioModelOutput(String key, String conditions) {
		this(key, conditions, null);
	}

	public AudioModelOutput(String key, List<Double> embed) {
		this(key, null, embed);
	}

	public AudioModelOutput(String key, int embedDim) {
		this(key, null, embedDim);
	}

	public AudioModelOutput(String key, String conditions, int embedDim) {
		this(key, conditions, Stream.generate(new Random()::nextGaussian).limit(embedDim).toList());
	}

	public AudioModelOutput(String key, String conditions, List<Double> embed) {
		super(key);
		this.conditions = conditions;
		this.embed = embed;
	}

	public String getModelName() { return modelName; }
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getConditionalText() {
		return conditions;
	}
	public void setConditionalText(String conditions) {
		this.conditions = conditions;
	}

	public List<Double> getEmbed() { return embed; }
	public void setEmbed(List<Double> embed) {
		this.embed = embed;
	}

	@JsonIgnore
	public PackedCollection getPackedEmbed() {
		if (getEmbed() == null) return null;
		return PackedCollection.of(getEmbed().stream().mapToDouble(d -> d).toArray());
	}
}
