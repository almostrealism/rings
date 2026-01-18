/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.gl;

import com.almostrealism.renderable.Diffuse;
import com.almostrealism.renderable.Specular;
import org.almostrealism.color.RGBA;

public class GLMaterial implements Diffuse, Specular {
	public RGBA ambient = new RGBA(1.0, 1.0, 1.0, 0.15);
	public RGBA diffuse = new RGBA(1.0, 1.0, 1.0, 0.15);
	public RGBA specular = new RGBA(1.0, 1.0, 1.0, 0.15);
	public double shininess = 15.0;

	@Override
	public void setDiffuse(float r, float g, float b, float a) {
		diffuse = new RGBA(r, g, b, a);
	}

	@Override
	public float[] getDiffuse() {
		return toFloat(diffuse.toArray());
	}

	@Override
	public void setSpecular(float r, float g, float b, float a) {
		specular = new RGBA(r, g, b, a);
	}

	@Override
	public float[] getSpecular() {
		return toFloat(specular.toArray());
	}

	@Override
	public void setShininess(float s) {
		this.shininess = s;
	}

	@Override
	public float getShininess() {
		return (float) this.shininess;
	}

	private static float[] toFloat(double[] d) {
		float[] f = new float[d.length];
		for (int i = 0; i < d.length; i++) {
			f[i] = (float) d[i];
		}
		return f;
	}
}
