package org.almostrealism.audioml.audio;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioUtils {
	private static final int SAMPLE_RATE = 44100;
	private static final int BITS_PER_SAMPLE = 32;
	private static final int NUM_CHANNELS = 2;
	private static final int AUDIO_FORMAT = 3; // IEEE float

	/**
	 * Saves audio data as a WAV file.
	 *
	 * @param path Output file path
	 * @param audioData Interleaved stereo audio data (left0, right0, left1, right1, ...)
	 * @throws IOException If there's an error writing the file
	 */
	public static void saveAsWav(String path, float[] audioData) throws IOException {
		int samplesPerChannel = audioData.length / 2;

		// Separate channels
		float[] leftChannel = new float[samplesPerChannel];
		float[] rightChannel = new float[samplesPerChannel];

		for (int i = 0; i < samplesPerChannel; i++) {
			leftChannel[i] = audioData[i * 2];
			rightChannel[i] = audioData[i * 2 + 1];
		}

		saveAsWav(path, leftChannel, rightChannel);
	}

	/**
	 * Saves separate audio channels as a WAV file.
	 *
	 * @param path Output file path
	 * @param leftChannel Left audio channel data
	 * @param rightChannel Right audio channel data
	 * @throws IOException If there's an error writing the file
	 */
	public static void saveAsWav(String path, float[] leftChannel, float[] rightChannel) throws IOException {
		if (leftChannel.length != rightChannel.length) {
			throw new IllegalArgumentException("Left and right channels must have the same length");
		}

		int dataChunkSize = leftChannel.length * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);
		int fmtChunkSize = 16;
		int headerSize = 44;
		int fileSize = headerSize + dataChunkSize - 8;
		int byteRate = SAMPLE_RATE * NUM_CHANNELS * (BITS_PER_SAMPLE / 8);
		int blockAlign = NUM_CHANNELS * (BITS_PER_SAMPLE / 8);

		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(path))) {
			// RIFF header
			out.writeBytes("RIFF");
			out.writeInt(Integer.reverseBytes(fileSize));
			out.writeBytes("WAVE");

			// Format chunk
			out.writeBytes("fmt ");
			out.writeInt(Integer.reverseBytes(fmtChunkSize));
			out.writeShort(Short.reverseBytes((short)AUDIO_FORMAT));
			out.writeShort(Short.reverseBytes((short)NUM_CHANNELS));
			out.writeInt(Integer.reverseBytes(SAMPLE_RATE));
			out.writeInt(Integer.reverseBytes(byteRate));
			out.writeShort(Short.reverseBytes((short)blockAlign));
			out.writeShort(Short.reverseBytes((short)BITS_PER_SAMPLE));

			// Data chunk
			out.writeBytes("data");
			out.writeInt(Integer.reverseBytes(dataChunkSize));

			// Write interleaved audio data
			for (int i = 0; i < leftChannel.length; i++) {
				writeFloat(out, leftChannel[i]);
				writeFloat(out, rightChannel[i]);
			}
		}
	}

	private static void writeFloat(DataOutputStream out, float value) throws IOException {
		// Write IEEE float in little-endian format
		int bits = Float.floatToIntBits(value);
		out.writeInt(Integer.reverseBytes(bits));
	}
}