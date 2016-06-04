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

public class SpanningTreeColorBuffer implements ColorBuffer {
	private double m = 1.0;
	
	public void addColor(double u, double v, boolean front, RGB c) {
		// TODO Auto-generated method stub
	}
	
	public RGB getColorAt(double u, double v, boolean front) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void clear() {
		// TODO Auto-generated method stub	
	}
	
	public double getScale() { return this.m; }
	public void setScale(double m) { this.m = m; }
	
	public void store(PhotonFieldSceneLoader loader, String name) throws IOException {
		// TODO Auto-generated method stub
	}
	
	public void load(PhotonFieldSceneLoader loader, String name) throws IOException {
		// TODO Auto-generated method stub
	}
}