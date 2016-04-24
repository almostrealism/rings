package com.almostrealism.receptor.ui;

import java.awt.GridLayout;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JPanel;

import com.almostrealism.receptor.synth.SampleFactory;

public class SamplerPanel extends JPanel {
	public SamplerPanel(int w, int h) throws UnsupportedAudioFileException, IOException {
		super(new GridLayout(h, w));
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				add(new SamplePad(SampleFactory.createSample("/Users/mike/Downloads/KICKS/FE_MD_KICK.wav")));
			}
		}
	}
}
