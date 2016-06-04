/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.util.buffers;

import java.io.IOException;

import com.almostrealism.photonfield.network.PhotonFieldSceneLoader;
import com.almostrealism.util.graphics.RGB;

public interface ColorBuffer {
	public void addColor(double u, double v, boolean front, RGB c);
	public RGB getColorAt(double u, double v, boolean front);
	public void setScale(double m);
	public double getScale();
	public void clear();
	
	public void store(PhotonFieldSceneLoader loader, String name) throws IOException;
	public void load(PhotonFieldSceneLoader loader, String name) throws IOException;
}