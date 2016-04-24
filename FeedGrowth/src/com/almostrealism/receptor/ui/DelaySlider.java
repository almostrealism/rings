package com.almostrealism.receptor.ui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.feedgrow.delay.BasicDelayCell;

public class DelaySlider extends JSlider implements ChangeListener {
	private BasicDelayCell delay;
	
	public DelaySlider(BasicDelayCell d, int orientation, int min, int max) {
		super(orientation, min, max, d.getDelay());
		this.delay = d;
		
		addChangeListener(this);
	}

	@Override
	public void stateChanged(ChangeEvent e) { delay.setDelay(getValue()); }
}
