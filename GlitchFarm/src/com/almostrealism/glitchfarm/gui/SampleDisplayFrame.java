package com.almostrealism.glitchfarm.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import com.almostrealism.glitchfarm.line.OutputLine;
import com.almostrealism.glitchfarm.obj.Sample;
import com.almostrealism.glitchfarm.util.SampleDisplayUtilities;

public class SampleDisplayFrame extends JFrame implements KeyListener {
	private SampleDisplayPane pane;
	
	public SampleDisplayFrame(Sample s, OutputLine l) {
		super("Sample");
		this.pane = new SampleDisplayPane(s, l);
		this.getContentPane().add(new JScrollPane(this.pane));
		this.addKeyListener(this);
		this.setSize(850, 270);
	}
	
	public void setKeyBoardSampleDisplay(KeyBoardSampleDisplay kd) {
		this.pane.setKeyBoardSampleDisplay(kd);
	}
	
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_S) {
			SampleDisplayUtilities.showColumnDisplay(pane.getSample(), pane.getKeyBoardSampleDisplay());
		} else {
			pane.keyPressed(e);
		}
	}

	public void keyReleased(KeyEvent e) {
		pane.keyReleased(e);
	}

	public void keyTyped(KeyEvent e) {
		pane.keyTyped(e);
	}
}
