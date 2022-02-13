package org.almostrealism.keyframing;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MediaProvider implements Supplier<Frame> {
	private MediaPreprocessor preprocessor;
	private FFmpegFrameGrabber media;
	private double fps;
	private int index;
	private int total;
	private int inclusion;

	private Frame current;

	public MediaProvider() { }

	public MediaProvider(String movieFile, double scale, int inclusion) {
		this.inclusion = inclusion;
		preprocessor = new MediaPreprocessor(scale);
		media = new FFmpegFrameGrabber(movieFile);
		start();
	}

	public void setFrameRate(double fps) { this.fps = fps; }
	public double getFrameRate() { return fps; }

	public void setCount(int count) { this.total = count; }
	public int getCount() { return total; }

	public void start() {
		try {
			media.start();
			index = -1;
			fps = media.getVideoFrameRate();
			total = media.getLengthInFrames();
		} catch (FFmpegFrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void restart() {
		try {
			media.restart();
			index = -1;
		} catch (FrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void next() {
		try {
			current = media.grab();
			index++;
		} catch (FFmpegFrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setPosition(int index) {
		if (this.index == index) return;
		if (index < this.index) {
			restart();
		}

		while (this.index < index) next();
	}

	public Frame get() { return current; }

	public Stream<VideoImage> stream(boolean enableScale) {
		return Stream.generate(() -> { next(); return getImage(enableScale); }).limit(getCount()).filter(Objects::nonNull);
	}

	public VideoImage getImage(KeyFrame frame) {
		setPosition(frame.getFrameIndex());
		return getImage(false);
	}

	public VideoImage getImage(boolean enableScale) {
		if (index % inclusion != 0) return new VideoImage(index, null);
		BufferedImage img = preprocessor.convertFrameToBuffer(get(), enableScale);
		if (img == null) return null;
		return new VideoImage(index, img);
	}
}
