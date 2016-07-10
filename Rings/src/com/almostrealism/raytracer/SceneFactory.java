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
 * Copyright (C) 2007  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *
 */

package com.almostrealism.raytracer;

import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.lighting.PointLight;
import com.almostrealism.util.Vector;

/**
 * The SceneFactory class provides static utility methods for getting commonly
 * used components of a scene for the ray tracing engine.
 * 
 * @author  Mike Murray
 */
public class SceneFactory {
	public static Light[] getStandard3PointLightRig(double scale) {
		Light l[] = new Light[3];
		
		l[0] = new PointLight(new Vector(scale, scale, scale));
		l[1] = new PointLight(new Vector(-scale, scale, scale));
		l[2] = new PointLight(new Vector(0.0, scale, -scale));
		
		return l;
	}
}
