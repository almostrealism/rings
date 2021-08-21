package com.almostrealism.audio.ui;

import java.awt.Color;

import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.almostrealism.audio.JavaAudioSample;

public class BeatBoxRadioButton extends JRadioButton implements ChangeListener {
	JavaAudioSample sample;
	
	public BeatBoxRadioButton(JavaAudioSample s) {
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
