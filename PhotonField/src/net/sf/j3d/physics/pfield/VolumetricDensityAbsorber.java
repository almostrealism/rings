/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield;

/**
 * VolumetricDensityAbsorber is an Absorber implementation that absorbs and re-emits
 * photons based on the law of reflection and Snell's law of refraction.
 * 
 * @author Mike Murray
 */
public class VolumetricDensityAbsorber implements Absorber {
	private Volume volume;
	
	/**
	 * @param v  The Volume for this SpecularAbsorber.
	 */
	public void setVolume(Volume v) { this.volume = v; }
	
	/**
	 * @return  The Volume used by this SpecularAbsorber.
	 */
	public Volume getVolume() { return this.volume; }
	
	public boolean absorb(double[] x, double[] p, double energy) {
		// TODO Auto-generated method stub
		return false;
	}

	public double[] emit() {
		// TODO Auto-generated method stub
		return null;
	}

	public Clock getClock() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getEmitEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getNextEmit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setClock(Clock c) {
		// TODO Auto-generated method stub
		
	}

	public double[] getEmitPosition() {
		// TODO Auto-generated method stub
		return null;
	}

}
