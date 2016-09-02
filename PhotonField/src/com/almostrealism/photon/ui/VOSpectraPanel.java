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

import javax.swing.JPanel;

import org.almostrealism.stats.ProbabilityDistribution;

import javax.swing.JLabel;

import java.awt.FlowLayout;

/**
 *  @author Samuel Tepper
 */
public class VOSpectraPanel extends JPanel{

	private JLabel nameLabel;
	public String nameString;
	private ProbabilityDistribution spectrum;
	
	public VOSpectraPanel() {
		super(new FlowLayout());
		nameString = spectrum.getName();
		nameLabel = new JLabel(nameString);
	}

}
