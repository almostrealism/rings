package org.almostrealism.audio.line;

import org.almostrealism.audio.OutputLine;

public class MultiplierDualOutputLine extends DefaultDualOutputLine {
	public MultiplierDualOutputLine(OutputLine line) {
		super(line);
	}
	
	public byte[] doWrite(byte left[], byte right[]) {
		byte b[] = super.doWrite(left, right);
		
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) ((1.0 - mix) * b[i] + (mix * left[i] * right[i] / 128));
		}
		
		return b;
	}
}
