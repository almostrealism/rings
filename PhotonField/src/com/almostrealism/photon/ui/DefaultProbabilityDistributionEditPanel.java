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

package com.almostrealism.photon.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.almostrealism.stats.ProbabilityDistribution;
import org.almostrealism.swing.panels.PercentagePanel;

import com.almostrealism.photon.xml.ProbabilityDistributionDisplay;

/**
 * @author  Mike Murray
 */
public class DefaultProbabilityDistributionEditPanel extends JPanel
													implements ChangeListener {
	private ProbabilityDistribution pdf;
	private PercentagePanel sliders[];
	private ProbabilityDistributionDisplay display;
	private ChangeListener listener;
	
	public DefaultProbabilityDistributionEditPanel(ProbabilityDistribution p) {
		super(new BorderLayout());
		
		this.pdf = p;
		
		this.sliders = new PercentagePanel[p.getNodeCount()];
		
		for (int i = 0; i < this.sliders.length; i++) {
			this.sliders[i] = new PercentagePanel(new FlowLayout());
			this.sliders[i].setSliderName(String.valueOf(i));
//			this.sliders[i].add(new JLabel());
			this.sliders[i].addChangeListener(this);
		}
		
		JPanel pl = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridheight = 1;
		c.gridwidth = 1;
		c.gridy = 1;
		c.gridx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		
		for (int i = 0; i < this.sliders.length; i++) {
			pl.add(this.sliders[i], c);
			c.gridy++;
		}
		
		this.display = new ProbabilityDistributionDisplay();
		this.display.setObject(this.pdf);
		
		super.add(new JScrollPane(pl), BorderLayout.CENTER);
		super.add(this.display, BorderLayout.SOUTH);
	}
	
	public void updateDisplay() {
		for (int i = 0; i < this.sliders.length; i++) {
			this.sliders[i].setValue(this.pdf.getRangeProbability(i));
		}
	}
	
	public void setChangeListener(ChangeListener l) { this.listener = l; }
	
	public void stateChanged(ChangeEvent e) {
		int index = Integer.parseInt(((Component)e.getSource()).getName());
		this.pdf.setRangeProbability(index, this.sliders[index].getValue());
		this.display.repaint();
		if (this.listener != null) this.listener.stateChanged(new ChangeEvent(this));
	}
}
