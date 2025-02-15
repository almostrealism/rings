package com.almostrealism.audio.ui;

import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.almostrealism.audio.JavaAudioSample;
import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.OutputLine;

public class KeyBoardSampleDisplay extends JPanel implements KeyListener {
	private JavaAudioSample samples[];
	private SampleDisplayPane displays[];
	private OutputLine lines[];
	
	private BeatBoxDisplay beatBox;
	
	public KeyBoardSampleDisplay() {
		super(new GridLayout(2, 9));
		
		this.samples = new JavaAudioSample[9];
		this.displays = new SampleDisplayPane[9];
		this.lines = new OutputLine[9];
		
		for (int i = 1; i <= 9; i++) this.add(new JLabel(String.valueOf(i)));
		
		for (int i = 0; i < 9; i++) {
			this.displays[i] = new SampleDisplayPane(null, null);
			this.displays[i].setKeyBoardSampleDisplay(this);
			this.displays[i].setScrollingDisplay(false);
			this.add(this.displays[i]);
		}
	}
	
	public void setSample(int index, JavaAudioSample s) {
		this.samples[index - 1] = s;
		s.unloop();
		this.lines[index - 1] = LineUtilities.getLine(s.getFormat());
		this.displays[index - 1].setSample(s);
		this.displays[index - 1].setLine(this.lines[index - 1]);
		
		if (this.beatBox != null) {
			s = s.shallowCopy();
			s.unloop();
			this.beatBox.addRow(s);
		}
	}
	
	public void playSample(int index) {
		if (this.samples[index - 1] == null) return;
		
		if (this.samples[index - 1].isStopped())
			this.samples[index - 1].playThread(this.lines[index - 1]).start();
		else
			this.samples[index - 1].restart();
	}
	
	public void setBeatBoxDisplay(BeatBoxDisplay b) { this.beatBox = b; }
	
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_1:
				this.playSample(1);
				break;
			case KeyEvent.VK_2:
				this.playSample(2);
				break;
			case KeyEvent.VK_3:
				this.playSample(3);
				break;
			case KeyEvent.VK_4:
				this.playSample(4);
				break;
			case KeyEvent.VK_5:
				this.playSample(5);
				break;
			case KeyEvent.VK_6:
				this.playSample(6);
				break;
			case KeyEvent.VK_7:
				this.playSample(7);
				break;
			case KeyEvent.VK_8:
				this.playSample(8);
				break;
			case KeyEvent.VK_9:
				this.playSample(9);
				break;
		}
	}
	
	public void keyReleased(KeyEvent e) { }

	public void keyTyped(KeyEvent e) { }
}
