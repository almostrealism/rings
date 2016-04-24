package com.almostrealism.receptor.ui;

import java.awt.GridLayout;

import javax.swing.JPanel;

public class SamplerPanel extends JPanel {
	public SamplerPanel(int w, int h) {
		super(new GridLayout(h, w));
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				add(new SamplePad());
			}
		}
	}
}
