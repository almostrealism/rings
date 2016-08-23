package com.almostrealism.glitchfarm.filter.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.glitchfarm.filter.SineFilter;

public class SineFilterConfigPanel extends JPanel implements ChangeListener {
	private SineFilter filter;
	
	private JSlider lenSlider, scaleSlider, mixSlider;
	
	public SineFilterConfigPanel(SineFilter f) {
		super(new GridLayout(0, 1));
		
		this.filter = f;
		
		this.lenSlider = new JSlider(JSlider.HORIZONTAL, 4000);
		this.scaleSlider = new JSlider(JSlider.HORIZONTAL, 1000);
		this.mixSlider = new JSlider(JSlider.HORIZONTAL, 1000);
		
		this.lenSlider.addChangeListener(this);
		this.scaleSlider.addChangeListener(this);
		this.mixSlider.addChangeListener(this);
		
		this.add(this.lenSlider);
		this.add(this.scaleSlider);
		this.add(this.mixSlider);
		
		this.updateValues();
	}
	
	public void updateValues() {
		this.lenSlider.setValue(this.filter.getSampleLength());
		this.scaleSlider.setValue((int) (this.filter.getScale() * 1000));
		this.mixSlider.setValue((int) (this.filter.getMixLevel() * 1000));
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.lenSlider)
			this.filter.setSampleLength(this.lenSlider.getValue());
		else if (e.getSource() == this.scaleSlider)
			this.filter.setScale(this.scaleSlider.getValue() / 1000.0);
		else if (e.getSource() == this.mixSlider)
			this.filter.setMixLevel(this.mixSlider.getValue() / 1000.0);
	}
}
