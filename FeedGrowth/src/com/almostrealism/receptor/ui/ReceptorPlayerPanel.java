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
	
	public void addDelayCell(BasicDelayCell c, int min, int max) {
		BasicDelayCellDisplay disp = new BasicDelayCellDisplay(c);
		
		final DelaySlider s = new DelaySlider(c, JSlider.HORIZONTAL, min, max);
		
		c.setUpdatable(new Updatable() {
			@Override
			public void update() {
				s.setValue(c.getDelay());
				disp.update();
			}
			
			@Override
			public int getResolution() { return disp.getResolution(); }
		});

		JPanel p = new JPanel(new BorderLayout());
		p.add(disp, BorderLayout.CENTER);
		p.add(s, BorderLayout.SOUTH);
		this.add(p);
	}
}
