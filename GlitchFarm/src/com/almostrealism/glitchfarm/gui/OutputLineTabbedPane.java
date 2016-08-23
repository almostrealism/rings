package com.almostrealism.glitchfarm.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.almostrealism.glitchfarm.line.OutputLine;
import com.almostrealism.glitchfarm.mixer.Mixer;
import com.almostrealism.glitchfarm.util.LineUtilities;

public class OutputLineTabbedPane extends JPanel implements ActionListener {
	private JTabbedPane tabs;
	private JButton addButton;
	private int lineCount = 0;
	
	private Mixer mixer;
	
	public OutputLineTabbedPane(Mixer m) {
		super(new BorderLayout());
		
		this.mixer = m;
		
		this.tabs = new JTabbedPane();
		this.addButton = new JButton("Add Filter Line");
		this.addButton.addActionListener(this);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(this.addButton);
		
		super.add(this.tabs, BorderLayout.CENTER);
		super.add(buttonPanel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.addButton) {
			OutputLine l = LineUtilities.getLine();
			FilterListDisplay f = new FilterListDisplay(l);
			String name = "Filter line " + (++lineCount);
			this.mixer.addLine(name, f);
			this.tabs.add(name, f);
		}
	}
}
