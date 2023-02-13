package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;

public class FeatureSettings {
	private FrameExtractionSettings frameExtractionSettings;
	private MelBanksSettings melBanksSettings;
	private int numCeps;  // e.g. 13: num cepstral coeffs, counting zero.
	private boolean useEnergy;  // use energy; else C0
	private Scalar energyFloor;  // 0 by default; set to a value like 1.0 or 0.1 if
	// you disable dithering.
	private boolean rawEnergy;  // If true, compute energy before preemphasis and windowing
	private Scalar cepstralLifter;  // Scaling factor on cepstra for HTK compatibility.
	// if 0.0, no liftering is done.
	private boolean htkCompat;  // if true, put energy/C0 last and introduce a factor of
	// sqrt(2) on C0 to be the same as HTK.

	public FeatureSettings() {
		this.frameExtractionSettings = new FrameExtractionSettings();
		this.melBanksSettings = new MelBanksSettings(23);
		// defaults the #mel-banks to 23 for the MFCC computations.
		// this seems to be common for 16khz-sampled data,
		// but for 8khz-sampled data, 15 may be better.
		this.numCeps = 13;
		this.useEnergy = true;
		this.energyFloor = new Scalar(0.0);
		this.rawEnergy = true;
		this.cepstralLifter = new Scalar(22.0);
		this.htkCompat = false;
	}

	public FrameExtractionSettings getFrameExtractionSettings() {
		return frameExtractionSettings;
	}

	public void setFrameExtractionSettings(FrameExtractionSettings frameExtractionSettings) {
		this.frameExtractionSettings = frameExtractionSettings;
	}

	public MelBanksSettings getMelBanksSettings() {
		return melBanksSettings;
	}

	public void setMelBanksSettings(MelBanksSettings melBanksSettings) {
		this.melBanksSettings = melBanksSettings;
	}

	public int getNumCeps() {
		return numCeps;
	}

	public void setNumCeps(int numCeps) {
		this.numCeps = numCeps;
	}

	public boolean isUseEnergy() {
		return useEnergy;
	}

	public void setUseEnergy(boolean useEnergy) {
		this.useEnergy = useEnergy;
	}

	public Scalar getEnergyFloor() {
		return energyFloor;
	}

	public void setEnergyFloor(Scalar energyFloor) {
		this.energyFloor = energyFloor;
	}

	public boolean isRawEnergy() {
		return rawEnergy;
	}

	public void setRawEnergy(boolean rawEnergy) {
		this.rawEnergy = rawEnergy;
	}

	public Scalar getCepstralLifter() {
		return cepstralLifter;
	}

	public void setCepstralLifter(Scalar cepstralLifter) {
		this.cepstralLifter = cepstralLifter;
	}

	public boolean isHtkCompat() {
		return htkCompat;
	}

	public void setHtkCompat(boolean htkCompat) {
		this.htkCompat = htkCompat;
	}

	public int size() { return numCeps; }

	public boolean isNeedRawLogEnergy() { return useEnergy && rawEnergy; }
}
