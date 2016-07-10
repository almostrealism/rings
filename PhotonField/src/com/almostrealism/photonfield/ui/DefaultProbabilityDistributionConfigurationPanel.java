/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.ui;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import com.almostrealism.obj.ObjectFactory;
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
