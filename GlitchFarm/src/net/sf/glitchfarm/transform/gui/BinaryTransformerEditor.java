package net.sf.glitchfarm.transform.gui;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.glitchfarm.transform.BinaryTransformer;

public class BinaryTransformerEditor extends JPanel implements ChangeListener {
	private BinaryTransformer transformer;
	
	private JSlider threshSlider;
	private JSlider piecesSlider;
	
	public BinaryTransformerEditor(BinaryTransformer t) {
		this.transformer = t;
		
		this.threshSlider = new JSlider(JSlider.HORIZONTAL, 127 * 10);
		this.threshSlider.addChangeListener(this);
		
		this.piecesSlider = new JSlider(JSlider.HORIZONTAL, 16);
		this.piecesSlider.addChangeListener(this);
		this.piecesSlider.setMajorTickSpacing(1);
		this.piecesSlider.setPaintTicks(true);
		
		this.threshSlider.setValue((int) (this.transformer.getThreshold() * 10));
		this.piecesSlider.setValue(this.transformer.getNumberOfPieces());
		
		this.add(this.threshSlider);
		this.add(this.piecesSlider);
	}

	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == this.threshSlider) {
			this.transformer.setThreshold(this.threshSlider.getValue() / 10.0);
		} else if (e.getSource() == this.piecesSlider) {
			this.transformer.setNumberOfPieces(this.piecesSlider.getValue());
		}
	}
}
