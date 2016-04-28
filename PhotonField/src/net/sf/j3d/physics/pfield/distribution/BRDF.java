/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.distribution;

public interface BRDF {
	public SphericalProbabilityDistribution getBRDF();
	
	public void setBRDF(SphericalProbabilityDistribution brdf);
}
