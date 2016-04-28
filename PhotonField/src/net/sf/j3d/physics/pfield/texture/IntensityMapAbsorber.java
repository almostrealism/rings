/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.texture;

import net.sf.j3d.physics.pfield.VolumeAbsorber;

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
