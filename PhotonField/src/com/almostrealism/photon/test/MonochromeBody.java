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

package com.almostrealism.photon.test;

/**
 * A MonochromeBody absorbs all radiation and only emits photons of one energy level.
 * 
 * @author  Mike Murray
 */
public class MonochromeBody extends BlackBody {
	private double chrome;
	
	public double[] emit() {
		return null;
	}
	
	public double getEmitEnergy() { return 0; }
	public double[] getEmitPosition() { return null; }
	
	public double getNextEmit() {
		if (super.energy >= this.chrome)
			return 0.0;
		else
			return Integer.MAX_VALUE;
	}
}
