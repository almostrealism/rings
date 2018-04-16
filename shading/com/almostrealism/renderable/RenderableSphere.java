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
package com.almostrealism.renderable;

import com.almostrealism.gl.GLDriver;
import com.almostrealism.raytracer.primitives.Sphere;

/**
 * @author  Michael Murray
 */
// TODO  Remove SLICES and STACKS values so they are filled in by GLDriver
public class RenderableSphere extends RenderableGeometry<Sphere> {
	public static final int SLICES = 40;
	public static final int STACKS = 40;

	public RenderableSphere(Sphere s) { super(s); }
	
	@Override
	public void init(GLDriver gl) { }

	/** Delegates to {@link GLDriver#glutSolidSphere(double, int, int)}. */
	@Override
	public void render(GLDriver gl) {
		gl.glutSolidSphere(getGeometry().getSize(), SLICES, STACKS);
	}
}
