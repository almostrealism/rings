package org.almostrealism.audio.line.test;

import org.almostrealism.audio.line.OutputLine;
import org.almostrealism.audio.data.WaveData;
import org.almostrealism.audio.line.LineUtilities;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SourceDataOutputLineTest {
	@Test
	public void playWaveData() throws IOException, InterruptedException {
		File f = new File("Library/MD_SNARE_09.wav");

		WaveData d = WaveData.load(f);
		double data[] = d.getChannelData(0).toArray();

		OutputLine line = LineUtilities.getLine();
		line.write(new double[][] { data });

		Thread.sleep(10000);
	}

	@Test
	public void otherTest() throws LineUnavailableException {
		AudioFormat audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
		SourceDataLine outLine;

		int bufferByteLength = 4096;
		byte[] b = new byte[4];
		final float framePeriod = 1.0f / audioFormat.getFrameRate();
		int frame = 0;

		outLine = AudioSystem.getSourceDataLine(audioFormat);
		outLine.open(audioFormat, bufferByteLength);
		outLine.start();

		System.out.println("Format: " + audioFormat);

		double begin = System.nanoTime();
		while((frame * framePeriod) < 5){
			short sample = (short)(0.2 * Math.sin(2 * Math.PI * 440 * frame * framePeriod) * Short.MAX_VALUE);
			b[0] = (byte)(sample & 0xFF);
			b[1] = (byte)((sample >> 8) & 0xFF);
			b[2] = (byte)(sample & 0xFF);
			b[3] = (byte)((sample >> 8) & 0xFF);
			++frame;
			outLine.write(b, 0, 4);
		}
		double end = System.nanoTime();

		System.out.println("True elapsed (seconds): " + (end - begin) * 1e-9);
		System.out.println("Line reported elapsed (seconds): " + outLine.getMicrosecondPosition() * 1e-6);

		outLine.drain();
		outLine.stop();
		outLine.close();

	}
}
