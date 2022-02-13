package org.almostrealism.keyframing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeyFrame {
	private int frameIndex;
	private int framesToNextKey;
	private BufferedImage image;
	private List<Word> words;

	public KeyFrame() { }

	public KeyFrame(int frameIndex) {
		this.frameIndex = frameIndex;
	}

	public KeyFrame(VideoImage img) {
		this(img.getFrame());
		this.image = img.getImage();
	}

	public void loadText(MediaProvider media, FrameOCR ocr) {
		ocr.processImage(media.getImage(this), this);
	}

	public int getFrameIndex() {
		return frameIndex;
	}

	public void setFrameIndex(int frameIndex) {
		this.frameIndex = frameIndex;
	}

	public int getFramesToNextKey() {
		return framesToNextKey;
	}

	public void setFramesToNextKey(int framesToNextKey) {
		this.framesToNextKey = framesToNextKey;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}

	public List<String> getText() { return words.stream().map(Word::getText).collect(Collectors.toList()); }

	public List<String> getSizeOrderedText() {
		return words.stream()
				.sorted(Comparator.comparing(Word::getSize).reversed())
				.map(Word::getText).collect(Collectors.toList());
	}

	public List<String> getSizeOrderedEnglishText() {
		return getSizeOrderedEnglishText(-1);
	}

	public List<String> getSizeOrderedEnglishText(int max) {
		Stream<String> w = words.stream().filter(Word::isEnglish)
				.sorted(Comparator.comparing(Word::getSize).reversed())
				.map(Word::getText);

		return (max > 0 ? w.limit(max) : w).collect(Collectors.toList());
	}

	// TODO  Remove
	public void setText(List<String> text) {  }

	public List<String> getEnglishText() {
		return words.stream().filter(w -> w.isEnglish()).map(Word::getText).collect(Collectors.toList());
	}

	// TODO  Remove
	public void setEnglishText(List<String> englishText) {
	}
}
