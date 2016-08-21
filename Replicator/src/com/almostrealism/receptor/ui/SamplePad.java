/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.receptor.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

import com.almostrealism.receptor.Receptor;
import com.almostrealism.receptor.synth.Sample;

public class SamplePad extends JPanel implements ActionListener {
	private JFormattedTextField durationField;
	
	private Sample sample;
	
	public SamplePad(Sample s) {
		super(new BorderLayout());
		
		sample = s;
		
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		intFormat.setGroupingUsed(false);
		durationField = new JFormattedTextField(intFormat);
		durationField.setText("100");
		
		JButton sampleButton = new JButton(new ImageIcon("/Users/mike/Desktop/veda.jpg"));
		
		sampleButton.addActionListener(this);
		
		add(sampleButton, BorderLayout.CENTER);
		add(durationField, BorderLayout.SOUTH);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Receptor.getGlobalMixer().add(sample);
	}
}
