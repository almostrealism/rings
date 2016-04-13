package net.sf.sound.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class LineUtilities {
	public static SourceDataLine getLine(AudioFormat format) {
		SourceDataLine line;
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
		if (!AudioSystem.isLineSupported(info)) {
			System.out.println("Not supported");
			return null;
		}
		
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException ex) {
			System.out.println("Unavailable (" + ex.getMessage() + ")");
			return null;
		}
		
		return line;
	}
}
