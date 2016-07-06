package com.almostrealism.physics.test;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;

public class SpectrumDisplay extends JPanel {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SpectrumDisplay d = new SpectrumDisplay();
		JFrame frame = new JFrame("Spectrum");
		frame.getContentPane().add(d);
		frame.setSize(400, 60);
		frame.setVisible(true);
	}
	
	public void paint(Graphics g) {
		int off = 380;
		
		for (int i = 0; i < (780 - 380); i++) {
			g.setColor(GraphicsConverter.convertToAWTColor(new RGB((double)(off + i))));
			g.drawLine(i * 2, 0, i * 2, 40);
			g.drawLine(i * 2 + 1, 0, i * 2 + 1, 40);
		}
	}
}
