/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */

package net.sf.j3d.physics.efield;

public class ParticleFieldTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double pos[][] = {{0.0, 0.0, 0.0}};
		EnergyField efield = new EnergyField(pos);
		ParticleField pfield = new ParticleField(efield, (int) ParticleField.delta);
	}
}
