package org.almostrealism.keyframing;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyFrame {
	private double startTime;
	private double duration;
	private BufferedImage image;
	private List<Word> words;

	public KeyFrame() { }

	public KeyFrame(double startTime) {
		this.startTime = startTime;
	}

	public KeyFrame(VideoImage img) {
		this(img.getTimestamp() * Math.pow(10, -6));
		this.image = img.getImage();
	}

	public void loadText(MediaProvider media, FrameOCR ocr) {
		try {
			ocr.processImage(media.getImage(this), this, media.getName());
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

	public double getStartTime() { return startTime; }

	public void setStartTime(double startTime) { this.startTime = startTime; }

	public double getDuration() { return duration; }

	public void setDuration(double duration) { this.duration = duration; }

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	@JsonIgnore
	public List<String> getText() { return words == null ? null : words.stream().map(Word::getText).collect(Collectors.toList()); }

	@JsonIgnore
	public List<String> getSizeOrderedText() {
		return words == null ? null : words.stream()
				.sorted(Comparator.comparing(Word::getSize).reversed())
				.map(Word::getText).collect(Collectors.toList());
	}

	@JsonIgnore
	public List<String> getSizeOrderedEnglishText() {
		return getSizeOrderedEnglishText(-1);
	}

	public List<String> getSizeOrderedEnglishText(int max) {
		if (words == null) return Collections.emptyList();

		Stream<String> w = words.stream().filter(Word::isEnglish)
				.sorted(Comparator.comparing(Word::getSize).reversed())
				.map(Word::getText);

		return (max > 0 ? w.limit(max) : w).collect(Collectors.toList());
	}

	@JsonIgnore
	public List<String> getEnglishText() {
		return words == null ? null : words.stream().filter(w -> w.isEnglish()).map(Word::getText).collect(Collectors.toList());
	}
}
