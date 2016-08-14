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
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photon;

/**
 * An SOrbital object represents the S orbital of a single proton atom.
 * 
 * @author Mike Murray
 */
public class SOrbital extends ProtonCloud implements SphericalAbsorber {

	public void setPotentialMap(PotentialMap m) {
		// TODO Auto-generated method stub

	}

	public PotentialMap getPotentialMap() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRadius(double r) {
		// TODO Auto-generated method stub

	}

	public double getRadius() {
		// TODO Auto-generated method stub
		return 0;
	}

	public double[] getDisplacement() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean absorb(double[] x, double[] p, double energy) {
		// TODO Auto-generated method stub
		return false;
	}

	public double[] emit() {
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

	public Clock getClock() {
		// TODO Auto-generated method stub
		return null;
	}

	public double[] getEmitPosition() {
		// TODO Auto-generated method stub
		return null;
	}

}
