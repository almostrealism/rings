package com.almostrealism.glitchfarm.filter.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.glitchfarm.filter.AmplitudeRangeFilter;

public class AmplitudeRangeFilterConfigPanel extends JPanel implements ChangeListener {
	private AmplitudeRangeFilter filter;
	
	private JSlider minSlider, maxSlider;
	
	public AmplitudeRangeFilterConfigPanel(AmplitudeRangeFilter f) {
		super(new GridLayout(0, 1));
		
		this.filter = f;
		
		this.minSlider = new JSlider(JSlider.HORIZONTAL, 128);
		this.maxSlider = new JSlider(JSlider.HORIZONTAL, 128);
		
		this.minSlider.addChangeListener(this);
		this.maxSlider.addChangeListener(this);
		
		this.add(this.minSlider);
		this.add(this.maxSlider);
		
		this.updateValues();
	}
	
	public void updateValues() {
		this.minSlider.setValue(this.filter.getMinimumAmplitude());
		this.maxSlider.setValue(this.filter.getMaximumAmplitude());
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.minSlider)
			this.filter.setMinimumAmplitude(this.minSlider.getValue());
		else if (e.getSource() == this.maxSlider)
			this.filter.setMaximumAmplitude(this.maxSlider.getValue());
	}
	
	public String toString() { return "AmplitudeRangeFilter"; }
}
