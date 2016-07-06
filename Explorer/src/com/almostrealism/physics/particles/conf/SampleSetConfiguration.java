/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.physics.particles.conf;

import com.almostrealism.physics.particles.InputFunction;

public class SampleSetConfiguration {
	public float samples[], mult[];
	public InputFunction input[];
	public int add[];
	
	private SampleSetConfiguration() {}
	
	public static SampleSetConfiguration getConfiguration(float samples[],
					int add[], float multiply[], InputFunction input[]) {
		SampleSetConfiguration c = new SampleSetConfiguration();
		c.samples = samples;
		c.add = add;
		c.mult = multiply;
		c.input = input;
		return c;
	}
}
