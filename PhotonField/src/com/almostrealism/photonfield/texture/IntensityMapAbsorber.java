/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield.texture;

import com.almostrealism.photonfield.VolumeAbsorber;

/**
 * 
 * @author  Mike Murray
 */
public class IntensityMapAbsorber extends VolumeAbsorber {
	private IntensityMap emitMap, absorbMap;
	
	public void setEmitMap(IntensityMap m) { this.emitMap = m; }
	public IntensityMap getEmitMap() { return this.emitMap; }
	public void setAbsorbMap(IntensityMap m) { this.absorbMap = m; }
	public IntensityMap getAbsorbMap() { return this.absorbMap; }
	
}
