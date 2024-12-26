package org.almostrealism.audio.line;

import org.almostrealism.audio.CellFeatures;
import org.almostrealism.audio.JavaAudioSample;
import org.almostrealism.audio.OutputLine;
import org.almostrealism.collect.PackedCollection;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class LineUtilities {
	protected static AudioFormat lastFormat;
	
	/**
	 * Returns a SourceDataOutputLine for the most recent format requested.
	 */
	public static OutputLine getLine() {
		if (lastFormat == null) {
			lastFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, OutputLine.sampleRate,
					16, 2, 4,
					OutputLine.sampleRate, false);
		}

		return getLine(lastFormat);
	}
	
	/**
	 * Returns a SourceDataOutputLine for the specified format.
	 */
	public static OutputLine getLine(AudioFormat format) {
		SourceDataLine line;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println("Not supported");
			return null;
		}
		
		lastFormat = format;
		
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
		} catch (LineUnavailableException ex) {
			System.out.println("Unavailable (" + ex.getMessage() + ")");
			return null;
		}
		
		return new SourceDataOutputLine(line);
	}
	
	/**
	 * Converts the specified sample so that it can be played using a line
	 * configured with the current default format.
	 * 
	 * @param s  Sample to convert.
	 * @return  Converted sample.
	 */
	public static JavaAudioSample convert(JavaAudioSample s) {
		return convert(s, lastFormat);
	}
	
	/**
	 * Adjusts the specified sample so that it can be played using a line
	 * configured with the specified format.
	 * 
	 * @param s  Sample to convert.
	 * @param f  Format to convert to.
	 * @return  Converted sample.
	 */
	public static JavaAudioSample convert(JavaAudioSample s, AudioFormat f) {
		return s;
	}

	public static byte[] toBytes(double frames[][], AudioFormat format) {
		int byteCount = format.getFrameSize();
		ByteBuffer buf = ByteBuffer.allocate(frames[0].length * byteCount);

		int offset = 0;
		int bitRate = format.getSampleSizeInBits();
		double floatOffset;
		double floatScale;

		if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
			floatOffset = 0;
			floatScale = Long.MAX_VALUE >> (64 - bitRate);
		} else if (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
			floatOffset = 1;
			floatScale = 0.5 * ((1 << bitRate) - 1);
		} else {
			throw new UnsupportedOperationException();
		}

		for (int f = 0; f < frames[0].length; f++) {
			for (int c = 0; c < format.getChannels(); c++) {
				long val = (long) (floatScale * (floatOffset + frames[0][offset]));
				for (int b = 0; b < format.getFrameSize() / format.getChannels(); b++) {
					buf.put((byte) (val & 0xFF));
					val >>= 8;
				}
			}

			offset++;
		}

		byte out[] = new byte[frames[0].length * byteCount];
		buf.get(0, out);
		return out;
	}

	/**
	 * Converts the specified long value to the bytes of one frame,
	 * depending on the frame size of the specified {@link AudioFormat}.
	 */
	public static byte[] toFrame(PackedCollection<?> frame, AudioFormat format) {
		if (frame.getMemLength() > 1) {
			// TODO  This method should actually convert all the frames in the collection
			CellFeatures.console.features(LineUtilities.class)
					.warn("Frame has more than one element, using only the first");
		}

		int frameSize = format.getFrameSize();

		byte frameBytes[] = null;

		double frameAsDouble = frame.toDouble(0);

		if (frameSize == 1) {
			frameBytes = new byte[1];
			frameBytes[0] = (byte) (Byte.MAX_VALUE * frameAsDouble);
		} else {
			throw new IllegalArgumentException("Frame size " + frameSize + " is not supported");
		}

		return frameBytes;
	}
	
	public static AudioFormat getAudioFormat() {
		return lastFormat;
	}
	
	/**
	 * Initializes the default audio format using that data read from the specified stream.
	 * This method buffers the stream for you.
	 * 
	 * @param instream  Stream to read initial audio data from.
	 * @throws UnsupportedAudioFileException
	 * @throws IOException
	 * @return  The format of the data that was read.
	 */
	public static AudioFormat initDefaultAudioFormat(InputStream instream) throws UnsupportedAudioFileException, IOException {
		instream = new BufferedInputStream(instream);
		AudioInputStream in = AudioSystem.getAudioInputStream(instream);
		return lastFormat = in.getFormat();
	}
}
