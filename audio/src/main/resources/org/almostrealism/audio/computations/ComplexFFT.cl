#define mcr(p, q, r, s) (p * r - q * s)
#define mci(p, q, r, s) (p * s + q * r)

void copyGlobalToLocal(__global float *input, float *output,
                                int offset, int length) {
    for (int i = 0; i < length; i++) output[i] = input[offset + i];
}

 void copyLocalToGlobal(float *input, __global float *output,
                                int offset, int length) {
    for (int i = 0; i < length; i++) output[offset + i] = input[i];
}

void calculateRadix2Transform(float *output, float *input, int len, int inverseTransform, int isFirstSplit) {
	float even[512];
	float odd[512];
	float evenFFT[512];
    float oddFFT[512];

	if (len >= 2) {
		int halfN = len / 2;
		float angle = 2 * M_PI_F / len;

		if (!inverseTransform) {
			angle *= -1;
		}

		for (int k = 0; k < halfN; k++) {
			int kPlusHalfN = k + halfN;
			float angleK = angle * k;
			float omega_r = cos(angleK);
			float omega_i = sin(angleK);

			even[2 * k] = input[2 * k] + input[2 * kPlusHalfN];
			even[2 * k + 1] = input[2 * k + 1] + input[2 * kPlusHalfN + 1];

			float in_kMinus_in_kPlusHalfN_r = input[2 * k] - input[2 * kPlusHalfN];
			float in_kMinus_in_kPlusHalfN_i = input[2 * k + 1] - input[2 * kPlusHalfN + 1];
			odd[2 * k] = mcr(in_kMinus_in_kPlusHalfN_r, in_kMinus_in_kPlusHalfN_i, omega_r, omega_i);
			odd[2 * k + 1] = mci(in_kMinus_in_kPlusHalfN_r, in_kMinus_in_kPlusHalfN_i, omega_r, omega_i);
		}

		calculateRadix2Transform(evenFFT, even, halfN, inverseTransform, false);
		calculateRadix2Transform(oddFFT, odd, halfN, inverseTransform, false);

	    for (int k = 0; k < halfN; k++) {
			int doubleK = k * 2;

			if (inverseTransform > 0 && isFirstSplit > 0) {
			    output[2 * doubleK] = evenFFT[2 * k] / len;
			    output[2 * doubleK + 1] = evenFFT[2 * k + 1] / len;
			    output[2 * (doubleK + 1)] = oddFFT[2 * k] / len;
			    output[2 * (doubleK + 1) + 1] = oddFFT[2 * k + 1] / len;
			} else {
				output[2 * doubleK] = evenFFT[2 * k];
            	output[2 * doubleK + 1] = evenFFT[2 * k + 1];
            	output[2 * (doubleK + 1)] = oddFFT[2 * k];
            	output[2 * (doubleK + 1) + 1] = oddFFT[2 * k + 1];
			}
		}
	} else {
		for (int i = 0; i < 2 * len; i++) output[i] = input[i];
	}
}

 void calculateTransform(float *output, float *input, int len, int inverseTransform, int isFirstSplit) {
    float radix2[512];
	float radix4Part1[256];
	float radix4Part2[256];
	float radix2FFT[512];
	float radix4Part1FFT[256];
	float radix4Part2FFT[256];

	if (len >= 4) {
		int halfN = len / 2;
		int quarterN = halfN / 2;
		int tripleQuarterN = 3 * quarterN;

		float angle = 2 * M_PI_F / len;
		float i;

		if (inverseTransform <= 0) {
			angle *= -1;
			i = 1;
		} else {
			i = -1;
		}

		for (int k = 0; k < quarterN; k++) {
			int kPlusTripleQuarterN = k + tripleQuarterN;
			int kPlusHalfN = k + halfN;
			int kPlusQuarterN = k + quarterN;
			float ar = input[2 * k];
			float ai = input[2 * k + 1];
			float br = input[2 * kPlusQuarterN];
			float bi = input[2 * kPlusQuarterN + 1];
			float cr = input[2 * kPlusHalfN];
			float ci = input[2 * kPlusHalfN + 1];
			float dr = input[2 * kPlusTripleQuarterN];
			float di = input[2 * kPlusTripleQuarterN + 1];

			//radix-2 part
			radix2[2 * k] = ar + cr;
			radix2[2 * k + 1] = ai + ci;
			radix2[2 * (k + quarterN)] = br + dr;
			radix2[2 * (k + quarterN) + 1] = bi + di;

			//radix-4 part
			float bMinusD_r = br - dr;
			float bMinusD_i = bi - di;
			float aMinusC_r = ar - cr;
			float aMinusC_i = ai - ci;

			float imaginaryTimesSub_r = -1 * i * bMinusD_i;
			float imaginaryTimesSub_i = i * bMinusD_r;

			float angleK = angle * k;
			float omega_r = cos(angleK);
			float omega_i = sin(angleK);

			float angleK3 = angleK * 3;
			float omegaToPowerOf3_r = cos(angleK3);
			float omegaToPowerOf3_i = sin(angleK3);

            double aMinusC_minus_its_r = aMinusC_r - imaginaryTimesSub_r;
            double aMinusC_minus_its_i = aMinusC_i - imaginaryTimesSub_i;
            double aMinusC_plus_its_r = aMinusC_r + imaginaryTimesSub_r;
            double aMinusC_plus_its_i = aMinusC_i + imaginaryTimesSub_i;

			radix4Part1[2 * k] = mcr(aMinusC_minus_its_r, aMinusC_minus_its_i, omega_r, omega_i);
			radix4Part1[2 * k + 1] = mci(aMinusC_minus_its_r, aMinusC_minus_its_i, omega_r, omega_i);
			radix4Part2[2 * k] = mcr(aMinusC_plus_its_r, aMinusC_plus_its_i, omegaToPowerOf3_r, omegaToPowerOf3_i);
			radix4Part2[2 * k + 1] = mci(aMinusC_plus_its_r, aMinusC_plus_its_i, omegaToPowerOf3_r, omegaToPowerOf3_i);
		}

		calculateTransform(radix2FFT, radix2, halfN, inverseTransform, 0);
		calculateTransform(radix4Part1FFT, radix4Part1, quarterN, inverseTransform, 0);
		calculateTransform(radix4Part2FFT, radix4Part2, quarterN, inverseTransform, 0);

		for (int k = 0; k < quarterN; k++) {
			int doubleK = 2 * k;
			int quadrupleK = 2 * doubleK;
			if (inverseTransform > 0 && isFirstSplit > 0) {
				output[2 * doubleK] = radix2FFT[2 * k] / len;
				output[2 * doubleK + 1] = radix2FFT[2 * k + 1] / len;
				output[2 * (quadrupleK + 1)] = radix4Part1FFT[2 * k] / len;
				output[2 * (quadrupleK + 1) + 1] = radix4Part1FFT[2 * k + 1] / len;
				output[2 * (doubleK + halfN)] = radix2FFT[2 * (k + quarterN)] / len;
				output[2 * (doubleK + halfN) + 1] = radix2FFT[2 * (k + quarterN) + 1] / len;
				output[2 * (quadrupleK + 3)] = radix4Part2FFT[2 * k] / len;
				output[2 * (quadrupleK + 3) + 1] = radix4Part2FFT[2 * k + 1] / len;
			} else {
				output[2 * doubleK] = radix2FFT[2 * k];
				output[2 * doubleK + 1] = radix2FFT[2 * k + 1];
				output[2 * (quadrupleK + 1)] = radix4Part1FFT[2 * k];
				output[2 * (quadrupleK + 1) + 1] = radix4Part1FFT[2 * k + 1];
				output[2 * (doubleK + halfN)] = radix2FFT[2 * (k + quarterN)];
				output[2 * (doubleK + halfN) + 1] = radix2FFT[2 * (k + quarterN) + 1];
				output[2 * (quadrupleK + 3)] = radix4Part2FFT[2 * k];
				output[2 * (quadrupleK + 3) + 1] = radix4Part2FFT[2 * k + 1];
			}
		}
	} else if (len >= 2) {
		calculateRadix2Transform(output, input, len, inverseTransform, isFirstSplit);
	} else {
		for (int i = 0; i < 2 * len; i++) output[i] = input[i];
	}
}

__kernel void transform(__global float *output, __global float *input, __global float *config,
                        int outputOffset, int inputOffset, int configOffset,
                        int outputSize, int inputSize, int configSize) {
    int N = (int) config[configOffset];
    // TODO  Extract forward/backward flag from config

    float inputLocal[1024];
    float outputLocal[1024];
    copyGlobalToLocal(input, inputLocal, inputOffset, 1024);
	calculateTransform(outputLocal, inputLocal, N, 0, 0);
	copyLocalToGlobal(outputLocal, output, outputOffset, 1024);
}