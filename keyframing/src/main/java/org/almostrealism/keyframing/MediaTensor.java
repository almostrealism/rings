package org.almostrealism.keyframing;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MediaTensor {
	private MediaProvider media;
	private KeyFramer keyFramer;
	private Map<Integer, BufferedImage> frames;

	private List<KeyFrame> keyFrames;

	public MediaTensor() {
		frames = new HashMap<>();
	}

	private MediaTensor(MediaProvider media, double salientRatio, double collapseWindowSeconds) {
		this.media = media;
		this.frames = new HashMap<>();
		initKeyFramer(salientRatio, collapseWindowSeconds);
	}

	@JsonIgnore
	public void setMedia(MediaProvider media) { this.media = media; }

	@JsonIgnore
	public MediaProvider getMedia() { return media; }

	public BufferedImage getFrame(int index) { return frames.get(index); }

	public void initKeyFramer(double salientRatio, double collapseWindowSeconds) {
		this.keyFramer = new KeyFramer(salientRatio / media.getFrameRate(), (int) (collapseWindowSeconds * media.getFrameRate()));
	}

	public void loadAllFrames() {
		loadFrames(0, getMedia().getCount());
	}

	public void loadFrames(int index, int length) {
		System.out.println("MediaTensor: Loading " + length + " frames...");
		media.setPosition(index);
		media.stream(true).limit(length).forEach(frame -> frames.put(frame.getFrame(), frame.getImage()));
	}

	// TODO  Remove
	public void setSalientRatio(double salientRatio) { }

	// TODO  Remove
	public void setKeyFrameCollapseWindow(int keyFrameCollapseWindow) { }

	public void setKeyFrames(List<KeyFrame> keyFrameData) { this.keyFrames = keyFrameData; }
	public List<KeyFrame> getKeyFrames() { return keyFrames; }

	public void computeKeyFrames() {
		this.keyFrames = computeKeyFrames(computeHistograms());
	}

	public void ocrKeyFrames() {
		FrameOCR ocr = new FrameOCR(media);
		ocr.init();
		keyFrames.forEach(frame -> frame.loadText(media, ocr));
		keyFrames = keyFramer.reduceByTopWords(keyFrames, media.getCount());
	}

	public String asJson() throws JsonProcessingException {
		return new ObjectMapper().writeValueAsString(this);
	}

	private List<VideoImage> computeHistograms() {
		return media.stream(true).map(frame -> {
			frames.put(frame.getFrame(), frame.getImage());
			return frame;
		}).filter(VideoImage::hasImage).map(VideoImage::computeHistogram).collect(Collectors.toList());
	}

	private List<KeyFrame> computeKeyFrames(List<VideoImage> histograms) {
		return keyFramer.process(histograms, media.getCount());
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
}
