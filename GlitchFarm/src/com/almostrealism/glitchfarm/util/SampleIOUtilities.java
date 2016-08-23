package com.almostrealism.glitchfarm.util;

import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.almostrealism.glitchfarm.obj.Sample;

public class SampleIOUtilities {
	public static Sample loadSample(InputStream instream) throws UnsupportedAudioFileException,
																IOException {
		AudioInputStream in = AudioSystem.getAudioInputStream(instream);
		AudioFormat format = in.getFormat();
		
		int frameSize = format.getFrameSize();
		double frameRate = format.getFrameRate();
		
		System.out.println("SampleIOUtilities: ");
		System.out.println("\t Frame size = " + frameSize);
		System.out.println("\t Frame rate = " + frameRate);
		System.out.println("\t " + in.getFrameLength() + " frames");
		
		byte data[][] = new byte[(int) in.getFrameLength()][frameSize];
		
		for (int l = 0; l < in.getFrameLength(); l++) {
			in.read(data[l]);
		}
		
		return new Sample(data, format);
	}
}
