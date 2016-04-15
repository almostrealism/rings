package com.almostrealism.receptor.player;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;

public class ReceptorPlayer implements Receptor<Long> {
	protected static final int SAMPLE_RATE = 16 * 1024;
	
	private static AudioFormat format;
	private static int frameSize;
	
	private static byte sineWave[];
	
	static {
		format = new AudioFormat(SAMPLE_RATE, 24, 1, true, true);
		frameSize = format.getFrameSize();
		sineWave = createSinWaveBuffer(800, 30000);
	}
	
	private AudioProteinCache cache;
	private SourceDataLine line;
	
	private int index = 0;
	
	public ReceptorPlayer() throws LineUnavailableException {
		System.out.println("ReceptorPlayer: Frame size is " + frameSize);
		
		SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();
	}
	
	@Override
	public void setProteinCache(ProteinCache<Long> p) {
		this.cache = (AudioProteinCache) p;
	}
	
	@Override
	public void push(long proteinIndex) {
		int offset = (int) (8 * proteinIndex) + (8 - frameSize);
		
//		line.write(cache.getByteData(), offset, frameSize);
		
		line.write(sineWave, index, frameSize);
		index = index + frameSize;
	}
	
	public void finish() {
		line.drain();
		line.stop();
	}
	
	public static byte[] createSinWaveBuffer(double freq, int ms) {
		int samples = (int)((ms * SAMPLE_RATE) / 1000);
		byte[] output = new byte[samples];
		
		double period = (double)SAMPLE_RATE / freq;
		for (int i = 0; i < output.length; i++) {
			double angle = 2.0 * Math.PI * i / period;
			output[i] = (byte)(Math.sin(angle) * 127f);  }

		return output;
	}
}
