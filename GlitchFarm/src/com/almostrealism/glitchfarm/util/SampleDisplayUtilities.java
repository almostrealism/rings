package com.almostrealism.glitchfarm.util;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.almostrealism.glitchfarm.gui.KeyBoardSampleDisplay;
import com.almostrealism.glitchfarm.gui.SampleRowColumnDisplay;
import com.almostrealism.glitchfarm.gui.SampleRowDisplay;
import com.almostrealism.glitchfarm.line.OutputLine;
import com.almostrealism.glitchfarm.obj.Sample;

public class SampleDisplayUtilities {
	public static void showSplit(Sample s) {
		OutputLine l = LineUtilities.getLine(s.getFormat());
		if (l == null) return;
		SampleRowDisplay sd = new SampleRowDisplay(s.splitLoop(8), l);
		JFrame f = new JFrame("Split");
		f.getContentPane().add(sd);
		f.addKeyListener(sd);
		f.setSize(625, 200);
		f.setLocation(0, 600);
		f.setVisible(true);
	}
	
	public static void showColumnDisplay(Sample s, KeyBoardSampleDisplay kd) {
		OutputLine l = LineUtilities.getLine(s.getFormat());
		if (l == null) return;
		
		SampleRowColumnDisplay display = new SampleRowColumnDisplay(16);
		display.setKeyBoardSampleDisplay(kd);
		display.addSampleRow(s.splitLoop(16), l);
		
		JFrame f = new JFrame("Columns");
		f.setSize(625, 200);
		f.setLocation(0, 600);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(display, BorderLayout.NORTH);
		f.addKeyListener(display);
		f.setVisible(true);
	}
}
