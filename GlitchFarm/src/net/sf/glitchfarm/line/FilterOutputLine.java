package net.sf.glitchfarm.line;

import net.sf.glitchfarm.filter.LineFilter;
import net.sf.glitchfarm.util.DataProducer;
import net.sf.glitchfarm.util.DataReceiver;

public class FilterOutputLine implements OutputLine, DataProducer, DataReceiver {
	private LineFilter filter;
	private OutputLine line;
	
	private byte next[];
	
	public FilterOutputLine(LineFilter filter, OutputLine line) {
		this.filter = filter;
		this.line = line; 
	}
	
	public void write(byte[] b) {
		this.line.write(this.filter.filter(b));
	}
	
	public void next(byte b[]) {
		this.next = b;
	}
	
	public byte[] next() {
		return this.filter.filter(this.next);
	}
}
