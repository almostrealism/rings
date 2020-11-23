/*
 * Copyright 2020 Michael Murray
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
import org.almostrealism.color.*;
import org.almostrealism.color.computations.ColorEvaluable;
import org.almostrealism.color.computations.GeneratedColorProducer;
import org.almostrealism.color.computations.RGBBlack;
import org.almostrealism.color.computations.RGBEvaluable;
import org.almostrealism.relation.Producer;
import org.almostrealism.space.LightingContext;
import org.almostrealism.util.Compactable;
import org.almostrealism.util.Editable;
import org.almostrealism.relation.Evaluable;

/**
 * A {@link SilhouetteShader} can be used to shade a surface with one color value
 * for all pixels where the surface appears.
 * 
 * @author  Michael Murray
 */
public class SilhouetteShader implements RGBEvaluable, Compactable, Editable, Shader<LightingContext> {
	private Producer<RGB> color;

	private String names[] = { "Color" };
	private String desc[] = { "The color of the silhouette" };
	private Class types[] = { RGBEvaluable.class };
  
  
	/**
	 * Constructs a new {@link SilhouetteShader} using black as a color.
	 */
	public SilhouetteShader() { this.color = RGBBlack.getInstance(); }
	
	/**
	 * Constructs a new {@link SilhouetteShader} using the specified {@link RGB}
	 * {@link Evaluable} as a color.
	 * 
	 * @param color  RGB Producer to use.
	 */
	public SilhouetteShader(Producer<RGB> color) { this.color = color; }
	
	/**
	 * @see  Shader#shade(LightingContext, DiscreteField)
	 */
	@Override
	public Producer<RGB> shade(LightingContext p, DiscreteField normals) {
		return GeneratedColorProducer.fromProducer(this, color);
	}

	/**
	 * @see ColorEvaluable#evaluate(java.lang.Object[])
	 */
	@Override
	public RGB evaluate(Object args[]) { return this.color.get().evaluate(args); }

	@Override
	public void compact() { color.compact(); }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyNames()
	 */
	@Override
	public String[] getPropertyNames() { return this.names; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyDescriptions()
	 */
	@Override
	public String[] getPropertyDescriptions() { return this.desc; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyTypes()
	 */
	@Override
	public Class[] getPropertyTypes() { return this.types; }

	/**
	 * @see org.almostrealism.util.Editable#getPropertyValues()
	 */
	@Override
	public Object[] getPropertyValues() { return new Object[] {this.color}; }

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValue(java.lang.Object, int)
	 */
	@Override
	public void setPropertyValue(Object value, int index) {
		if (index == 0)
			this.color = (Producer<RGB>) value;
		else
			throw new IllegalArgumentException("Illegal property index: " + index);
	}

	/**
	 * @see org.almostrealism.util.Editable#setPropertyValues(java.lang.Object[])
	 */
	@Override
	public void setPropertyValues(Object values[]) {
		if (values.length > 0) this.color = (Producer<RGB>) values[0];
	}

	/**
	 * @see org.almostrealism.util.Editable#getInputPropertyValues()
	 */
	@Override
	public Producer[] getInputPropertyValues() { return new Producer[] { this.color }; }

	/**
	 * @see org.almostrealism.util.Editable#setInputPropertyValue(int, Producer)
	 */
	@Override
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.color = (Producer<RGB>) p;
		else
			throw new IllegalArgumentException("Illegal property index: " + index);
	}
	
	/**
	 * @return  "Silhouette Shader".
	 */
	@Override
	public String toString() { return "Silhouette Shader"; }
}
