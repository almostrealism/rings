package org.almostrealism.keyframing;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Word {
	private String text;
	private boolean english;
	private double size;

	public Word() { }

	public Word(String text, double size) {
		this(text, size, false);
	}

	public Word(String text, double size, boolean english) {
		setText(text);
		setSize(size);
		setEnglish(english);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = clean(text);
	}

	public boolean isEnglish() {
		return english;
	}

	public void setEnglish(boolean english) {
		this.english = english;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double size) {
		this.size = size;
	}

	public List<Word> split() {
		return Stream.of(text.split(" ")).map(t -> new Word(t, getSize(), english)).collect(Collectors.toList());
	}

	public boolean equals(Object o) {
		if (!(o instanceof Word)) return false;
		return getText().equals(((Word) o).getText());
	}

	public int hashCode() { return getText().hashCode(); }

	private static String clean(String text) {
		return text
				.replaceAll("\\|", "")
				.replaceAll("\\[", "")
				.replaceAll("]", "")
				.replaceAll("\\(", "")
				.replaceAll("\\)", "")
				.replaceAll("<", "")
				.replaceAll(">", "")
				.replaceAll(":", "")
				.replaceAll("\\.", "")
				.replaceAll("!", "");
	}
}
