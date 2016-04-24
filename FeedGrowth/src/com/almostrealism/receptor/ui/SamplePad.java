package com.almostrealism.receptor.ui;

import java.awt.BorderLayout;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

public class SamplePad extends JPanel {
	private JFormattedTextField durationField;
	
	public SamplePad() {
		super(new BorderLayout());
		
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		intFormat.setGroupingUsed(false);
		durationField = new JFormattedTextField(intFormat);
		durationField.setText("100");
		
		JButton sampleButton = new JButton(new ImageIcon("/Users/mike/Desktop/veda.jpg"));
		
		add(sampleButton, BorderLayout.CENTER);
		add(durationField, BorderLayout.SOUTH);
	}
}
