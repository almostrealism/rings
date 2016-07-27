package net.sf.glitchfarm.gui;

import java.awt.Color;

import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.glitchfarm.obj.Sample;

public class BeatBoxRadioButton extends JRadioButton implements ChangeListener {
	Sample sample;
	
	public BeatBoxRadioButton(Sample s) {
		this.sample = s;
		this.setOpaque(true);
		this.setBackground(Color.black);
		this.addChangeListener(this);
	}

	public void stateChanged(ChangeEvent e) {
		if (this.isSelected()) {
			this.setBackground(Color.blue);
		} else {
			this.setBackground(Color.black);
		}
	}
	
	
}
