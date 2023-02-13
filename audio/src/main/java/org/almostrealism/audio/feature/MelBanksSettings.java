package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.CodeFeatures;

public class MelBanksSettings implements CodeFeatures {
	private int numBins;  // e.g. 25; number of triangular bins
	private Scalar lowFreq;  // e.g. 20; lower frequency cutoff
	private Scalar highFreq;  // an upper frequency cutoff; 0 -> no cutoff, negative
	// ->added to the Nyquist frequency to get the cutoff.
	private Scalar vtlnlow;  // vtln lower cutoff of warping function.
	private Scalar vtlnHigh;  // vtln upper cutoff of warping function: if negative, added
	// to the Nyquist frequency to get the cutoff.
	private boolean debugMel;
	// htk_mode is a "hidden" config, it does not show up on command line.
	// Enables more exact compatibility with HTK, for testing purposes.  Affects
	// mel-energy flooring and reproduces a bug in HTK.
	private boolean htkMode;

	public MelBanksSettings() {
		this(25);
	}

	public MelBanksSettings(int numBins) {
		this.numBins = numBins;
		this.lowFreq = new Scalar(20);
		this.highFreq = new Scalar(0);
		this.vtlnlow = new Scalar(100);
		this.vtlnHigh = new Scalar(-500);
		this.debugMel = false;
		this.htkMode = false;
	}

	public int getNumBins() {
		return numBins;
	}

	public void setNumBins(int numBins) {
		this.numBins = numBins;
	}

	public Scalar getLowFreq() {
		return lowFreq;
	}

	public void setLowFreq(Scalar lowFreq) {
		this.lowFreq = lowFreq;
	}

	public Scalar getHighFreq() {
		return highFreq;
	}

	public void setHighFreq(Scalar highFreq) {
		this.highFreq = highFreq;
	}

	public Scalar getVtlnlow() {
		return vtlnlow;
	}

	public void setVtlnlow(Scalar vtlnlow) {
		this.vtlnlow = vtlnlow;
	}

	public Scalar getVtlnHigh() {
		return vtlnHigh;
	}

	public void setVtlnHigh(Scalar vtlnHigh) {
		this.vtlnHigh = vtlnHigh;
	}

	public boolean isDebugMel() {
		return debugMel;
	}

	public void setDebugMel(boolean debugMel) {
		this.debugMel = debugMel;
	}

	public boolean isHtkMode() {
		return htkMode;
	}

	public void setHtkMode(boolean htkMode) {
		this.htkMode = htkMode;
	}
}
