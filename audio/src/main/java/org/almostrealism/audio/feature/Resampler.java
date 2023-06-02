package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Scalar;
import org.almostrealism.collect.PackedCollection;

import java.util.ArrayList;
import java.util.List;

public class Resampler {
	private final WaveMath math;
	private final int sampRateIn;
	private final int sampRateOut;
	private final Scalar filterCutoff;
	private final int numZeros;
	private final int inputSamplesInUnit;
	private final int outputSamplesInUnit;
	private long inputSampleOffset;
	private long outputSampleOffset;
	private List<Scalar> inputRemainder;
	private PackedCollection<Scalar> firstIndex;
	private List<PackedCollection<Scalar>> weights;

	public Resampler(int sampRateInHz, int sampRateOutHz,
					 Scalar filterCutoffHz, int numZeros) {
		this.math = new WaveMath();
		this.sampRateIn = sampRateInHz;
		this.sampRateOut = sampRateOutHz;
		this.filterCutoff = filterCutoffHz;
		this.numZeros = numZeros;
		assert sampRateInHz > 0.0 &&
				sampRateOutHz > 0.0 &&
				filterCutoffHz.getValue() > 0.0 &&
				filterCutoffHz.getValue() * 2 <= sampRateInHz &&
				filterCutoffHz.getValue() * 2 <= sampRateOutHz &&
				numZeros > 0;

		// baseFreq is the frequency of the repeating unit, which is the gcd
		// of the input frequencies.
		int baseFreq = WaveMath.gcd(sampRateIn, sampRateOut);
		inputSamplesInUnit = sampRateIn / baseFreq;
		outputSamplesInUnit = sampRateOut / baseFreq;

		setIndexesAndWeights();
		reset();
	}

	void reset() {
		inputSampleOffset = 0;
		outputSampleOffset = 0;
		inputRemainder = new ArrayList<>();
	}

	long getNumOutputSamples(long inputNumSamp,
							 boolean flush) {
		// For exact computation, we measure time in "ticks" of 1.0 / tickFreq,
		// where tickFreq is the least common multiple of sampRateIn and
		// sampRateOut.
		int tickFreq = WaveMath.lcm(sampRateIn, sampRateOut);
		int ticksPerInputPeriod = tickFreq / sampRateIn;

		// work out the number of ticks in the time interval
		// [ 0, inputNumSamp/sampRateIn).
		long intervalLengthInTicks = inputNumSamp * ticksPerInputPeriod;
		if (!flush) {
			Scalar windowWidth = new Scalar(numZeros / (2.0 * filterCutoff.getValue()));
			// To count the window-width in ticks we take the floor.  This
			// is because since we're looking for the largest integer num-out-samp
			// that fits in the interval, which is open on the right, a reduction
			// in interval length of less than a tick will never make a difference.
			// For example, the largest integer in the interval [ 0, 2 ) and the
			// largest integer in the interval [ 0, 2 - 0.9 ) are the same (both one).
			// So when we're subtracting the window-width we can ignore the fractional
			// part.
			int windowWidthTicks = (int) Math.floor(windowWidth.getValue() * tickFreq);
			// The time-period of the output that we can sample gets reduced
			// by the window-width (which is actually the distance from the
			// center to the edge of the windowing function) if we're not
			// "flushing the output".
			intervalLengthInTicks -= windowWidthTicks;
		}

		if (intervalLengthInTicks <= 0)
			return 0;
		int ticksPerOutputPeriod = tickFreq / sampRateOut;
		// Get the last output-sample in the closed interval, i.e. replacing [ ) with
		// [ ].  Note: integer division rounds down.  See
		// http://en.wikipedia.org/wiki/Interval_(mathematics) for an explanation of
		// the notation.
		long lastOutputSamp = intervalLengthInTicks / ticksPerOutputPeriod;
		// We need the last output-sample in the open interval, so if it takes us to
		// the end of the interval exactly, subtract one.
		if (lastOutputSamp * ticksPerOutputPeriod == intervalLengthInTicks)
			lastOutputSamp--;
		// First output-sample index is zero, so the number of output samples
		// is the last output-sample plus one.
		return lastOutputSamp + 1;
	}

	void setIndexesAndWeights() {
		firstIndex = Scalar.scalarBank(outputSamplesInUnit);
		weights = new ArrayList<>();

		double windowWidth = numZeros / (2.0 * filterCutoff.getValue());

		for (int i = 0; i < outputSamplesInUnit; i++) {
			double outputT = i / (double) sampRateOut;
			double minT = outputT - windowWidth, maxT = outputT + windowWidth;
			// we do ceil on the min and floor on the max, because if we did it
			// the other way around we would unnecessarily include indexes just
			// outside the window, with zero coefficients.  It's possible
			// if the arguments to the ceil and floor expressions are integers
			// (e.g. if filterCutoff has an exact ratio with the sample rates),
			// that we unnecessarily include something with a zero coefficient,
			// but this is only a slight efficiency issue.
			int minInputIndex = (int) Math.ceil(minT * sampRateIn),
					maxInputIndex = (int) Math.floor(maxT * sampRateIn);
			firstIndex.set(i, minInputIndex);
			weights.add(null);
			int numIndices = maxInputIndex - minInputIndex + 1;
			weights.set(i, Scalar.scalarBank(numIndices));
			for (int j = 0; j < numIndices; j++) {
				int inputIndex = minInputIndex + j;
				double inputT = inputIndex / (double) sampRateIn,
						deltaT = inputT - outputT;
				// sign of deltaT doesn't matter.
				weights.get(i).set(j, new Scalar(filterFunc(new Scalar(deltaT)).getValue() / sampRateIn));
			}
		}
	}

	// TODO  inline?
	double[] getIndexes(long sampOut) {
		// A unit is the smallest nonzero amount of time that is an exact
		// multiple of the input and output sample periods.  The unit index
		// is the answer to "which numbered unit we are in".
		long unitIndex = sampOut / outputSamplesInUnit;
		// sampOutWrapped is equal to sampOut % outputSamplesInUnit

		double v[] = new double[2];
		v[1] = sampOut - unitIndex * outputSamplesInUnit;
		v[0] = (int) firstIndex.get((int) v[1]).getValue() + unitIndex * inputSamplesInUnit;
		return v;
	}


	PackedCollection<Scalar> resample(PackedCollection<Scalar> input, boolean flush) {
		int inputDim = input.getCount();
		long totInputSamp = inputSampleOffset + inputDim,
				totOutputSamp = getNumOutputSamples(totInputSamp, flush);

		if (totOutputSamp >= Integer.MAX_VALUE) {
			throw new UnsupportedOperationException("Cannot resample");
		}

		PackedCollection<Scalar> output = Scalar.scalarBank((int) totOutputSamp);

		assert totOutputSamp >= outputSampleOffset;

		// sampOut is the index into the total output signal, not just the part
		// of it we are producing here.
		for (long sampOut = outputSampleOffset;
			 sampOut < totOutputSamp;
			 sampOut++) {
			double indexes[] = getIndexes(sampOut);
			PackedCollection<Scalar> weights = this.weights.get((int) indexes[1]);
			// firstInputIndex is the first index into "input" that we have a weight
			// for.
			int firstInputIndex = (int) (indexes[0] - inputSampleOffset);
			Scalar thisOutput;

			if (firstInputIndex >= 0 &&
					firstInputIndex + weights.getCount() <= inputDim) {
				PackedCollection<Scalar> inputPart = listSegment(input, firstInputIndex, weights.getCount());
				thisOutput = math.dot(inputPart, weights);
			} else {  // Handle edge cases.
				thisOutput = new Scalar(0.0);
				for (int i = 0; i < weights.getCount(); i++) {
					Scalar weight = weights.get(i);
					int inputIndex = firstInputIndex + i;
					if (inputIndex < 0 && inputRemainder.size() + inputIndex >= 0) {
						thisOutput = new Scalar(thisOutput.getValue() + weight.getValue() *
								inputRemainder.get(inputRemainder.size() + inputIndex).getValue());
					} else if (inputIndex >= 0 && inputIndex < inputDim) {
						thisOutput = new Scalar(thisOutput.getValue() + weight.getValue() * input.get(inputIndex).getValue());
					} else assert inputIndex < inputDim || flush;
					// We're past the end of the input and are adding zero; should only
					// happen if the user specified flush == true, or else we would not
					// be trying to output this sample.
				}
			}
			int outputIndex = (int) (sampOut - outputSampleOffset);

			output.set(outputIndex, thisOutput);
		}

		if (flush) {
			reset();  // Reset the internal state.
		} else {
			setRemainder(input);
			inputSampleOffset = totInputSamp;
			outputSampleOffset = totOutputSamp;
		}
		
		return output;
	}

	// TODO  This doesn't look right. What data is impacted by this method?
	void setRemainder(PackedCollection<Scalar> input) {
		List<Scalar> oldRemainder = new ArrayList<>(inputRemainder);
		// maxRemainderNeeded is the width of the filter from side to side,
		// measured in input samples.  you might think it should be half that,
		// but you have to consider that you might be wanting to output samples
		// that are "in the past" relative to the beginning of the latest
		// input... anyway, storing more remainder than needed is not harmful.
		int maxRemainderNeeded = (int) Math.ceil(sampRateIn * numZeros /
				filterCutoff.getValue());

		for (int index = -inputRemainder.size(); index < 0; index++) {
			// we interpret "index" as an offset from the end of "input" and
			// from the end of inputRemainder.
			int inputIndex = index + input.getCount();
			if (inputIndex >= 0)
				inputRemainder.set(index + inputRemainder.size(), input.get(inputIndex));
			else if (inputIndex + oldRemainder.size() >= 0)
				inputRemainder.set(index + inputRemainder.size(),
						oldRemainder.get(inputIndex + oldRemainder.size()));
			// else leave it at zero.
		}
	}

	/**
	 * Here, t is a time in seconds representing an offset from
	 * the center of the windowed filter function, and FilterFunction(t)
	 * returns the windowed filter function, described
	 * in the header as h(t) = f(t)g(t), evaluated at t.
	 */
	private Scalar filterFunc(Scalar t) {
		Scalar window;  // raised-cosine (Hanning) window of width
						// numZeros / 2 * filterCutoff
		if (Math.abs(t.getValue()) < numZeros / (2.0 * filterCutoff.getValue()))
			window = new Scalar(0.5 * (1 + Math.cos(2 * Math.PI * filterCutoff.getValue() / numZeros * t.getValue())));
		else
			window = new Scalar(0.0);  // outside support of window function

		Scalar filter;// sinc filter function
		if (t.getValue() != 0)
			filter = new Scalar(Math.sin(2 * Math.PI * filterCutoff.getValue() * t.getValue()) / (Math.PI * t.getValue()));
		else
			filter = new Scalar(2 * filterCutoff.getValue());  // limit of the function at t = 0
		return new Scalar(filter.getValue() * window.getValue());
	}

	private void setWeights(List<Scalar> samplePoints) {
		int numSamplesOut = numSamplesOut();
		for (int i = 0; i < numSamplesOut; i++) {
			for (int j = 0; j < weights.get(i).getCount(); j++) {
				Scalar deltaT = new Scalar(samplePoints.get(i).getValue() -
						((int) firstIndex.get(i).getValue() + j) / sampRateIn);
				// Include at this point the factor of 1.0 / sampRateIn which
				// appears in the math.
				weights.get(i).set(j, new Scalar(filterFunc(deltaT).getValue() / sampRateIn));
			}
		}
	}

	private int numSamplesOut() { return weights.size(); }

	public static PackedCollection<Scalar> resampleWaveform(Scalar origFreq, PackedCollection<Scalar> wave, Scalar newFreq) {
		Scalar minFreq = new Scalar(Math.min(origFreq.getValue(), newFreq.getValue()));
		Scalar lowpassCutoff = new Scalar(0.99 * 0.5 * minFreq.getValue());
		int lowpassFilterWidth = 6;
		Resampler resampler = new Resampler((int) origFreq.getValue(), (int) newFreq.getValue(),
				lowpassCutoff, lowpassFilterWidth);
		return resampler.resample(wave, true);
	}

	private PackedCollection<Scalar> listSegment(PackedCollection<Scalar> input, int index, int len) {
		PackedCollection<Scalar> output = Scalar.scalarBank(len);
		for (int i = 0; i < len; i++) {
			output.set(i, input.get(i + index));
		}
		return output;
	}
}
