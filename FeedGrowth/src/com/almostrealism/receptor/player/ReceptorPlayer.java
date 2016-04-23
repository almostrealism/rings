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
		pushSize = frameSize * 30;
	}

	private SourceDataLine line;
	private AudioProteinCache cache;
	
	private int bufIndex;
	private int byteBufSize;
	
	private byte byteData[][];
	
	public ReceptorPlayer(ProteinCache<Long> cache) throws LineUnavailableException {
		setProteinCache(cache);
		System.out.println("ReceptorPlayer: Frame size is " + frameSize);
		
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
		byteData[locx][locy] = cache.getProtein(proteinIndex).byteValue();
		bufIndex++;
		
		if (locy % pushSize == 0 && locy >= pushSize) {
			int index = (int) (locy - pushSize);
			
			byte data[] = byteData[locx];
			index = index % byteBufSize;
			
			line.write(data, index, pushSize);
		}
	}
	
	public void finish() {
		line.drain();
		line.stop();
	}
	
//	private byte flatten(long l) {
//		return (byte) (l / convertToByte);
//	}
//	
//	private void insertIntoByteBuffer(int cursor, long value) {
//		int xloc = 8 * cursor;
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc] = (byte) (data[cursor] >> 56);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 1] = (byte) (data[cursor] >> 48);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 2] = (byte) (data[cursor] >> 40);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 3] = (byte) (data[cursor] >> 32);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 4] = (byte) (data[cursor] >> 24);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 5] = (byte) (data[cursor] >> 16);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 6] = (byte) (data[cursor] >> 8);
//		byteData[cursor % AudioProteinCache.bufferDuration][xloc + 7] = (byte) (data[cursor]);
//	}
}