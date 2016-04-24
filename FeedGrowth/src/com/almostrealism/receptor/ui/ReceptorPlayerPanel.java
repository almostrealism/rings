package com.almostrealism.receptor.ui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;

import com.almostrealism.feedgrow.delay.BasicDelayCell;
import com.almostrealism.receptor.player.ReceptorPlayer;

public class ReceptorPlayerPanel extends JPanel {
	private ReceptorPlayer player;
	
	public ReceptorPlayerPanel() {
		super(new GridLayout(0, 1));
	}
	
	public void setReceptorPlayer(ReceptorPlayer p) {
		this.player = p;
	}
	
	public void addDelayCell(BasicDelayCell c, int min, int max) {
		this.add(new DelaySlider(c, JSlider.HORIZONTAL, min, max));
	}
}
