package com.almostrealism.glitchfarm;
import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.UnsupportedLookAndFeelException;

import com.almostrealism.glitchfarm.exec.SoundExperiment;

public class GlitchFarm {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnsupportedAudioFileException 
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws UnsupportedAudioFileException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		SoundExperiment.main(args);
	}

}
