package org.almostrealism.audio;

import org.almostrealism.audio.line.OutputLine;

import java.util.Hashtable;
import java.util.Iterator;

/**
 * TODO  Should extend {@link org.almostrealism.audio.Mixer}
 * 
 * @author  Michael Murray
 */
public class JavaAudioMixer {
	protected static JavaAudioMixer currentMixer;
	
	private Hashtable lines, beatBoxes;
	
	public JavaAudioMixer() {
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
	
	public static JavaAudioMixer getCurrentMixer() { return currentMixer; }
}
