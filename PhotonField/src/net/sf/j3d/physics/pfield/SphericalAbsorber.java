/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield;

/**
 * A SphericalAbsorber instance represents an atomic component that absorbs and emits
 * energy within a spherical volume of space. A spherical absorber stores a displacement
 * vector which represents the direction and magnitude of the absorbers displacement from
 * equalibrium (where displacement vector = origin = (0.0, 0.0, 0.0). A spherical absorber
 * will be initialized with a reference to a PotentialMap object which provides a value
 * for the potential energy at each point within the sphere. The energy stored by the
 * absorber should be equal to the potential energy given by the potential map for the
 * displacement vector. The center of the spherical volume is assumed to be the origin of
 * the absorbers internal coordinate system. When referencing the PotentialMap object,
 * a vector of unit length represents a point on the surface of the sphere (shorter
 * than unit length is inside the sphere, greater than unit length is outside the sphere).
 * 
 * @author Mike Murray
 */
public interface SphericalAbsorber extends Absorber {
	/**
	 * @param m  The PotentialMap instance for this spherical absorber to use.
	 */
	public void setPotentialMap(PotentialMap m);
	
	/**
	 * @return  The PotentialMap instance used by this spherical absorber.
	 */
	public PotentialMap getPotentialMap();
	
	/**
	 * @param r  The radius of the spherical volume. (Usually measured in micrometers).
	 */
	public void setRadius(double r);
	
	/**
	 * @return  The radius of the spherical volume. (Usually measured in micrometers).
	 */
	public double getRadius();
	
	/**
	 * @return  {x, y, z} - The displacement vector for this spherical aborber. A unit length
	 *          vector represents a displacement equal to the radius of this spherical absorber.
	 */
	public double[] getDisplacement();
}
