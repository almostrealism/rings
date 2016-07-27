package net.sf.glitchfarm.util;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;

import net.sf.glitchfarm.gui.KeyBoardSampleDisplay;
import net.sf.glitchfarm.gui.SampleRowColumnDisplay;
import net.sf.glitchfarm.gui.SampleRowDisplay;
import net.sf.glitchfarm.line.OutputLine;
import net.sf.glitchfarm.obj.Sample;

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
