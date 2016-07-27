package com.almostrealism.metamerise.ui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BoundaryControlPanel extends JPanel implements ChangeListener {
	private BoundaryInputPanel inputPanel;
	private JSlider sliderX, sliderY, sliderW, sliderH;
	private JSlider sliderScale;
	
	public BoundaryControlPanel(BoundaryInputPanel p, int w, int h) {
		super(new GridLayout(5, 1));
		
		inputPanel = p;
		
		sliderX = new JSlider(0, w);
		sliderY = new JSlider(0, h);
		sliderW = new JSlider(0, w);
		sliderH = new JSlider(0, h);
		sliderScale = new JSlider(0, 1000);
		
		sliderX.setValue(p.getBoundaryX());
		sliderY.setValue(p.getBoundaryY());
		sliderW.setValue(p.getBoundaryW());
		sliderH.setValue(p.getBoundaryH());
		sliderScale.setValue((int) (p.getBoundaryScale() * 1000));
		
		sliderX.addChangeListener(this);
		sliderY.addChangeListener(this);
		sliderW.addChangeListener(this);
		sliderH.addChangeListener(this);
		sliderScale.addChangeListener(this);
		
		super.add(sliderX);
		super.add(sliderY);
		super.add(sliderW);
		super.add(sliderH);
		super.add(sliderScale);
	}
	
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sliderX) {
			inputPanel.setBoundaryX(sliderX.getValue());
		} else if (e.getSource() == sliderY) {
			inputPanel.setBoundaryY(sliderY.getValue());
		} else if (e.getSource() == sliderW) {
			inputPanel.setBoundaryW(sliderW.getValue());
		} else if (e.getSource() == sliderH) {
			inputPanel.setBoundaryH(sliderH.getValue());
		} else if (e.getSource() == sliderScale) {
			inputPanel.setBoundaryScale(sliderScale.getValue() / 1000.0);
		}
	}
}
