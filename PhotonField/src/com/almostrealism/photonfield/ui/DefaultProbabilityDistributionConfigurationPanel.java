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

package com.almostrealism.photonfield.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.almostrealism.obj.ObjectFactory;

import com.almostrealism.photonfield.util.OverlayDistribution;
import com.almostrealism.photonfield.util.ProbabilityDistribution;

/**
 * @author  Mike Murray
 */
public class DefaultProbabilityDistributionConfigurationPanel extends JPanel
															implements ObjectFactory {
	private double e = Math.pow(10.0, -15.0);
	private Integer divOpt[] = { new Integer(2),
								new Integer(3),
								new Integer(4),
								new Integer(5),
								new Integer(6),
								new Integer(7),
								new Integer(8),
								new Integer(9),
								new Integer(10),
								new Integer(15)};
	private JComboBox divBox;
	private double start = 0.350, end = 0.780;
	
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
	
	public Object overlay(Object values[]) {
		ProbabilityDistribution d[] = new ProbabilityDistribution[values.length];
		for (int i = 0; i < d.length; i++) d[i] = (ProbabilityDistribution) values[i];
		return OverlayDistribution.createOverlayDistribution(d);
	}
}
