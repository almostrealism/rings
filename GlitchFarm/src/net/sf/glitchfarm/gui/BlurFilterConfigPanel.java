package net.sf.glitchfarm.gui;

import javax.swing.JPanel;

import net.sf.glitchfarm.filter.BlurFilter;

public class BlurFilterConfigPanel extends JPanel {
	private BlurFilter filter;
	
	public BlurFilterConfigPanel(BlurFilter f) {
		this.filter = f;
	}
}
