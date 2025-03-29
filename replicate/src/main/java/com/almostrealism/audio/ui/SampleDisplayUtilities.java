package com.almostrealism.audio.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.almostrealism.audio.JavaAudioSample;
import org.almostrealism.audio.line.LineUtilities;
import org.almostrealism.audio.line.OutputLine;

public class SampleDisplayUtilities {
	public static void showSplit(JavaAudioSample s) {
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
	
	public static void showColumnDisplay(JavaAudioSample s, KeyBoardSampleDisplay kd) {
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
