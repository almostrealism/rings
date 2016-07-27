package net.sf.glitchfarm.gui.views;

import javax.swing.JPanel;

import net.sf.glitchfarm.util.DataProducer;

public class StandardMixView extends JPanel {
	private DataProducer left, right;
	
	public StandardMixView(DataProducer left, DataProducer right) {
		this.left = left;
		this.right = right;
	}
	
	public DataProducer getLeft() { return this.left; }
}
