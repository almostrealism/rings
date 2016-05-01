/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.util.buffers;

/**
 * @author  Mike Murray
 */
public interface AveragedVectorMap2D {
	public void addVector(double u, double v, double x, double y, double z, boolean front);
	public double[] getVector(double u, double v, boolean front);
	public int getSampleCount(double u, double v, boolean front);
}
