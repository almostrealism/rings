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
	private static AudioFormat format;
	private static int frameSize;
	
	static {
		format = new AudioFormat(AudioProteinCache.sampleRate, 24, 1, true, true);
		frameSize = format.getFrameSize();
	}

	private SourceDataLine line;
	private AudioProteinCache cache;
	
	public ReceptorPlayer(ProteinCache<Long> cache) throws LineUnavailableException {
		setProteinCache(cache);
		System.out.println("ReceptorPlayer: Frame size is " + frameSize);
		
		SourceDataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(format);
		line.start();
	}
	
	@Override
	public void setProteinCache(ProteinCache<Long> p) {
		cache = (AudioProteinCache) p;
	}
	
	@Override
	public void push(long proteinIndex) {
		if (proteinIndex % frameSize == 0 && proteinIndex >= frameSize) {
			line.write(cache.getByteData(), ((int) proteinIndex - frameSize), frameSize);
		}
	}
	
	public void finish() {
		line.drain();
		line.stop();
	}
}