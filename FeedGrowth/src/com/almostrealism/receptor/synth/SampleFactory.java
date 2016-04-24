package com.almostrealism.receptor.synth;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SampleFactory {
	public static Sample createSample(String file) throws UnsupportedAudioFileException, IOException {
		AudioInputStream input = AudioSystem.getAudioInputStream(new File(file));
		
		byte data[] = new byte[100000];
		
		for (int i = 0; input.available() > 0; i++) {
			input.read(data);
		}
		
		return new Sample(data);
	}
}
