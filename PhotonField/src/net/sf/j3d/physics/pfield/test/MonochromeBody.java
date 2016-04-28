/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.test;

/**
 * A MonochromeBody absorbs all radiation and only emits photons of one energy level.
 * 
 * @author  Mike Murray
 */
public class MonochromeBody extends BlackBody {
	private double chrome;
	
	public double[] emit() {
		return null;
	}
	
	public double getEmitEnergy() { return 0; }
	public double[] getEmitPosition() { return null; }
	
	public double getNextEmit() {
		if (super.energy >= this.chrome)
			return 0.0;
		else
			return Integer.MAX_VALUE;
	}
}
