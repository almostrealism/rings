package com.almostrealism.glitchfarm.gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.almostrealism.glitchfarm.obj.Sample;

public class SampleControlsDisplay extends JPanel implements Runnable, ActionListener, ItemListener {
	private Sample sample;
	private SampleDisplayPane display;
	
	private JCheckBox muteBox, loopBox, fillBox, editBox;
	private JButton resetLoopButton;
	
	private boolean updatingValues = false;
	
	public SampleControlsDisplay(Sample s) {
		super(new GridLayout(5, 1));
		
		this.sample = s;
		
		this.muteBox = new JCheckBox("Mute");
		this.loopBox = new JCheckBox("Loop");
		this.fillBox = new JCheckBox("Fill");
		this.editBox = new JCheckBox("Edit");
		
		this.resetLoopButton = new JButton("Reset Loop");
		
		this.muteBox.addItemListener(this);
		this.loopBox.addItemListener(this);
		this.fillBox.addItemListener(this);
		this.editBox.addItemListener(this);
		
		this.resetLoopButton.addActionListener(this);
		
		this.add(this.muteBox);
		this.add(this.loopBox);
		this.add(this.fillBox);
		this.add(this.editBox);
		this.add(this.resetLoopButton);
		
		this.updateValues();
	}
	
	public void setSampleDisplayPane(SampleDisplayPane p) {
		this.display = p;
		this.updateValues();
	}
	
	public void run() {
		this.updateValues();
	}
	
	public void updateValues() {
		this.updatingValues = true;
		this.muteBox.setSelected(this.sample.isMuted());
		this.loopBox.setSelected(this.sample.isLooped());
		this.fillBox.setSelected(this.sample.isFilled());
		if (this.display != null) this.editBox.setSelected(this.display.isEditing());
		this.updatingValues = false;
	}

	public void itemStateChanged(ItemEvent e) {
		if (updatingValues) return;
		
		if (e.getSource() == this.muteBox) {
			this.sample.toggleMute();
		} else if (e.getSource() == this.loopBox) {
			this.sample.toggleLoop();
		} else if (e.getSource() == this.fillBox) {
			this.sample.toggleFill();
		} else if (e.getSource() == this.editBox) {
			this.display.toggleEditing();
		}
		
		SwingUtilities.invokeLater(this);
		
		if (this.display != null)
			this.display.repaint();
	}

	public void actionPerformed(ActionEvent arg0) {
		this.sample.resetLoop();
		
		if (this.display != null)
			this.display.repaint();
	}
}
