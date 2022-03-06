package org.almostrealism.keyframing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeyFramer {
	private double salientRatio;
	private long collapseWindow;

	public KeyFramer(double salientRatio, long collapseWindow) {
		this.salientRatio = salientRatio;
		this.collapseWindow = collapseWindow;
	}

	public List<KeyFrame> process(List<VideoImage> images, double totalDuration) {
		System.out.println("KeyFramer: Computing sequential delta vectors for " + images.size() + " frames");
		sequentialDeltas(images);
		return collapseKeyFrames(images.stream()
				.sorted((i, j) -> (int) (1000 * (j.getHistogramDelta() - i.getHistogramDelta())))
				.limit((int) (totalDuration * salientRatio))
				.collect(Collectors.toList()), totalDuration);
	}

	public List<KeyFrame> reduceByTopWords(List<KeyFrame> frames, double totalDuration) {
		List<KeyFrame> collapsed = new ArrayList<>();
		KeyFrame current = frames.get(0);
		KeyFrame next = frames.get(0);

		for (int i = 1; i < frames.size(); i++) {
			if (diff(frames.get(i).getSizeOrderedEnglishText(6), next.getSizeOrderedEnglishText(6)) < 4) {
				next = frames.get(i);
			} else {
				collapsed.add(current);
				current = frames.get(i);
				next = frames.get(i);
			}
		}

		if (collapsed.isEmpty()) collapsed.add(current);

		for (int i = 0; i < collapsed.size() - 1; i++) {
			collapsed.get(i).setDuration(collapsed.get(i + 1).getStartTime() - collapsed.get(i).getStartTime());
		}

		collapsed.get(collapsed.size() - 1).setDuration(totalDuration - collapsed.get(collapsed.size() - 1).getStartTime());
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

	protected List<KeyFrame> collapseKeyFrames(List<VideoImage> frames, double totalDuration) {
		System.out.println("KeyFramer: " + frames.size() + " initial key frames");
		Collections.sort(frames);

		List<VideoImage> collapsedKeyFrames = new ArrayList<>();
		VideoImage currentKeyFrame = frames.get(0);
		for (int i = 1; i < frames.size(); i++) {
			if (frames.get(i).getTimestamp() - currentKeyFrame.getTimestamp() < collapseWindow) {
				currentKeyFrame = frames.get(i);
			} else {
				collapsedKeyFrames.add(currentKeyFrame);
				currentKeyFrame = frames.get(i);
			}
		}

		System.out.println("KeyFramer: " + collapsedKeyFrames.size() + " collapsed key frames");
		List<KeyFrame> keyFrames = collapsedKeyFrames.stream().map(KeyFrame::new).collect(Collectors.toList());

		for (int i = 0; i < keyFrames.size() - 1; i++) {
			keyFrames.get(i).setDuration(keyFrames.get(i + 1).getStartTime() - keyFrames.get(i).getStartTime());
		}

		KeyFrame last = keyFrames.get(keyFrames.size() - 1);
		last.setDuration(totalDuration - last.getStartTime());

		return keyFrames;
	}

	private long diff(List<?> a, List<?> b) {
		if (a == null) a = Collections.emptyList();
		if (b == null) b = Collections.emptyList();
		List<?> fa = a;
		List<?> fb = b;
		return a.stream().filter(v -> !fb.contains(v)).count() + b.stream().filter(v -> !fa.contains(v)).count();
	}

	private double length(double a[]) {
		return IntStream.range(0, a.length).mapToDouble(i -> a[i] * a[i]).sum();
	}
}
