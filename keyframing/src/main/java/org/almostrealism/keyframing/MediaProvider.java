package org.almostrealism.keyframing;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MediaProvider implements Supplier<Frame> {
	private String movieFile;
	private MediaPreprocessor preprocessor;
	private FFmpegFrameGrabber media;
	private double fps;
	private double index;
	private double total;
	private int inclusion;

	private Frame current;

	public MediaProvider() { }

	public MediaProvider(String movieFile, double scale, int inclusion) {
		this.movieFile = movieFile;
		this.inclusion = inclusion;
		preprocessor = new MediaPreprocessor(scale);
		media = new FFmpegFrameGrabber(movieFile);
		start();
	}

	public String getName() { return new File(movieFile).getName(); }

	public void setFrameRate(double fps) { this.fps = fps; }
	public double getFrameRate() { return fps; }

	public void setTotalDuration(double seconds) { this.total = seconds; }
	public double getTotalDuration() { return total; }

	public void start() {
		try {
			media.start();
			index = -1;
			fps = media.getVideoFrameRate();
			total = media.getLengthInFrames() / fps;
		} catch (FFmpegFrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void restart() {
		try {
			media.restart();
			index = 0;
		} catch (FrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void next() {
		try {
			current = media.grab();
			if (current != null)
				index = current.timestamp * Math.pow(10, -6);
		} catch (FFmpegFrameGrabber.Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setPosition(double index) {
		if (this.index == index) return;
		if (index < this.index) {
			restart();
		}

		while (this.index < index) next();
		while (!get().getTypes().contains(Frame.Type.VIDEO)) next();
	}

	public Frame get() { return current; }

	public Stream<VideoImage> stream(boolean enableScale) {
		return Stream.generate(() -> { next(); return getImage(enableScale); })
				.takeWhile(v -> v == null || !v.isLast()).filter(Objects::nonNull);
	}

	public VideoImage getImage(KeyFrame frame) {
		setPosition(frame.getStartTime());
		return getImage(false);
	}

	public VideoImage getImage(boolean enableScale) {
		Frame f = get();
		BufferedImage img = preprocessor.convertFrameToBuffer(f, enableScale);
		VideoImage v = new VideoImage(f == null ? -1 : f.timestamp, img);
		v.setLast(f == null);
		return v;
	}
}
