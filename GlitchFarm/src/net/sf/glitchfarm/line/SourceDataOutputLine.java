package net.sf.glitchfarm.line;

import javax.sound.sampled.SourceDataLine;

public class SourceDataOutputLine implements OutputLine {
	private SourceDataLine line;
	
	public SourceDataOutputLine(SourceDataLine line) {
		this.line = line;
	}
	
	public void write(byte[] b) { line.write(b, 0, b.length); }
}
