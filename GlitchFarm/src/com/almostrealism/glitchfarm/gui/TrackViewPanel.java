package com.almostrealism.glitchfarm.gui;

import javax.swing.JPanel;

import com.almostrealism.glitchfarm.obj.Sample;

public class TrackViewPanel extends JPanel {
	private SampleDisplayPane display;
	
	public TrackViewPanel() { }
	
	public TrackViewPanel(Sample s) {
		this.setSample(s);
	}
	
	public void setSample(Sample s) {
		this.display = new SampleDisplayPane(s);
		this.display.setOrientation(SampleDisplayPane.VERTICAL);
		
		super.removeAll();
		super.add(this.display);
		super.invalidate();
		super.repaint();
	}
}
