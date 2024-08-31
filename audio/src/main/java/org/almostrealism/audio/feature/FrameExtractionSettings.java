package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;

public class FrameExtractionSettings {
	private Scalar sampFreq;
	private Scalar frameShiftMs;  // in milliseconds.
	private Scalar frameLengthMs;  // in milliseconds.
	private Scalar dither;  // Amount of dithering, 0.0 means no dither.
	private Scalar preemphCoeff;  // Preemphasis coefficient.
	private boolean removeDcOffset;  // Subtract mean of wave before FFT.
	private String windowType;  // e.g. Hamming window
	private boolean roundToPowerOfTwo;
	private Scalar blackmanCoeff;
	private boolean snipEdges;
	private boolean allowDownsample;
	private boolean allowUpsample;
	private int maxFeatureVectors;

	public FrameExtractionSettings() {
		this(16000.0);
	}

	public FrameExtractionSettings(double sampleRate) {
		this.sampFreq = new Scalar(sampleRate);
		this.frameShiftMs = new Scalar(10.0);
		this.frameLengthMs = new Scalar(25.0);
		this.dither = new Scalar(1.0);
		this.preemphCoeff = new Scalar(0.97);
		this.removeDcOffset = true;
		this.windowType = "povey";
		this.roundToPowerOfTwo = true;
		this.blackmanCoeff = new Scalar(0.42);
		this.snipEdges = true;
		this.allowDownsample = true;
		this.allowUpsample = false;
		this.maxFeatureVectors = -1;
	}

	public int getWindowShift() {
		return (int) (sampFreq.getValue() * 0.001 * frameShiftMs.getValue());
	}

	public int getWindowSize() {
		return (int) (sampFreq.getValue() * 0.001 * frameLengthMs.getValue());
	}

	public int getPaddedWindowSize() {
		return roundToPowerOfTwo ? RoundUpToNearestPowerOfTwo(getWindowSize()) :
				getWindowSize();
	}

	public Scalar getSampFreq() {
		return sampFreq;
	}

	public void setSampFreq(Scalar sampFreq) {
		this.sampFreq = sampFreq;
	}

	public Scalar getFrameShiftMs() {
		return frameShiftMs;
	}

	public void setFrameShiftMs(Scalar frameShiftMs) {
		this.frameShiftMs = frameShiftMs;
	}

	public Scalar getFrameLengthMs() {
		return frameLengthMs;
	}

	public void setFrameLengthMs(Scalar frameLengthMs) {
		this.frameLengthMs = frameLengthMs;
	}

	public Scalar getDither() {
		return dither;
	}

	public void setDither(Scalar dither) {
		this.dither = dither;
	}

	public Scalar getPreemphCoeff() {
		return preemphCoeff;
	}

	public void setPreemphCoeff(Scalar preemphCoeff) {
		this.preemphCoeff = preemphCoeff;
	}

	public boolean isRemoveDcOffset() {
		return removeDcOffset;
	}

	public void setRemoveDcOffset(boolean removeDcOffset) {
		this.removeDcOffset = removeDcOffset;
	}

	public String getWindowType() {
		return windowType;
	}

	public void setWindowType(String windowType) {
		this.windowType = windowType;
	}

	public boolean isRoundToPowerOfTwo() {
		return roundToPowerOfTwo;
	}

	public void setRoundToPowerOfTwo(boolean roundToPowerOfTwo) {
		this.roundToPowerOfTwo = roundToPowerOfTwo;
	}

	public Scalar getBlackmanCoeff() {
		return blackmanCoeff;
	}

	public void setBlackmanCoeff(Scalar blackmanCoeff) {
		this.blackmanCoeff = blackmanCoeff;
	}

	public boolean isSnipEdges() {
		return snipEdges;
	}

	public void setSnipEdges(boolean snipEdges) {
		this.snipEdges = snipEdges;
	}

	public boolean isAllowDownsample() {
		return allowDownsample;
	}

	public void setAllowDownsample(boolean allowDownsample) {
		this.allowDownsample = allowDownsample;
	}

	public boolean isAllowUpsample() {
		return allowUpsample;
	}

	public void setAllowUpsample(boolean allowUpsample) {
		this.allowUpsample = allowUpsample;
	}

	public int getMaxFeatureVectors() {
		return maxFeatureVectors;
	}

	public void setMaxFeatureVectors(int maxFeatureVectors) {
		this.maxFeatureVectors = maxFeatureVectors;
	}

	public static int RoundUpToNearestPowerOfTwo(int n) {
		assert n > 0;
		n--;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return n + 1;
	}
}
