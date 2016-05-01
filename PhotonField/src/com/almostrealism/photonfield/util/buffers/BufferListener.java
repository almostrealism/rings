/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.util.buffers;

import com.almostrealism.photonfield.Volume;

/**
 * @author  Mike Murray
 */
public interface BufferListener {
	public void updateColorBuffer(double u, double v, Volume source, ColorBuffer target, boolean front);
	public void updateIncidenceBuffer(double u, double v, Volume source, AveragedVectorMap2D target, boolean front);
	public void updateExitanceBuffer(double u, double v, Volume source, AveragedVectorMap2D target, boolean front);
}
