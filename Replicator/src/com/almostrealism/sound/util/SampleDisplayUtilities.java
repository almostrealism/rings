package com.almostrealism.sound.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import com.almostrealism.sound.Sample;
import com.almostrealism.sound.SampleRowColumnDisplay;
import com.almostrealism.sound.SampleRowDisplay;

public class SampleDisplayUtilities {
	public static void showSplit(Sample s) {
		SourceDataLine l = LineUtilities.getLine(s.getFormat());
		if (l == null) return;
		l.start();
		SampleRowDisplay sd = new SampleRowDisplay(s.splitLoop(8), l);
		JFrame f = new JFrame("Split");
		f.getContentPane().add(sd);
		f.addKeyListener(sd);
		f.setSize(500, 200);
		f.setVisible(true);
	}
	
	public static void showColumnDisplay(Sample s) {
		SourceDataLine l = LineUtilities.getLine(s.getFormat());
		if (l == null) return;
		l.start();
		
		SampleRowColumnDisplay display = new SampleRowColumnDisplay(8);
		display.addSampleRow(s.splitLoop(8), l);
		
		JFrame f = new JFrame("Columns");
		f.setSize(400, 600);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(display, BorderLayout.NORTH);
		f.addKeyListener(display);
		f.setVisible(true);
	}
}
