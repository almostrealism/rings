package org.almostrealism.keyframing;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.leptonica.PIX;
import org.bytedeco.leptonica.global.lept;
import org.bytedeco.opencv.opencv_java;
import org.bytedeco.tesseract.TessBaseAPI;
import org.bytedeco.tesseract.global.tesseract;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRotatedRect;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class FrameOCR {
	static {
		Loader.load(opencv_java.class);
	}

	public static boolean enableDebugImages = false;
	public static final boolean enableDnnRecognition = false;
	public static final boolean enablePostPadding = false;
	public static final float confidenceThreshold = 0.1f;
	public static final float nmsThreshold = 0.05f;
	public static final float contrastScaleFactor = 2;
	// public static final int inpWidth = 320, inpHeight = 320;
	public static final int inpWidth = 1280, inpHeight = 1280;
	public static final int padX = 6;
	public static final int padY = 2;

	private final MediaProvider media;
	private Net detector;
	private Net recognition;
	private TessBaseAPI tess;
	private Dictionary english;

	private int tot;

	public FrameOCR() { this(null); }

	public FrameOCR(MediaProvider media) {
		this.media = media;
	}

	public void init() {
		detector = Dnn.readNet("/Users/michael/AlmostRealism/frozen_east_text_detection.pb");

		if (enableDnnRecognition) {
			recognition = Dnn.readNetFromONNX("/Users/michael/AlmostRealism/CRNN_VGG_BiLSTM_CTC.onnx");
		} else {
			tess = tesseract.TessBaseAPICreate();
			System.out.println("FrameOCR: tess init " + tesseract.TessBaseAPIInit3(tess, "/Users/michael/AlmostRealism/tessdata", "eng"));
		}

		try {
			JWNL.initialize(new FileInputStream("/Users/michael/AlmostRealism/rings/keyframing/src/main/resources/wordnet-conf.xml"));
			english = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public List<String> processImage(VideoImage image, KeyFrame destination, String name) {
		if (image == null || image.getImage() == null) {
			System.out.println("WARN: Frame " + image.getTimestamp() + " was targeted for OCR, but was not included in media");
			return Collections.emptyList();
		}

		try {
			long timestamp = (long) (destination.getStartTime() * Math.pow(10, 6));
			OCRResult words = processImage(image.getImage(),
						() -> "Videos/samples/" + name + "-" + timestamp + ".png");

			for (Word w : words.getWords()) {
				w.setEnglish(english.lookupAllIndexWords(w.getText()).size() > 0);
			}

			destination.setWords(words);
			System.out.println("FrameOCR: " + words.getWords().size() + " words");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JWNLException e) {
			e.printStackTrace();
		}

		return Collections.emptyList();
	}

	private Mat bufferedImageToMat(BufferedImage image) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		ImageIO.write(image, "png", byteArrayOutputStream);
		byteArrayOutputStream.flush();
		return Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
	}

	private BufferedImage matToBufferedImage(Mat matrix) {
		Imgcodecs.imwrite("tmp.jpg", matrix);

		try {
			return ImageIO.read(new File("tmp.jpg"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public OCRResult processImage(BufferedImage f, Supplier<String> imageLogFile) throws IOException {
		if (enableDebugImages) ImageIO.write(f, "png", new File("/Users/michael/Desktop/pre-ocr.png"));

		List<Word> texts = new ArrayList<>();

		int width = f.getWidth();
		int height = f.getHeight();
		double rW = width / (float) inpWidth;
		double rH = height / (float) inpHeight;

		Size size = new Size(inpWidth, inpHeight);
		Scalar mean = new Scalar(123.68, 116.78, 103.94, 0.0);

		Mat frame = bufferedImageToMat(f);
		Mat blob = Dnn.blobFromImage(bufferedImageToMat(f), 1.0, size,
										mean, true, false, CvType.CV_32F);

		List<Mat> outs = new ArrayList<>();

		List<String> outNames = new ArrayList<>();
		outNames.add("feature_fusion/Conv_7/Sigmoid");
		outNames.add("feature_fusion/concat_3");

		detector.setInput(blob);
		detector.forward(outs, outNames);

		Mat scores = outs.get(0);
		Mat geometry = outs.get(1);

		Detections detections = decodeBoundingBoxes(scores, geometry, confidenceThreshold);
		MatOfInt indcs = new MatOfInt();
		MatOfRotatedRect boxesMat = detections.getBoxes();
		MatOfFloat confidencesMat = detections.getConfidences();
		Dnn.NMSBoxesRotated(boxesMat, confidencesMat, confidenceThreshold, nmsThreshold, indcs);

		List<RotatedRect> boxes = boxesMat.toList();
		int[] indices = indcs.toArray();
		Arrays.sort(indices);

		for (int i : indices) {
			Mat vertices = new Mat();
			Imgproc.boxPoints(boxes.get(i), vertices);

			List<Point> points = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				points.add(paddedPoint(j, rW, rH, vertices.get(j, 0)[0], vertices.get(j, 1)[0]));
			}

			Mat cropped = fourPointsTransform(frame, points);
			Mat cvtCropped = new Mat();
			Imgproc.cvtColor(cropped, cvtCropped, Imgproc.COLOR_BGR2GRAY);
			// Mat rblob = Dnn.blobFromImage(cvtCropped, 127.5, new Size(100, 32), new Scalar(127.5, 127.5, 127.5, 127.5));
			Mat rblob = Dnn.blobFromImage(cvtCropped, contrastScaleFactor, new Size(100, 32), new Scalar(127.5, 127.5, 127.5, 127.5));

			List<Mat> blobImages = new ArrayList<>();
			Dnn.imagesFromBlob(rblob, blobImages);
			if (enableDebugImages) Imgcodecs.imwrite("/Users/michael/Desktop/Blobs/blob-" + i + ".jpg", blobImages.get(0));

			if (enableDnnRecognition) {
				List<Mat> outputs = new ArrayList<>();
				recognition.setInput(rblob);
				recognition.forward(outputs);
				texts.add(new Word(decodeText(outputs.get(0)), cvtCropped.width() * cvtCropped.height()));
			} else {
				texts.add(new Word(ocr(blobImages.get(0)), cvtCropped.width() * cvtCropped.height()));
			}

			for (int j = 0; j < 4; j++) {
				Point p1 = points.get(j);
				Point p2 = points.get((j + 1) % 4);
				Imgproc.line(frame, p1, p2, new Scalar(0, 255, 0, 0), 1);
			}
		}

		OCRResult result = new OCRResult();

		if (imageLogFile != null) {
			String name = imageLogFile.get();
			Imgcodecs.imwrite(name, frame);
			result.setImage(name);
		}

		result.setWords(texts);

		scores.release();
		geometry.release();
		blob.release();

		System.out.println("FrameOCR(" + tot++ + "): Extracted " + texts.size() + " texts");
		return result;
	}

	private Point paddedPoint(int vIdx, double rW, double rH, double x, double y) {
		if (enablePostPadding) {
			if (vIdx == 0) {
				return new Point(Math.max(0, rW * (x - padX)), Math.max(0, rH * (y + padY)));
			} else if (vIdx == 1) {
				return new Point(Math.max(0, rW * (x - padX)), Math.max(0, rH * (y - padY)));
			} else if (vIdx == 2) {
				return new Point(Math.max(0, rW * (x + padX)), Math.max(0, rH * (y - padY)));
			} else if (vIdx == 3) {
				return new Point(Math.max(0, rW * (x + padX)), Math.max(0, rH * (y + padY)));
			} else {
				throw new UnsupportedOperationException();
			}
		} else {
			return new Point(rW * x, rH * y);
		}
	}

	private String ocr(Mat matrix) {
		Imgcodecs.imwrite("tmp.jpg", matrix);
		PIX pix = lept.pixRead("tmp.jpg");

		try {
			tess.SetImage(pix);
			return tess.GetUTF8Text().getString().trim();
		} finally {
			pix.destroy();
		}
	}

	private Detections decodeBoundingBoxes(Mat scores, Mat geometry, double scoreThresh) {
		if (scores.dims() != 4) throw new UnsupportedOperationException();
		if (geometry.dims() != 4) throw new UnsupportedOperationException();
		if (scores.size(0) != 1) throw new UnsupportedOperationException();
		if (geometry.size(0) != 1) throw new UnsupportedOperationException();
		if (scores.size(1) != 1) throw new UnsupportedOperationException();
		if (geometry.size(1) != 5) throw new UnsupportedOperationException();
		if (scores.size(2) != geometry.size(2)) throw new UnsupportedOperationException();
		if (scores.size(3) != geometry.size(3)) throw new UnsupportedOperationException();

		int width = scores.size(2);
		int height = scores.size(3);

		Detections detections = new Detections();

		IntStream.range(0, height).forEach(y -> {
			Function<Integer, Double> scoresData = v -> scores.get(new int[] { 0, 0, y, v })[0];
			Function<Integer, Double>  x0_data = v -> geometry.get(new int[] { 0, 0, y, v })[0];
			Function<Integer, Double>  x1_data = v -> geometry.get(new int[] { 0, 1, y, v })[0];
			Function<Integer, Double>  x2_data = v -> geometry.get(new int[] { 0, 2, y, v })[0];
			Function<Integer, Double>  x3_data = v -> geometry.get(new int[] { 0, 3, y, v })[0];
			Function<Integer, Double>  anglesData = v -> geometry.get(new int[] { 0, 4, y, v })[0];

			IntStream.range(0, width).forEach(x -> {
				double score = scoresData.apply(x);
				if (score < scoreThresh) return;

				double offsetX = x * 4.0 + padX / 2;
				double offsetY = y * 4.0 + padY / 2;
				double angle = anglesData.apply(x);

				double cosA = Math.cos(angle);
				double sinA = Math.sin(angle);
				double h = x0_data.apply(x) + x2_data.apply(x) + padY;
				double w = x1_data.apply(x) + x3_data.apply(x) + padX;

				double[] offset = new double[] { offsetX + cosA * x1_data.apply(x) + sinA * x2_data.apply(x), offsetY - sinA * x1_data.apply(x) + cosA * x2_data.apply(x) };
				double[] p1 = new double[] { -sinA * h + offset[0], -cosA * h + offset[1] };
				double[] p3 = new double[] { -cosA * w + offset[0], sinA * w + offset[1] };
				double[] center = new double[] { 0.5 * (p1[0] + p3[0]), 0.5 * (p1[1] + p3[1]) };
				detections.detections.add(new Detection(center, new double[] { w, h }, -1 * angle * 180.0 / Math.PI));
				detections.confidences.add(score);
			});
		});

		return detections;
	}

	private Mat fourPointsTransform(Mat frame, List<Point> points) {
		Size outputSize = new Size(100, 32);
		List<Point> targetVertices = new ArrayList<>();
		targetVertices.add(new Point(0, outputSize.height - 1));
		targetVertices.add(new Point(0, 0));
		targetVertices.add(new Point(outputSize.width - 1, 0));
		targetVertices.add(new Point(outputSize.width - 1, outputSize.height - 1));

		MatOfPoint2f vertices = new MatOfPoint2f(points.toArray(new Point[0]));
		MatOfPoint2f target = new MatOfPoint2f(targetVertices.toArray(new Point[0]));
		Mat rotationMatrix = Imgproc.getPerspectiveTransform(vertices, target);
		Mat result = new Mat();
		Imgproc.warpPerspective(frame, result, rotationMatrix, outputSize);
		return result;
	}

	private String decodeText(Mat scores) {
		StringBuffer text = new StringBuffer();
		char[] alphabet = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

		for (int i = 0; i < scores.size(0); i++) {
			int c = argmax(vals(scores, i, 0));

			if (c != 0) {
				text.append(alphabet[c - 1]);
			} else {
				text.append('-');
			}
		}

		StringBuffer result = new StringBuffer();
		char[] txt = text.toString().toCharArray();
		for (int i = 0; i < txt.length; i++) {
			if (txt[i] != '-' && !(i > 0 && txt[i] == txt[i - 1]))
				result.append(txt[i]);
		}

		System.out.println("FrameOCR: " + result);
		return result.toString();
	}

	private int argmax(double[] vals) {
		return IntStream.range(0, vals.length).mapToObj(i -> i).max((i, j) -> (int) (1000 * vals[i] - vals[j])).get();
	}

	private double[] vals(Mat mat, int x, int y) {
		double[] vals = new double[mat.size(2)];
		IntStream.range(0, vals.length).forEach(i -> vals[i] = mat.get(new int[] { x, y, i })[0]);
		return vals;
	}

	private static class Detections {
		private final List<Detection> detections = new ArrayList<>();
		private final List<Double> confidences = new ArrayList<>();


		public MatOfRotatedRect getBoxes() {
			return new MatOfRotatedRect(detections.stream().map(Detection::getRectangle).toArray(RotatedRect[]::new));
		}

		public MatOfFloat getConfidences() {
			float[] f = new float[confidences.size()];
			IntStream.range(0, f.length).forEach(i -> f[i] = (float) confidences.get(i).doubleValue());
			return new MatOfFloat(f);
		}
	}

	private static class Detection {
		private final double[] center;
		private final double[] wh;
		private final double angle;

		public Detection(double[] center, double[] wh, double angle) {
			this.center = center;
			this.wh = wh;
			this.angle = angle;
		}

		public RotatedRect getRectangle() {
			Point p = new Point(center[0], center[1]);
			Size s = new Size(wh[0], wh[1]);
			return new RotatedRect(p, s, angle);
		}
	}

	public static BufferedImage preprocess(BufferedImage image) {
		BufferedImage imageRGB = new BufferedImage(image.getWidth(),
				image.getHeight(), BufferedImage.TYPE_INT_RGB);
		imageRGB.setData(image.getData());
		return imageRGB;
	}

	public static void main(String[] args) {
		enableDebugImages = true;

		FrameOCR ocr = new FrameOCR();
		ocr.init();

		int targetIndex = 39;
		MediaTensor tensor = MediaTensor.load("/Users/michael/Desktop/Finance Meeting for April 20, 2020.mp4", 4, 0.0001, 12);
		tensor.getKeyFrames().get(targetIndex).loadText(tensor.getMedia(), ocr);
		for (String s : tensor.getKeyFrames().get(targetIndex).getEnglishText()) {
			System.out.println("FrameOCR: " + s);
		}
	}
}
