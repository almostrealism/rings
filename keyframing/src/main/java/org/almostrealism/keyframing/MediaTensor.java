package org.almostrealism.keyframing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MediaTensor {
	private String mediaFile;
	private MediaProvider media;
	private KeyFramer keyFramer;

	private List<KeyFrame> keyFrames;

	public MediaTensor() { }

	private MediaTensor(MediaProvider media, double salientRatio, double collapseWindowSeconds) {
		this.media = media;
		initKeyFramer(salientRatio, collapseWindowSeconds);
	}

	public String getMediaFile() { return mediaFile; }

	public void setMediaFile(String mediaFile) { this.mediaFile = mediaFile; }

	@JsonIgnore
	public void setMedia(MediaProvider media) { this.media = media; }

	@JsonIgnore
	public MediaProvider getMedia() {
		if (media == null) {
			System.out.println("MediaTensor: Loading media for " + mediaFile + " with default inclusion and scale");
			media = new MediaProvider(getMediaFile(), 0.4, 8);
		}

		return media;
	}

	public void initKeyFramer(double salientRatio, double collapseWindowSeconds) {
		this.keyFramer = new KeyFramer(salientRatio, (long) (collapseWindowSeconds * Math.pow(10, 6)));
	}

	public void setKeyFrames(List<KeyFrame> keyFrameData) { this.keyFrames = keyFrameData; }
	public List<KeyFrame> getKeyFrames() { return keyFrames; }

	public KeyFrame getKeyFrameAt(double seconds) {
		for (int i = 0; i < keyFrames.size(); i++) {
			if (keyFrames.get(i).getStartTime() > seconds) {
				if (i == 0) {
					return null;
				}

				return i > 0 ? keyFrames.get(i - 1) : null;
			}
		}

		KeyFrame last = keyFrames.get(keyFrames.size() - 1);

		if (last.getStartTime() + last.getDuration() > seconds) {
			return last;
		} else {
			return null;
		}
	}

	public boolean hasWords() {
		return keyFrames.stream().map(KeyFrame::getWords).filter(Objects::nonNull)
								.map(OCRResult::getWords).filter(Objects::nonNull)
								.flatMap(List::stream).findAny().isPresent();
	}

	public void computeKeyFrames() {
		this.keyFrames = computeKeyFrames(computeHistograms());
	}

	public void ocrKeyFrames() {
		FrameOCR ocr = new FrameOCR(media);
		ocr.init();
		keyFrames.forEach(frame -> frame.loadText(media, ocr));
		keyFrames = keyFramer.reduceByTopWords(keyFrames, media.getTotalDuration());
	}

	public String asJson() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}

	private List<VideoImage> computeHistograms() {
		return media.stream(true).filter(VideoImage::hasImage).map(VideoImage::computeHistogram).collect(Collectors.toList());
	}

	private List<KeyFrame> computeKeyFrames(List<VideoImage> histograms) {
		return keyFramer.process(histograms, media.getTotalDuration());
	}

	public static MediaTensor load(String movieFile, int inclusion, double salientRatio, double collapseWindowSeconds) {
		return load(movieFile, movieFile + ".json", inclusion, salientRatio, collapseWindowSeconds);
	}

	public static MediaTensor load(String movieFile, String tensorFile, int inclusion, double salientRatio, double collapseWindowSeconds) {
		try {
			File existing = new File(tensorFile);
			MediaProvider media = new MediaProvider(movieFile, 0.4, inclusion);
			MediaTensor tensor = existing.exists() ?
					new ObjectMapper().readValue(new File(tensorFile).toURI().toURL(), MediaTensor.class) :
					new MediaTensor();
			tensor.setMediaFile(movieFile);
			tensor.setMedia(media);
			tensor.initKeyFramer(salientRatio, collapseWindowSeconds);
			return tensor;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (JsonParseException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static MediaTensor load(String movieFile) {
		MediaTensor tensor = MediaTensor.load(movieFile, 8, 0.02, 30);

		if (tensor.getKeyFrames() == null || tensor.getKeyFrames().isEmpty()) {
			tensor.computeKeyFrames();
		}

		if (!tensor.hasWords()) tensor.ocrKeyFrames();

		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(movieFile + ".json")))) {
			out.write(tensor.asJson());
			out.flush();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return tensor;
	}
}
