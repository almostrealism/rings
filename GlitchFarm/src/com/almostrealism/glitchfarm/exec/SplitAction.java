package com.almostrealism.glitchfarm.exec;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.almostrealism.glitchfarm.gui.KeyBoardSampleDisplay;
import com.almostrealism.glitchfarm.gui.SampleRowColumnDisplay;
import com.almostrealism.glitchfarm.gui.util.MixerMenus;
import com.almostrealism.glitchfarm.line.OutputLine;
import com.almostrealism.glitchfarm.util.LineUtilities;

public class SplitAction extends AbstractAction {
	private int pieces;
	private JTabbedPane tabs;
	private KeyBoardSampleDisplay kd;
	
	public SplitAction(String name, int pieces, JTabbedPane tabs) {
		this(name, pieces, tabs, null);
	}
	
	public SplitAction(String name, int pieces, JTabbedPane tabs, KeyBoardSampleDisplay kd) {
		super(name);
		this.pieces = pieces;
		this.tabs = tabs;
		this.kd = kd;
	}
	
	public void actionPerformed(ActionEvent e) {
		OutputLine l = LineUtilities.getLine();
		SampleRowColumnDisplay display = new SampleRowColumnDisplay(pieces);
		display.setKeyBoardSampleDisplay(kd);
		display.addSampleRow(MixerMenus.currentSample.splitLoop(pieces), l);
		tabs.addTab("", new JScrollPane(display));
	}
}