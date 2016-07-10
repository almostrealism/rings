/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield;

/**
 * VolumetricDensityAbsorber is an Absorber implementation that absorbs and re-emits
 * photons based on the law of reflection and Snell's law of refraction.
 * 
 * @author Mike Murray
 */
public class VolumetricDensityAbsorber implements Absorber {
	private Volume volume;
	
	/**
	 * @param v  The Volume for this SpecularAbsorber.
	 */
	public void setVolume(Volume v) { this.volume = v; }
	
	/**
	 * @return  The Volume used by this SpecularAbsorber.
	 */
	public Volume getVolume() { return this.volume; }
	
	public boolean absorb(double[] x, double[] p, double energy) {
		// TODO Auto-generated method stub
		return false;
	}

	public double[] emit() {
		// TODO Auto-generated method stub
		return null;
	}

	public Clock getClock() {
		// TODO Auto-generated method stub
		return null;
	}

	public double getEmitEnergy() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getNextEmit() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setClock(Clock c) {
		// TODO Auto-generated method stub
		
	}

	public double[] getEmitPosition() {
		// TODO Auto-generated method stub
		return null;
	}

}
