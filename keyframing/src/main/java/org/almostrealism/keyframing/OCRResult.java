package org.almostrealism.keyframing;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

public class OCRResult {
	private String image;
	private List<Word> words;

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		words = words.stream()
				.map(Word::split).flatMap(List::stream)
				.filter(w -> w.getText().length() > 2)
				.collect(Collectors.toList());
		this.words = new ArrayList<>(new LinkedHashSet<>(words));
	}
}
