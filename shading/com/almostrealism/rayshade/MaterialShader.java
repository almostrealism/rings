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
package com.almostrealism.rayshade;

import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.chem.Material;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.Shader;
import org.almostrealism.color.ShaderContext;

/**
 * @author  Michael Murray
 */
public class MaterialShader implements Shader {
	private Material m;
	
	public MaterialShader(Material m) {
		this.m = m;
	}
	
	@Override
	public ColorProducer shade(ShaderContext parameters, DiscreteField normals) {
		// TODO Auto-generated method stub
		return null;
	}

}