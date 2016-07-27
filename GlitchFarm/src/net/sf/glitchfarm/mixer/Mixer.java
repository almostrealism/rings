package net.sf.glitchfarm.mixer;

import java.util.Hashtable;
import java.util.Iterator;

import net.sf.glitchfarm.line.OutputLine;

public class Mixer {
	protected static Mixer currentMixer;
	
	private Hashtable lines, beatBoxes;
	
	public Mixer() {
		if (this.currentMixer == null) this.currentMixer = this;
		this.lines = new Hashtable();
		this.beatBoxes = new Hashtable();
	}
	
	public void addLine(String name, OutputLine line) {
		this.lines.put(name, line);
	}
	
	public OutputLine getLine(String name) {
		return (OutputLine) this.lines.get(name);
	}
	
	public String[] getLineNames() {
		String s[] = new String[this.lines.size()];
		int i = 0;
		
		Iterator itr = this.lines.keySet().iterator();
		while (itr.hasNext()) s[i++] = itr.next().toString();
		
		return s;
	}
	
	public static Mixer getCurrentMixer() { return currentMixer; }
}
