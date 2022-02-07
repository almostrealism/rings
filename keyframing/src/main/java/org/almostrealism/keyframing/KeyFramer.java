package org.almostrealism.keyframing;

import org.almostrealism.texture.GraphicsConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class KeyFramer {
	public static void main(String args[]) throws IOException {
		new KeyFramer().process("/Users/michael/Desktop/ScreenRecording.mov");
	}

	public void process(String file) throws IOException {
		FFmpegFrameGrabber g = new FFmpegFrameGrabber(file);
		g.start();

		int total = g.getLengthInFrames();
		List<double[]> histograms = new ArrayList<>();

		System.out.println("KeyFramer: Histograming " + total + " frames...");

		for (int i = 0 ; i < total; i++) {
			Frame f = g.grab();
			histograms.add(GraphicsConverter.histogram(convertFrameToBuffer(f), 0, 0, f.imageWidth, f.imageHeight, 40));
		}

		g.stop();

		System.out.println("KeyFramer: Computing sequential delta vectors...");
		double deltas[] = sequentialDeltas(histograms);
		double avg = DoubleStream.of(deltas).sum() / deltas.length;
		System.out.println("KeyFramer: Average delta = " + avg);
		List<Integer> keyFrames = IntStream.range(0, deltas.length)
				.mapToObj(i -> i)
				.sorted((i, j) -> (int) (1000 * (deltas[j] - deltas[i])))
				.limit(total / 40)
				.collect(Collectors.toList());

		g = new FFmpegFrameGrabber(file);
		g.start();

		for (int i = 0 ; i < total; i++) {
			Frame f = g.grab();
			if (keyFrames.contains(i)) {
				ImageIO.write(convertFrameToBuffer(f), "png", new File("/Users/michael/Desktop/keyframe-" + i + ".png"));
			}
		}

		System.out.println("KeyFramer: Done");
	}

	protected double[] sequentialDeltas(List<double[]> vectors) {
		double deltas[] = new double[vectors.size()];

		for (int i = 1; i < vectors.size(); i++) {
			double a[] = vectors.get(i - 1);
			double b[] = vectors.get(i);
			deltas[i] = length(IntStream.range(0, a.length).mapToDouble(x -> b[x] - a[x]).toArray());
		}

		return deltas;
	}

	private double length(double a[]) {
		return IntStream.range(0, a.length).mapToDouble(i -> a[i] * a[i]).sum();
	}

	private BufferedImage convertFrameToBuffer(Frame f) {
		return new Java2DFrameConverter().convert(f);
	}
}
