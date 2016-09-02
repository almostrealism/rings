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
 * Copyright (C) 2004-2016 Mike Murray
 * 
 * All rights reserved.
 */

package com.almostrealism.raytracer.engine;

import org.almostrealism.space.Intersectable;
import org.almostrealism.space.Gradient;
import org.almostrealism.space.Vector;
import org.almostrealism.texture.RGB;

import com.almostrealism.rayshade.Shadable;
import com.almostrealism.rayshade.ShadableIntersection;

/**
 * The {@link ShadableSurface} interface is implemented by any 3d object which may be
 * intersected by a 3d ray. These objects must supply methods for calculating
 * ray-surface intersections.
 */
public interface ShadableSurface extends Gradient, Intersectable<ShadableIntersection>, Shadable {
	/**
	 * Returns true if the front side of this Surface object should be shaded.
	 * The "front side" is the side that the Vector object returned by the
	 * {@link #getNormalAt(Vector)} method for this Surface object points
	 * outward from.
	 */
	public boolean getShadeFront();
	
	/**
	 * Returns true if the back side of this Surface object should be shaded.
	 * The "back side" is the side that the vector opposite the Vector object
	 * returned by the getNormalAt() method for this Surface object points outward from.
	 */
	public boolean getShadeBack();
	
	/**
	 * Returns the color of this Surface object at the specified point as an RGB object.
	 */
	public RGB getColorAt(Vector point);
}
