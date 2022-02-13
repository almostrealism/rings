package org.almostrealism.keyframing;

import org.almostrealism.texture.GraphicsConverter;

import java.awt.image.BufferedImage;

public class VideoImage implements Comparable<VideoImage> {
	public static final int histogramBuckets = 64;

	private int frame;
	private BufferedImage image;
	private double[] histogram;
	private double histogramDelta;

	public VideoImage(int frame, BufferedImage image) {
		setFrame(frame);
		setImage(image);
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public boolean hasImage() { return getImage() != null; }

	public VideoImage computeHistogram() {
		getHistogram();
		return this;
	}

	public double[] getHistogram() {
		if (histogram == null && getImage() != null) {
			histogram = GraphicsConverter.histogram(getImage(), 0, 0, getImage().getWidth(), getImage().getHeight(), histogramBuckets);
			setImage(null);
		}

		return histogram;
	}

	public double getHistogramDelta() {
		return histogramDelta;
	}

	public void setHistogramDelta(double histogramDelta) {
		this.histogramDelta = histogramDelta;
	}

	@Override
	public int compareTo(VideoImage o) {
		return this.getFrame() - o.getFrame();
	}
}
