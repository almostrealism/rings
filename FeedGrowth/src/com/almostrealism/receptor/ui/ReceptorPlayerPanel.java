package com.almostrealism.receptor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;

import com.almostrealism.feedgrow.delay.BasicDelayCell;
import com.almostrealism.receptor.player.ReceptorPlayer;

public class ReceptorPlayerPanel extends JPanel {
	private ReceptorPlayer player;
	
	public ReceptorPlayerPanel() { super(new GridLayout(1, 0)); }
	
	public void setReceptorPlayer(ReceptorPlayer p) { this.player = p; }
	
	public void addDelayCell(BasicDelayCell c, int min, int max, int displayResolution) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(new BasicDelayCellDisplay(c, displayResolution), BorderLayout.CENTER);
		p.add(new DelaySlider(c, JSlider.HORIZONTAL, min, max), BorderLayout.SOUTH);
		this.add(p);
	}
}
