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

import org.almostrealism.color.OverlayDistribution;
import org.almostrealism.color.ProbabilityDistribution;
import org.almostrealism.obj.ObjectFactory;

import javax.swing.*;

/**
 * @author  Mike Murray
 */
public class DefaultProbabilityDistributionConfigurationPanel extends JPanel
															implements ObjectFactory {
	private final double e = Math.pow(10.0, -15.0);
	private final Integer[] divOpt = {Integer.valueOf(2),
			Integer.valueOf(3),
			Integer.valueOf(4),
			Integer.valueOf(5),
			Integer.valueOf(6),
			Integer.valueOf(7),
			Integer.valueOf(8),
			Integer.valueOf(9),
			Integer.valueOf(10),
			Integer.valueOf(15)};
	private final JComboBox divBox;
	private final double start = 0.350;
	private final double end = 0.780;
	
	public DefaultProbabilityDistributionConfigurationPanel() {
		this.divBox = new JComboBox(this.divOpt);
		this.divBox.setSelectedIndex(1);
		super.add(this.divBox);
	}
	
	public Class getObjectType() {
		return ProbabilityDistribution.class;
	}
	
	public Object newInstance() throws InstantiationException, IllegalAccessException {
		ProbabilityDistribution pdf = new ProbabilityDistribution();
		
		double tot = this.end - this.start;
		int t =  ((Integer) this.divBox.getSelectedItem()).intValue();
		double delta = tot / t;
		
		for (int i = 0; i < t; i++) {
			double s = this.start + i * delta;
			
			if (i == 0)
				pdf.addRange(s, s + delta, 1.0);
			else
				pdf.addRange(s + this.e, s + delta, 0.0);
		}
		
		return pdf;
	}
	
	public Object overlay(Object[] values) {
		ProbabilityDistribution[] d = new ProbabilityDistribution[values.length];
		for (int i = 0; i < d.length; i++) d[i] = (ProbabilityDistribution) values[i];
		return OverlayDistribution.createOverlayDistribution(d);
	}
}
