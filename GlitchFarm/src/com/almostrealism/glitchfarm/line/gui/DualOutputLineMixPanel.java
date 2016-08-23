package com.almostrealism.glitchfarm.line.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.almostrealism.glitchfarm.line.DualOutputLine;
import com.almostrealism.glitchfarm.line.OutputLine;

public class DualOutputLineMixPanel extends JPanel implements DualOutputLine, ChangeListener {
	public static int sliderResolution = 10000;
	
	private DualOutputLine line;
	
	private JSlider mixSlider;
	
	public DualOutputLineMixPanel(DualOutputLine l) {
		super(new BorderLayout());
		
		this.line = l;
		
		this.mixSlider = new JSlider(0, sliderResolution);
		this.mixSlider.addChangeListener(this);
		
		this.add(this.mixSlider);
	}
	
	public OutputLine getLeftLine() { return this.line.getLeftLine(); }
	public OutputLine getRightLine() { return this.line.getRightLine(); }
	
	public void setMix(double m) {
		this.line.setMix(m);
		this.mixSlider.setValue((int) (sliderResolution * m));
		this.repaint();
	}
	
	public double getMix() { return this.line.getMix(); }
	
	public void stateChanged(ChangeEvent e) {
		this.line.setMix(this.mixSlider.getValue() / (double)sliderResolution);
	}
}
