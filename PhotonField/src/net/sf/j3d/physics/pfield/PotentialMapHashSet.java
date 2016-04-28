/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield;

import java.util.HashSet;

/**
 * A PotentialMapHashSet object is an implementation of PotentialMapSet that uses
 * a HashSet to store the child potential maps.
 * 
 * @author Mike Murray
 */
public class PotentialMapHashSet extends HashSet implements PotentialMapSet {

	public int addPotentialMap(PotentialMap m, double[] x) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int removePotentialMaps(double[] x, double radius) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int removePotentialMap(PotentialMap m) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setMaxProximity(double radius) {
		// TODO Auto-generated method stub

	}

	public double getMaxProximity() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getPotential(double[] p) {
		// TODO Auto-generated method stub
		return 0;
	}

}
