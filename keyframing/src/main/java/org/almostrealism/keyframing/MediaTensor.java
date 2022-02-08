package org.almostrealism.keyframing;

import org.almostrealism.texture.GraphicsConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MediaTensor {
	public static final int histogramBuckets = 64;

	private double fps;
	private List<BufferedImage> frames;

	private int keyFrameCollapseWindow;
	private List<double[]> histograms;
	private List<Integer> keyFrames;
	private List<Integer> collapsedKeyFrames;

	private MediaTensor(List<BufferedImage> frames, double fps) {
		this.frames = frames;
		this.fps = fps;
		// this.keyFrameCollapseWindow = (int) fps;
		this.keyFrameCollapseWindow = 6;
		computeHistograms();
		computeKeyFrames();
		collapseKeyFrames();
	}

	public Image getFrame(int index) { return frames.get(index); }

	public double getFrameRate() { return fps; }

	public int getCount() { return frames.size(); }

	public List<Integer> getCollapsedKeyFrames() {
		return collapsedKeyFrames;
	}

	public static MediaTensor load(String movieFile, int maxFrames) throws FFmpegFrameGrabber.Exception {
		FFmpegFrameGrabber g = new FFmpegFrameGrabber(movieFile);
		g.start();
		double fps = g.getVideoFrameRate();

		int total = Math.min(maxFrames, g.getLengthInFrames());

		MediaPreprocessor prep = new MediaPreprocessor();

		return new MediaTensor(IntStream.range(0, total).mapToObj(i -> {
			try {
				return prep.convertFrameToBuffer(g.grab());
			} catch (FFmpegFrameGrabber.Exception e) {
				e.printStackTrace();
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList()), fps);
	}

	private void computeHistograms() {
		histograms = frames.stream().map(f ->
					GraphicsConverter.histogram(f, 0, 0, f.getWidth(), f.getHeight(), histogramBuckets))
				.collect(Collectors.toList());
	}

	private void computeKeyFrames() {
		keyFrames = new KeyFramer().process(histograms);
	}

	private void collapseKeyFrames() {
		Collections.sort(keyFrames);

		collapsedKeyFrames = new ArrayList<>();
		int currentKeyFrame = keyFrames.get(0);
		for (int i = 1; i < keyFrames.size(); i++) {
			if (keyFrames.get(i) - currentKeyFrame < keyFrameCollapseWindow) {
				currentKeyFrame = keyFrames.get(i);
			} else {
				collapsedKeyFrames.add(currentKeyFrame);
				currentKeyFrame = keyFrames.get(i);
			}
		}

		System.out.println(collapsedKeyFrames.size() + " collapsed key frames");
	}
}
