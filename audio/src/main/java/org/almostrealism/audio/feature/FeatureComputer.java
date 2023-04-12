package org.almostrealism.audio.feature;

import org.almostrealism.algebra.Pair;
import org.almostrealism.algebra.ScalarTable;
import org.almostrealism.audio.computations.ComplexFFT;
import org.almostrealism.audio.computations.WindowPreprocess;
import org.almostrealism.audio.util.TensorRow;
import io.almostrealism.relation.Evaluable;
import org.almostrealism.algebra.PairBank;
import org.almostrealism.algebra.Scalar;
import org.almostrealism.algebra.ScalarBank;
import org.almostrealism.algebra.Tensor;
import org.almostrealism.algebra.computations.ScalarBankSum;
import org.almostrealism.audio.computations.SplitRadixFFT;
import org.almostrealism.CodeFeatures;
import org.almostrealism.collect.PackedCollection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class FeatureComputer implements CodeFeatures {
	public static boolean enableVerbose = false;

	private static final double epsilon = 0.00000001;

	private final FeatureSettings settings;
	private final FeatureWindowFunction featureWindowFunction;

	private final WaveMath math;

	private final Evaluable<? extends PackedCollection<Pair<?>>> fft;

	private final Evaluable<? extends ScalarBank> processWindow;
	private Evaluable<? extends ScalarBank> preemphasizeAndWindowFunctionAndPad;

	private final Evaluable<ScalarBank> powerSpectrum;

	private ScalarBank lifterCoeffs;
	private final ScalarTable dctMatrix;  // matrix we left-multiply by to perform DCT.
	private Scalar logEnergyFloor;
	private final Map<Double, MelBanks> allMelBanks;  // BaseFloat is VTLN coefficient.

	private final ScalarBank melEnergies;

	private final ScalarBank windowInput;
	private final Scalar rawLogEnergy;
	private final PackedCollection<Pair<?>> complexSignalFrame;
	private final ScalarBank featureEnergies;

	private int featureCount = -1;
	private Tensor<Double> logTensor;

	public FeatureComputer(FeatureSettings settings) {
		this.settings = settings;
		this.featureWindowFunction = new FeatureWindowFunction(settings.getFrameExtractionSettings());
		this.allMelBanks = new HashMap<>();
		this.math = new WaveMath();
		this.logTensor = new Tensor<>();

		int binCount = this.settings.getMelBanksSettings().getNumBins();
		this.melEnergies = new ScalarBank(binCount);

		if (this.settings.getNumCeps() > binCount)
			System.err.println("num-ceps cannot be larger than num-mel-bins."
					+ " It should be smaller or equal. You provided num-ceps: "
					+ settings.getNumCeps() + "  and num-mel-bins: "
					+ binCount);

		ScalarTable dctm = new ScalarTable(binCount, binCount);
		computeDctMatrix(dctm, binCount, binCount);

		int paddedWindowSize = this.settings.getFrameExtractionSettings().getPaddedWindowSize();

		windowInput = new ScalarBank(paddedWindowSize);
		rawLogEnergy = new Scalar(0.0);
		complexSignalFrame = Pair.bank(paddedWindowSize);

		// Note that we include zeroth dct in either case.  If using the
		// energy we replace this with the energy.  This means a different
		// ordering of features than HTK.
		// dctMatrix.trim(this.settings.getNumCeps(), binCount);
		dctMatrix = dctm.copy(this.settings.getNumCeps(), binCount);
		featureEnergies = new ScalarBank(this.settings.getNumCeps());
		if (this.settings.getCepstralLifter().getValue() != 0.0) {
			lifterCoeffs = new ScalarBank(this.settings.getNumCeps());
			computeLifterCoeffs(this.settings.getCepstralLifter().getValue(), lifterCoeffs);
		}
		if (this.settings.getEnergyFloor().getValue() > 0.0)
			logEnergyFloor = new Scalar(Math.log(this.settings.getEnergyFloor().getValue()));

		PackedCollection<Pair<?>> fftOutput = Pair.bank(paddedWindowSize);

		fft = new ComplexFFT(paddedWindowSize, true, v(2 * paddedWindowSize, 0));

		int count = settings.getFrameExtractionSettings().getWindowSize();
		Supplier<Evaluable<? extends ScalarBank>> processWindow = null;

		if (processWindow == null) {
			processWindow = v(2 * count, 0);
			processWindow = dither(count, processWindow, v(Scalar.class, 1));

			if (settings.getFrameExtractionSettings().isRemoveDcOffset()) {
				processWindow = scalarBankAdd(count, processWindow, new ScalarBankSum(count, processWindow).divide(count).multiply(-1));
			}
		}

		this.processWindow = processWindow.get();

		if (preemphasizeAndWindowFunctionAndPad == null) {
			this.preemphasizeAndWindowFunctionAndPad = new WindowPreprocess(settings.getFrameExtractionSettings(),
																		v(2 * count, 0)).get();
		}

		this.powerSpectrum = powerSpectrum(paddedWindowSize, v(2 * paddedWindowSize, 0)).get();

		// We'll definitely need the filterbanks info for VTLN warping factor 1.0.
		// [note: this call caches it.]
		getMelBanks(1.0);
	}

	public void setLogTensor(Tensor<Double> logTensor) {
		this.logTensor = logTensor;
	}

	public Tensor<Double> getLogTensor() {
		return logTensor;
	}

	protected void computeDctMatrix(ScalarTable M, int K, int N) {
//		MatrixIndexT K = M->NumRows();
//		MatrixIndexT N = M->NumCols();

		assert K > 0;
		assert N > 0;
		Scalar normalizer = new Scalar(Math.sqrt(1.0 / N));  // normalizer for
															 // X.0.
		for (int j = 0; j < N; j++) M.set(0, j, normalizer);
		normalizer = new Scalar(Math.sqrt(2.0 / N));  // normalizer for other
													  // elements.
		for (int k = 1; k < K; k++)
			for (int n = 0; n < N; n++)
				M.set(k, n, new Scalar(normalizer.getValue() * Math.cos(Math.PI /N * (n + 0.5) * k)));
	}

	protected void computeLifterCoeffs(double Q, ScalarBank coeffs) {
		// Compute liftering coefficients (scaling on cepstral coeffs)
		// coeffs are numbered slightly differently from HTK: the zeroth
		// index is C0, which is not affected.
		for (int i = 0; i < coeffs.getCount(); i++)
			coeffs.set(i, 1.0 + 0.5 * Q * Math.sin(Math.PI * i / Q));
	}

	public void computeFeatures(ScalarBank wave,
								Scalar sampleFreq,
								Tensor<Scalar> output) {
		computeFeatures(wave, sampleFreq, 1.0, output);
	}

	public void computeFeatures(ScalarBank wave,
								Scalar sampleFreq,
								double vtlnWarp,
								Tensor<Scalar> output) {
		Scalar newSampleFreq = settings.getFrameExtractionSettings().getSampFreq();
		if (sampleFreq.getValue() == newSampleFreq.getValue()) {
			compute(wave, vtlnWarp, output);
		} else {
			if (newSampleFreq.getValue() < sampleFreq.getValue() &&
					!settings.getFrameExtractionSettings().isAllowDownsample())
				throw new IllegalArgumentException("Waveform and config sample Frequency mismatch: "
						+ sampleFreq + " .vs " + newSampleFreq);
			else if (newSampleFreq.getValue() > sampleFreq.getValue() &&
					!settings.getFrameExtractionSettings().isAllowUpsample())
				throw new IllegalArgumentException("Waveform and config sample Frequency mismatch: "
						+ sampleFreq + " .vs " + newSampleFreq);

			// Resample the waveform
			ScalarBank resampledWave = Resampler.resampleWaveform(sampleFreq, wave, newSampleFreq);
			compute(resampledWave, vtlnWarp, output);
		}
	}

	public void computeFeatures(ScalarBank wave,
								Scalar sampleFreq,
								BiConsumer<Integer, ScalarBank> output) {
		computeFeatures(wave, sampleFreq, 1.0, output);
	}

	public void computeFeatures(ScalarBank wave,
								Scalar sampleFreq,
								double vtlnWarp,
								BiConsumer<Integer, ScalarBank> output) {
		Scalar newSampleFreq = settings.getFrameExtractionSettings().getSampFreq();
		if (sampleFreq.getValue() == newSampleFreq.getValue()) {
			compute(wave, vtlnWarp, output);
		} else {
			if (newSampleFreq.getValue() < sampleFreq.getValue() &&
					!settings.getFrameExtractionSettings().isAllowDownsample())
				throw new IllegalArgumentException("Waveform and config sample Frequency mismatch: "
						+ sampleFreq + " .vs " + newSampleFreq);
			else if (newSampleFreq.getValue() > sampleFreq.getValue() &&
					!settings.getFrameExtractionSettings().isAllowUpsample())
				throw new IllegalArgumentException("Waveform and config sample Frequency mismatch: "
						+ sampleFreq + " .vs " + newSampleFreq);

			// Resample the waveform
			ScalarBank resampledWave = Resampler.resampleWaveform(sampleFreq, wave, newSampleFreq);
			compute(resampledWave, vtlnWarp, output);
		}
	}

	protected void compute(ScalarBank wave, double vtlnWarp, Tensor<Scalar> output) {
		compute(wave, vtlnWarp,
				(r, v) -> IntStream.range(0, v.getCount())
						.forEach(i ->
								new TensorRow<>(output, r).set(i, new Scalar(featureEnergies.get(i).getValue()))));
	}

	public void compute(ScalarBank wave, double vtlnWarp, BiConsumer<Integer, ScalarBank> output) {
		int rowsOut = numFrames(wave.getCount(), settings.getFrameExtractionSettings(), false);

		if (rowsOut == 0) {
			return;
		}

		boolean useRawLogEnergy = settings.isNeedRawLogEnergy();
		for (int r = 0; r < rowsOut; r++) {
			featureCount++;
			logTensor.insert((double) featureCount, featureCount, 0); // Number column
			ScalarBank window = extractWindow(0, wave, r, settings.getFrameExtractionSettings(),
					featureWindowFunction, windowInput,
					useRawLogEnergy ? rawLogEnergy : null);

			long start = System.currentTimeMillis();
			compute(rawLogEnergy, vtlnWarp, window, featureEnergies);
			output.accept(r, featureEnergies);
			if (enableVerbose) System.out.println("-----> " + (System.currentTimeMillis() - start) + " total");
		}
	}

	private double dot(ScalarBank x, ScalarBank y) {
		return Math.max(
				IntStream.range(0, x.getCount()).mapToDouble(i ->
						x.get(i).x() * y.get(i).x()).sum(), epsilon);
	}

	private double logDot(ScalarBank x, ScalarBank y) {
		return Math.log(dot(x, y));
	}

	private void removeDcOffset(ScalarBank window) {
		double dcOffset = window.stream().mapToDouble(Scalar::getValue).sum();
		window.forEach(scalar -> scalar.setValue(scalar.getValue() - dcOffset));
	}

	protected void compute(Scalar signalRawLogEnergy,
						double vtlnWarp,
						ScalarBank realSignalFrame,
						ScalarBank feature) {
		assert realSignalFrame.getCount() == settings.getFrameExtractionSettings().getPaddedWindowSize();

		MelBanks melBanks = getMelBanks(vtlnWarp);

		if (settings.isUseEnergy() && !settings.isRawEnergy()) {
			signalRawLogEnergy.setValue(Math.log(Math.max(math.dot(realSignalFrame, realSignalFrame).getValue(), epsilon)));
		}

		long start = System.currentTimeMillis();

		PackedCollection<Pair<?>> signalFrame;

		if (fft != null) {
			signalFrame = fft.evaluate(toPairBank(realSignalFrame, complexSignalFrame));
		} else {
			throw new UnsupportedOperationException();
		}

		if (enableVerbose) System.out.println("--> FFT: " + (System.currentTimeMillis() - start));

		// Convert the FFT into a power spectrum.
		start = System.currentTimeMillis();
		ScalarBank powerSpectrum = this.powerSpectrum.evaluate(signalFrame).range(0, signalFrame.getCount() / 2 + 1);
		if (enableVerbose) System.out.println("--> computePowerSpectrum: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		melBanks.compute(powerSpectrum, melEnergies);
		if (enableVerbose) System.out.println("--> melBanks: " + (System.currentTimeMillis() - start));

		// avoid log of zero (which should be prevented anyway by dithering).
		start = System.currentTimeMillis();
		melEnergies.applyFloor(FeatureComputer.epsilon);
		melEnergies.applyLog();  // take the log.
		if (enableVerbose) System.out.println("--> applyLog: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		feature.setZero();  // in case there were NaNs.
		feature.addMatVec(dctMatrix, melEnergies);
		if (enableVerbose) System.out.println("--> dctMatrix: " + (System.currentTimeMillis() - start));

		start = System.currentTimeMillis();
		if (settings.getCepstralLifter().getValue() != 0.0)
			feature.mulElements(lifterCoeffs);
		if (enableVerbose) System.out.println("--> lifterCoeffs: " + (System.currentTimeMillis() - start));

		if (settings.isUseEnergy()) {
			if (settings.getEnergyFloor().getValue() > 0.0 &&
					signalRawLogEnergy.getValue() < logEnergyFloor.getValue()) {
				if (enableVerbose) System.out.println("Assigning energy floor: " + logEnergyFloor.getValue());
				signalRawLogEnergy.setValue(logEnergyFloor.getValue());
			}

			feature.set(0, signalRawLogEnergy);
		}

		if (settings.isHtkCompat()) {
			double energy = feature.get(0).getValue();
			for (int i = 0; i < settings.getNumCeps() - 1; i++)
				feature.set(i, feature.get(i + 1));

			if (!settings.isUseEnergy())
				energy *= SplitRadixFFT.SQRT_2;  // scale on C0 (actually removing a scale
			// we previously added that's part of one common definition of
			// the cosine transform.)
			feature.set(settings.getNumCeps() - 1, new Scalar(energy));
		}
	}

	private int numFrames(long numSamples, FrameExtractionSettings opts) {
		return numFrames(numSamples, opts, true);
	}

	private int numFrames(long numSamples, FrameExtractionSettings opts, boolean flush) {
		long frameShift = opts.getWindowShift();
		long frameLength = opts.getWindowSize();
		if (opts.isSnipEdges()) {
			// with --snip-edges=true (the default), we use a HTK-like approach to
			// determining the number of frames-- all frames have to fit completely into
			// the waveform, and the first frame begins at sample zero.
			if (numSamples < frameLength)
				return 0;
			else
				return (int) (1 + (numSamples - frameLength) / frameShift);
			// You can understand the expression above as follows: 'numSamples -
			// frameLength' is how much room we have to shift the frame within the
			// waveform; 'frameShift' is how much we shift it each time; and the ratio
			// is how many times we can shift it (integer arithmetic rounds down).
		} else {
			// if --snip-edges=false, the number of frames is determined by rounding the
			// (file-length / frame-shift) to the nearest integer.  The point of this
			// formula is to make the number of frames an obvious and predictable
			// function of the frame shift and signal length, which makes many
			// segmentation-related questions simpler.
			//
			// Because integer division in C++ rounds toward zero, we add (half the
			// frame-shift minus epsilon) before dividing, to have the effect of
			// rounding towards the closest integer.
			int numFrames = (int) ((numSamples + frameShift / 2) / frameShift);

			if (flush)
				return numFrames;

			// note: 'end' always means the last plus one, i.e. one past the last.
			long endSampleOfLastFrame = firstSampleOfFrame(numFrames - 1, opts) + frameLength;

			// the following code is optimized more for clarity than efficiency.
			// If flush == false, we can't output frames that extend past the end
			// of the signal.
			while (numFrames > 0 && endSampleOfLastFrame > numSamples) {
				numFrames--;
				endSampleOfLastFrame -= frameShift;
			}
			return numFrames;
		}
	}

	long firstSampleOfFrame(int frame, FrameExtractionSettings opts) {
		long frameShift = opts.getWindowShift();

		if (opts.isSnipEdges()) {
			return frame * frameShift;
		} else {
			long midpointOfFrame = frameShift * frame + frameShift / 2;
			return midpointOfFrame - opts.getWindowSize() / 2;
		}
	}

	// ExtractWindow extracts a windowed frame of waveform with a power-of-two,
	// padded size.  It does mean subtraction, pre-emphasis and dithering as
	// requested.
	ScalarBank extractWindow(long sampleOffset, ScalarBank wave,
					   int f,  // with 0 <= f < NumFrames(feats, opts)
					   FrameExtractionSettings opts,
					   FeatureWindowFunction windowFunction,
					   ScalarBank window,
					   Scalar logEnergyPreWindow) {

		assert sampleOffset >= 0 && wave.getCount() != 0;
		int frameLength = opts.getWindowSize();
		int frameLengthPadded = opts.getPaddedWindowSize();
		long numSamples = sampleOffset + wave.getCount(),
				startSample = firstSampleOfFrame(f, opts);

		if (enableVerbose) {
			// System.out.println("Wave: " + Arrays.toString(IntStream.range((int) startSample, (int) startSample + 24).mapToDouble(i -> wave.get(i).x()).toArray()));
		}

		if (opts.isSnipEdges()) {
			long endSample = startSample + frameLength;
			assert startSample >= sampleOffset &&
					endSample <= numSamples;
		} else {
			assert sampleOffset == 0 || startSample >= sampleOffset;
		}

		// waveStart and waveEnd are start and end indexes into 'wave', for the
		// piece of wave that we're trying to extract.
		int waveStart = (int) (startSample - sampleOffset);
		int waveEnd = waveStart + frameLength;
		if (waveStart >= 0 && waveEnd <= wave.getCount()) {
			// the normal case-- no edge effects to consider.
//			window->Range(0, frameLength).CopyFromVec(
//					wave.Range(waveStart, frameLength));
			for (int i = 0; i < frameLength; i++) {
				window.set(i, wave.get(waveStart + i));
			}
		} else {
			// Deal with any end effects by reflection, if needed.  This code will only
			// be reached for about two frames per utterance, so we don't concern
			// ourselves excessively with efficiency.
			int waveDim = wave.getCount();
			for (int s = 0; s < frameLength; s++) {
				int sInWave = s + waveStart;
				while (sInWave < 0 || sInWave >= waveDim) {
					// reflect around the beginning or end of the wave.
					// e.g. -1 -> 0, -2 -> 1.
					// dim -> dim - 1, dim + 1 -> dim - 2.
					// the code supports repeated reflections, although this
					// would only be needed in pathological cases.
					if (sInWave < 0) sInWave = -sInWave - 1;
					else sInWave = 2 * waveDim - 1 - sInWave;
				}

				window.set(s, wave.get(sInWave));
			}
		}

		// System.out.println(Arrays.toString(IntStream.range(0, 12).mapToDouble(i -> window.get(i).x()).toArray()));

		ScalarBank frame = window;
		if (frameLengthPadded > frameLength) frame = frame.range(0, frameLength);

		return processWindow(opts, frame, logEnergyPreWindow);
	}

	ScalarBank processWindow(FrameExtractionSettings opts,
					   ScalarBank window,
					   Scalar logEnergyPreWindow) {
		long start = System.currentTimeMillis();

		int frameLength = opts.getWindowSize();
		assert window.getCount() == frameLength;

		// System.out.println("dot(window) before processing: " + dot(window, window));
		// System.out.println("logDot(window) before processing: " + logDot(window, window));

		logTensor.insert(dot(window, window), featureCount, 1);
		logTensor.insert(logDot(window, window), featureCount, 2);

		// TODO window = processWindow.evaluate(window, settings.getFrameExtractionSettings().getDither());
		// removeDcOffset(window);

		if (logEnergyPreWindow != null) {
			double logDot = logDot(window, window);
			double logEnergy = Math.log(Math.max(math.dot(window, window).getValue(), epsilon));
			logTensor.insert(logDot, featureCount, 3);
			logTensor.insert(logEnergy, featureCount, 4);
			logEnergyPreWindow.setValue(logDot); // TODO
			if (Math.abs(logEnergyPreWindow.getValue() - logDot) > 0.0001) {
				throw new RuntimeException("Native energy computation appears to be wrong");
			}
		}

		window = preemphasizeAndWindowFunctionAndPad.evaluate(window);

		if (enableVerbose) System.out.println("--> processWindow: " + (System.currentTimeMillis() - start));

		return window;
	}

	private MelBanks getMelBanks(double vtlnWarp) {
		MelBanks melBanks;
		MelBanks val = allMelBanks.get(vtlnWarp);
		if (val == null) {
			melBanks = new MelBanks(settings.getMelBanksSettings(),
					settings.getFrameExtractionSettings(),
					new Scalar(vtlnWarp));
			allMelBanks.put(vtlnWarp, melBanks);
		} else {
			return val;
		}

		return melBanks;
	}

	static PackedCollection<Pair<?>> toPairBank(ScalarBank real) {
		return toPairBank(real, Pair.bank(real.getCount()));
	}

	static PackedCollection<Pair<?>> toPairBank(ScalarBank real, PackedCollection<Pair<?>> out) {
		IntStream.range(0, real.getCount()).forEach(i -> out.set(i, real.get(i).getValue(), 0.0));
		return out;
	}
}
