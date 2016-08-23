package com.almostrealism.glitchfarm.gui.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import com.almostrealism.glitchfarm.obj.Sample;

public class SetBPMAction extends AbstractAction {
	private int pieces = 1;
	
	public SetBPMAction(int pieces) {
		super(String.valueOf(pieces));
		this.pieces = pieces;
	}
	
	public void actionPerformed(ActionEvent e) {
		Sample s = MixerMenus.currentSample;
		int l = s.loopEnd - s.loopStart;
		System.out.println("Set BPM: " + s.loopStart + " " + l + " " + this.pieces);
		MixerMenus.currentSample.setBPM(s.loopStart, l / this.pieces);
		
		if (MixerMenus.currentDisplayPane != null) {
			MixerMenus.currentDisplayPane.updateControlInfo();
			MixerMenus.currentDisplayPane.repaint();
		}
	}
}
