package com.almostrealism.glitchfarm.gui;

import javax.swing.JPanel;

import com.almostrealism.glitchfarm.filter.BlurFilter;

public class BlurFilterConfigPanel extends JPanel {
	private BlurFilter filter;
	
	public BlurFilterConfigPanel(BlurFilter f) {
		this.filter = f;
	}
}
