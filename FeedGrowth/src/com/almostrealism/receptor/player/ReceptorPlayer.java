package com.almostrealism.receptor.player;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.almostrealism.feedgrow.audio.AudioProteinCache;
import com.almostrealism.feedgrow.cellular.Receptor;
import com.almostrealism.feedgrow.content.ProteinCache;

public class ReceptorPlayer implements Receptor<Long> {
	private static AudioFormat format;
	private static int frameSize;
	
	static {
		try {
			format = AudioSystem.getAudioFileFormat(new File("/Users/mike/Desktop/Crazy Eyes Kit Samples/2020_PH_Snare1.wav")).getFormat();
			frameSize = format.getFrameSize();
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private AudioProteinCache cache;
	private SourceDataLine line;
	
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
		
		System.out.println(cache.getByteData()[offset] + " " +
							cache.getByteData()[offset + 1] + " " +
							cache.getByteData()[offset + 2]);
		
		line.write(cache.getByteData(), offset, frameSize);
	}
	
	public void finish() {
		line.drain();
		line.stop();
	}
}
