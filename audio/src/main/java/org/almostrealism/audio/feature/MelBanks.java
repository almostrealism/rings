/*
 * Copyright 2023 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;

public class MelBanks implements CodeFeatures {

	private final WaveMath math;

	/**
	 * Center frequencies of bins, numbered from 0 ... num_bins-1.
	 */
	private final List<Scalar> centerFreqs;

	/**
	 * A pair, one for each bin: (the first nonzero fft-bin), (the vector of weights).
	 */
	private final List<Bin> bins;

	private final boolean debug;
	private final boolean htkMode;

	public MelBanks(MelBanksSettings opts, FrameExtractionSettings frameExtractionSettings,
					   Scalar vtlnWarpFactor) {
		this.math = new WaveMath();
		this.centerFreqs = new ArrayList<>();
		this.bins = new ArrayList<>();
		this.htkMode = opts.isHtkMode();
		int numBins = opts.getNumBins();
		if (numBins < 3) System.err.println("Must have at least 3 mel bins");
		Scalar sampleFreq = frameExtractionSettings.getSampFreq();
		int windowLengthPadded = frameExtractionSettings.getPaddedWindowSize();
		assert windowLengthPadded % 2 == 0;
		double nyquist = 0.5 * sampleFreq.getValue();

		Scalar lowFreq = opts.getLowFreq(), highFreq;
		if (opts.getHighFreq().getValue() > 0.0)
			highFreq = opts.getHighFreq();
		else
			highFreq = new Scalar(nyquist + opts.getHighFreq().getValue());

		if (lowFreq.getValue() < 0.0 || lowFreq.getValue() >= nyquist
				|| highFreq.getValue() <= 0.0 || highFreq.getValue() > nyquist
				|| highFreq.getValue() <= lowFreq.getValue()) {
			System.err.println("Bad values in options: low-freq " + lowFreq
					+ " and high-freq " + highFreq + " vs. nyquist "
					+ nyquist);
		}

		double fftBinWidth = sampleFreq.getValue() / windowLengthPadded;
		// fft-bin width [think of it as Nyquist-freq / half-window-length]

		double melLowFreq = melScale(lowFreq.getValue());
		double melHighFreq = melScale(highFreq.getValue());

		debug = opts.isDebugMel();

		// divide by numBins+1 in next line because of end-effects where the bins
		// spread out to the sides.

		Scalar vtlnLow = opts.getVtlnlow(),
				vtlnHigh = opts.getVtlnHigh();
		if (vtlnHigh.getValue() < 0.0) {
			vtlnHigh.setValue(vtlnHigh.getValue() + nyquist);
		}

		if (vtlnWarpFactor.getValue() != 1.0 &&
				(vtlnLow.getValue() < 0.0 || vtlnLow.getValue() <= lowFreq.getValue()
						|| vtlnLow.getValue() >= highFreq.getValue()
						|| vtlnHigh.getValue() <= 0.0 || vtlnHigh.getValue() >= highFreq.getValue()
						|| vtlnHigh.getValue() <= vtlnLow.getValue())) {
			System.err.println("Bad values in options: vtln-low " + vtlnLow
					+ " and vtln-high " + vtlnHigh + ", versus "
					+ "low-freq " + lowFreq + " and high-freq "
					+ highFreq);
		}

		int numFftBins = windowLengthPadded / 2;
		double melFreqDelta = (melHighFreq - melLowFreq) / (numBins + 1);
		for (int bin = 0; bin < numBins; bin++) {
			double leftMel = melLowFreq + bin * melFreqDelta,
					centerMel = melLowFreq + (bin + 1) * melFreqDelta,
					rightMel = melLowFreq + (bin + 2) * melFreqDelta;

			if (vtlnWarpFactor.getValue() != 1.0) {
				leftMel = vtlnWarpMelFreq(vtlnLow.getValue(), vtlnHigh.getValue(),
						lowFreq.getValue(), highFreq.getValue(),
						vtlnWarpFactor.getValue(), leftMel);
				centerMel = vtlnWarpMelFreq(vtlnLow.getValue(), vtlnHigh.getValue(),
						lowFreq.getValue(), highFreq.getValue(),
						vtlnWarpFactor.getValue(), centerMel);
				rightMel = vtlnWarpMelFreq(vtlnLow.getValue(), vtlnHigh.getValue(),
						lowFreq.getValue(), highFreq.getValue(),
						vtlnWarpFactor.getValue(), rightMel);
			}

			centerFreqs.add(null);
			centerFreqs.set(bin, new Scalar(inverseMelScale(centerMel)));

			// thisBin will be a vector of coefficients that is only
			// nonzero where this mel bin is active.
			PackedCollection<Scalar> thisBin = Scalar.scalarBank(numFftBins);
			int firstIndex = -1, lastIndex = -1;

			for (int i = 0; i < numFftBins; i++) {
				double freq = fftBinWidth * i;  // Center frequency of this fft bin
				double mel = melScale(freq);

				if (mel > leftMel && mel < rightMel) {
					double weight;

					if (mel <= centerMel) {
						weight = (mel - leftMel) / (centerMel - leftMel);
					} else {
						weight = (rightMel - mel) / (rightMel - centerMel);
					}

					thisBin.set(i, new Scalar(weight));

					if (firstIndex == -1)
						firstIndex = i;
					lastIndex = i;
				}
			}

			assert firstIndex != -1 && lastIndex >= firstIndex;

			bins.add(new Bin());
			bins.get(bin).setKey(firstIndex);
			int size = lastIndex + 1 - firstIndex;

			bins.get(bin).setValue(Scalar.scalarBank(size));
			bins.get(bin).getValue().copyFrom(thisBin.range(shape(size, 2), 2 * firstIndex));
			for (int i = 0; i < size; i++) {
				bins.get(bin).getValue().set(i, thisBin.get(firstIndex + i));
			}

			// Replicate a bug in HTK, for testing purposes.
			if (opts.isHtkMode() && bin == 0 && melLowFreq != 0.0)
				bins.get(bin).getValue().set(0, new Scalar(0.0));

		}

		if (debug) {
			for (int i = 0; i < bins.size(); i++) {
				System.out.println("bin " + i + ", offset = " + bins.get(i).getKey()
						+ ", vec = " + bins.get(i).getValue());
			}
		}
	}

	public int getNumBins() { return bins.size(); }

	/** returns vector of central freq of each bin; needed by plp code. */
	public List<Scalar> getCenterFreqs() { return centerFreqs; }

	public List<Bin> getBins() { return bins; }

	/**
	 * @param powerSpectrum  contains fft energies.
	 */
	public void compute(PackedCollection<Scalar> powerSpectrum, PackedCollection<Scalar> melEnergiesOut) {
		int numBins = bins.size();
		assert melEnergiesOut.getCount() == numBins;

		for (int i = 0; i < numBins; i++) {
			int offset = bins.get(i).getKey();
   			PackedCollection<Scalar> v = bins.get(i).getValue();
			// System.out.println("Bin(" + i + "):");
			// IntStream.range(0, v.getCount()).mapToObj(v::get).forEach(System.out::println);

			PackedCollection<Scalar> spec = powerSpectrum.range(v.getShape(), 2 * offset);
   			// System.out.println("Spectrum:");
   			// IntStream.range(0, spec.getCount()).mapToObj(spec::get).forEach(System.out::println);

   			Scalar r = math.dot(v, spec);
   			// System.out.println(r);
			// double energy = vecDot(v, powerSpectrum.range(offset, v.getCount())).getValue();
			double energy = r.getValue();
			// HTK-like flooring- for testing purposes (we prefer dither)
			if (htkMode && energy < 1.0) energy = 1.0;
			melEnergiesOut.set(i, energy);
		}

		if (debug) {
			System.err.print("MEL BANKS:\n");
			for (int i = 0; i < numBins; i++)
				System.err.print(" " + melEnergiesOut.get(i));
			System.err.print("\n");
		}
	}

	// Durbin's recursion - converts autocorrelation coefficients to the LPC
	// pTmp - temporal place [n]
	// pAC - autocorrelation coefficients [n + 1]
	// pLP - linear prediction coefficients [n] (predicted_sn = sum_1^P{a[i-1] * s[n-i]}})
	//       F(z) = 1 / (1 - A(z)), 1 is not stored in the demoninator
	protected static Scalar durbin(int n,
								   PackedCollection<Scalar> pAC,
								   PackedCollection<Scalar> pLP,
								   PackedCollection<Scalar> pTmp) {
		double E = pAC.get(0).getValue();

		for (int i = 0; i < n; i++) {
			// next reflection coefficient
			// reflection coefficient
			double ki = pAC.get(i + 1).getValue();
			for (int j = 0; j < i; j++)
				ki += pLP.get(j).getValue() * pAC.get(i - j).getValue();
			ki = ki / E;

			// new error
			double c = 1 - ki * ki;
			if (c < 1.0e-5) // remove NaNs for constan signal
				c = 1.0e-5;
			E *= c;

			// new LP coefficients
			pTmp.set(i, -ki);
			for (int j = 0; j < i; j++)
				pTmp.set(j, pLP.get(j).getValue() - ki * pLP.get(i - j - 1).getValue());

			for (int j = 0; j <= i; j++)
				pLP.set(j, pTmp.get(j));
		}

		return new Scalar(E);
	}

	protected static double inverseMelScale(double melFreq) {
		return 700.0f * (Math.exp(melFreq / 1127.0f) - 1.0f);
	}

	protected static double melScale(double freq) {
		return 1127.0f * Math.log(1.0f + freq / 700.0f);
	}

	protected static double vtlnWarpFreq(double vtlnLowCutoff,  // upper+lower frequency cutoffs for VTLN.
									  double vtlnHighCutoff,
									  double lowFreq,  // upper+lower frequency cutoffs in mel computation
									  double highFreq,
									  double vtlnWarpFactor,
									  double freq) {
		/// This computes a VTLN warping function that is not the same as HTK's one,
		/// but has similar inputs (this function has the advantage of never producing
		/// empty bins).

		/// This function computes a warp function F(freq), defined between lowFreq and
		/// highFreq inclusive, with the following properties:
		///  F(lowFreq) == lowFreq
		///  F(highFreq) == highFreq
		/// The function is continuous and piecewise linear with two inflection
		///   points.
		/// The lower inflection point (measured in terms of the unwarped
		///  frequency) is at frequency l, determined as described below.
		/// The higher inflection point is at a frequency h, determined as
		///   described below.
		/// If l <= f <= h, then F(f) = f/vtlnWarpFactor.
		/// If the higher inflection point (measured in terms of the unwarped
		///   frequency) is at h, then max(h, F(h)) == vtlnHighCutoff.
		///   Since (by the last point) F(h) == h/vtlnWarpFactor, then
		///   max(h, h/vtlnWarpFactor) == vtlnHighCutoff, so
		///   h = vtlnHighCutoff / max(1, 1/vtlnWarpFactor).
		///     = vtlnHighCutoff * min(1, vtlnWarpFactor).
		/// If the lower inflection point (measured in terms of the unwarped
		///   frequency) is at l, then min(l, F(l)) == vtlnLowCutoff
		///   This implies that l = vtlnLowCutoff / min(1, 1/vtlnWarpFactor)
		///                       = vtlnLowCutoff * max(1, vtlnWarpFactor)


		if (freq < lowFreq || freq > highFreq) return freq;  // in case this gets called
		// for out-of-range frequencies, just return the freq.

		assert vtlnLowCutoff > lowFreq; // "be sure to set the --vtln-low option higher than --low-freq"
		assert vtlnHighCutoff < highFreq; // "be sure to set the --vtln-high option lower than --high-freq [or negative]");
		double one = 1.0;
		double l = vtlnLowCutoff * Math.max(one, vtlnWarpFactor);
		double h = vtlnHighCutoff * Math.min(one, vtlnWarpFactor);
		assert l > lowFreq && h < highFreq;

		// [slope of center part is just "scale"]
		double scale = 1.0 / vtlnWarpFactor;

		if (freq < l) {
			// slope of left part of the 3-piece linear function
			// F(l);
			double Fl = scale * l;
			double scale_left = (Fl - lowFreq) / (l - lowFreq);
			return lowFreq + scale_left * (freq - lowFreq);
		} else if (freq < h) {
			return scale * freq;
		} else {  // freq >= h
			// slope of right part of the 3-piece linear function
			// F(h);
			double Fh = scale * h;
			double scaleRight = (highFreq - Fh) / (highFreq - h);
			return highFreq + scaleRight * (freq - highFreq);
		}
	}

	protected static double vtlnWarpMelFreq(double vtlnLowCutoff,  // upper+lower frequency cutoffs for VTLN.
										 double vtlnHighCutoff,
										 double lowFreq,  // upper+lower frequency cutoffs in mel computation
										 double highFreq,
										 double vtlnWarpFactor,
										 double melFreq) {
		return melScale(vtlnWarpFreq(vtlnLowCutoff, vtlnHighCutoff,
				lowFreq, highFreq,
				vtlnWarpFactor, inverseMelScale(melFreq)));
	}
}
