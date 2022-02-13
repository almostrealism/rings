package org.almostrealism.keyframing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeyFramer {
	private double salientRatio;
	private int collapseWindow;

	public KeyFramer(double salientRatio, int collapseWindow) {
		this.salientRatio = salientRatio;
		this.collapseWindow = collapseWindow;
	}

	public List<KeyFrame> process(List<VideoImage> images, int totalFrames) {
		System.out.println("KeyFramer: Computing sequential delta vectors for " + images.size() + " frames");
		sequentialDeltas(images);
		return collapseKeyFrames(images.stream()
				.sorted((i, j) -> (int) (1000 * (j.getHistogramDelta() - i.getHistogramDelta())))
				.limit((int) (totalFrames * salientRatio))
				.collect(Collectors.toList()), totalFrames);
	}

	public List<KeyFrame> reduceByTopWords(List<KeyFrame> frames, int totalFrames) {
		List<KeyFrame> collapsed = new ArrayList<>();
		KeyFrame current = frames.get(0);

		for (int i = 1; i < frames.size(); i++) {
			if (diff(frames.get(i).getSizeOrderedEnglishText(6), current.getSizeOrderedEnglishText(6)) < 4) {
				current = frames.get(i);
			} else {
				collapsed.add(current);
				current = frames.get(i);
			}
		}

		for (int i = 0; i < collapsed.size() - 1; i++) {
			collapsed.get(i).setFramesToNextKey(collapsed.get(i + 1).getFrameIndex() - collapsed.get(i).getFrameIndex());
		}

		collapsed.get(collapsed.size() - 1).setFramesToNextKey(totalFrames - collapsed.get(collapsed.size() - 1).getFrameIndex());
		System.out.println("KeyFramer: " + frames.size() + " key frames reduced to " + collapsed.size() + " due to similar words");
		return collapsed;
	}

	protected void sequentialDeltas(List<VideoImage> vectors) {
		for (int i = 1; i < vectors.size(); i++) {
			double a[] = vectors.get(i - 1).getHistogram();
			double b[] = vectors.get(i).getHistogram();
			vectors.get(i).setHistogramDelta(length(IntStream.range(0, a.length).mapToDouble(x -> b[x] - a[x]).toArray()));
		}
	}

	protected List<KeyFrame> collapseKeyFrames(List<VideoImage> frames, int totalFrames) {
		System.out.println("KeyFramer: " + frames.size() + " initial key frames");
		Collections.sort(frames);

		List<VideoImage> collapsedKeyFrames = new ArrayList<>();
		VideoImage currentKeyFrame = frames.get(0);
		for (int i = 1; i < frames.size(); i++) {
			if (frames.get(i).getFrame() - currentKeyFrame.getFrame() < collapseWindow) {
				currentKeyFrame = frames.get(i);
			} else {
				collapsedKeyFrames.add(currentKeyFrame);
				currentKeyFrame = frames.get(i);
			}
		}

		System.out.println("KeyFramer: " + collapsedKeyFrames.size() + " collapsed key frames");
		List<KeyFrame> keyFrames = collapsedKeyFrames.stream().map(KeyFrame::new).collect(Collectors.toList());

		for (int i = 0; i < keyFrames.size() - 1; i++) {
			keyFrames.get(i).setFramesToNextKey(keyFrames.get(i + 1).getFrameIndex() - keyFrames.get(i).getFrameIndex());
		}

		KeyFrame last = keyFrames.get(keyFrames.size() - 1);
		last.setFramesToNextKey(totalFrames - last.getFrameIndex());

		return keyFrames;
	}

	private long diff(List<?> a, List<?> b) {
		return a.stream().filter(v -> !b.contains(v)).count() + b.stream().filter(v -> !a.contains(v)).count();
	}

	private double length(double a[]) {
		return IntStream.range(0, a.length).mapToDouble(i -> a[i] * a[i]).sum();
	}
}
