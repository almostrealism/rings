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
	public static final int frameSize;
	public static final int pushSize;
	
	static {
		format = new AudioFormat(AudioProteinCache.sampleRate, 24, 1, true, true);
		frameSize = format.getFrameSize();
		pushSize = frameSize;
	}

	private SourceDataLine line;
	private AudioProteinCache cache;
	
	private int bufIndex;
	private int byteBufSize;
	
	private byte byteData[][];
	
	public ReceptorPlayer(ProteinCache<Long> cache) throws LineUnavailableException {
		setProteinCache(cache);
		
		byteBufSize = AudioProteinCache.sampleRate * frameSize;
		byteData = new byte[AudioProteinCache.bufferDuration][byteBufSize];
		
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
		int locx = (int) (bufIndex / byteBufSize);
		int locy = (int) (bufIndex % byteBufSize);

		byte data[] = byteData[locx];
		data[locy] = cache.getProtein(proteinIndex).byteValue();
		bufIndex++;
		
		if (locy % pushSize == 0 && locy >= pushSize) {
			int index = (int) (locy - pushSize);
			
			index = index % byteBufSize;
			
			line.write(data, index, pushSize);
		}
	}
	
	public void finish() {
		line.drain();
		line.stop();
	}
}