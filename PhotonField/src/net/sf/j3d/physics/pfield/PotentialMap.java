/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield;

/**
 * A PotentialMap instance provides a value for potential energy for all points
 * in space centered around an arbitrary origin (0.0, 0.0, 0.0). Potential is
 * usually measured in volts.
 * 
 * @author Mike Murray
 */
public interface PotentialMap {
	/**
	 * @param p  {x, y, z} - A position in space relative to the internal coordinate
	 *           system for this PotentialMap instance.
	 * @return  The potential at the specified point (usually measured in volts).
	 */
	public double getPotential(double p[]);
}
