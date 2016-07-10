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

package com.almostrealism.photonfield;

/**
 * A PotentialMap instance provides a value for potential energy for all points
 * in space centered around an arbitrary origin (0.0, 0.0, 0.0). Potential is
 * usually measured in volts.
 * 
 * @author Mike Murray
 */
public interface PotentialMap {
	/**
	 * @param p  {x, y, z} - A position in space relative to the internal coordinate
	 *           system for this PotentialMap instance.
	 * @return  The potential at the specified point (usually measured in volts).
	 */
	public double getPotential(double p[]);
}
