package org.almostrealism.keyframing;

import org.almostrealism.texture.GraphicsConverter;

import java.awt.image.BufferedImage;

public class VideoImage implements Comparable<VideoImage> {
	public static final int histogramBuckets = 64;

	private BufferedImage image;
	private double[] histogram;
	private double histogramDelta;
	private long timestamp;
	private boolean isLast;

	public VideoImage(long timestamp, BufferedImage image) {
		setTimestamp(timestamp);
		setImage(image);
	}

	public long getTimestamp() { return timestamp; }

	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public boolean hasImage() { return getImage() != null; }

	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean last) {
		isLast = last;
	}

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
		return (int) (this.getTimestamp() - o.getTimestamp());
	}
}
