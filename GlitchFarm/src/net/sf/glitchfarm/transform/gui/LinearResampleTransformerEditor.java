package net.sf.glitchfarm.transform.gui;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.glitchfarm.transform.LinearResampleTransformer;

public class LinearResampleTransformerEditor extends JPanel implements ChangeListener {
	private LinearResampleTransformer transformer;
	
	private JSlider ratioSlider;
	
	public LinearResampleTransformerEditor(LinearResampleTransformer t) {
		this.transformer = t;
		
		this.ratioSlider = new JSlider(JSlider.HORIZONTAL, 100000);
		this.ratioSlider.addChangeListener(this);
		
		this.ratioSlider.setValue((int) (10000 * transformer.getRatio() / 2.0));
		
		super.add(this.ratioSlider);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.ratioSlider) {
			this.transformer.setRatio(2 * this.ratioSlider.getValue() / 10000.0);
		}
	}
}
