/*
 * Copyright 2022 Michael Murray
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

import org.almostrealism.algebra.ScalarProducerBase;
import org.almostrealism.geometry.DiscreteField;
import org.almostrealism.algebra.ScalarProducer;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.*;
import org.almostrealism.color.computations.*;
import org.almostrealism.geometry.Ray;
import io.almostrealism.relation.Producer;
import org.almostrealism.CodeFeatures;
import io.almostrealism.relation.Editable;
import io.almostrealism.relation.Evaluable;

import java.util.function.Supplier;

/**
 * A {@link BlendingShader} provides a method for blending values from two
 * different {@link ColorEvaluable} instances based on lighting. This is best
 * for cool to warm shading or cartoon shading.
 * 
 * @author  Michael Murray
 */
public class BlendingShader implements Shader<LightingContext>, Editable, RGBFeatures, CodeFeatures {
  private static final String names[] = {"Hot color", "Cold color"};
  private static final String desc[] = {"Color for hot (lit) area.", "Color for cold (dim) area."};
  private static final Class types[] = {ColorEvaluable.class, ColorEvaluable.class};
  
  private Producer<RGB> hotColor, coldColor;

	/**
	 * Constructs a new BlendingShader using white as a hot color
	 * and black as a cold color.
	 */
	public BlendingShader() {
		this.hotColor = RGBWhite.getInstance();
		this.coldColor = RGBBlack.getInstance();
	}
	
	/**
	 * Constructs a new BlendingShader using the specified hot and cold colors.
	 * 
	 * @param hot  ColorProducer to use for hot color.
	 * @param cold  ColorProducer to use for cold color.
	 */
	public BlendingShader(Producer<RGB> hot, Producer<RGB> cold) {
		this.hotColor = hot;
		this.coldColor = cold;
	}
	
	/**
	 * @see  Shader#shade(LightingContext, DiscreteField)
	 */
	public Producer<RGB> shade(LightingContext p, DiscreteField normals) {
		// TODO  Put evaluation into producer

		Producer<Ray> n;
		
		try {
			n = normals.iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		Supplier<Evaluable<? extends Vector>> l = p.getLightDirection();

		ScalarProducerBase k = direction(n).dotProduct(l).add(1.0);
		ScalarProducerBase oneMinusK = scalar(1.0).subtract(k);
		
		RGB hc = this.hotColor.get().evaluate(p);
		RGB cc = this.coldColor.get().evaluate(p);
		
		RGBProducer c = v(hc).multiply(cfromScalar(k));
		c = c.add(v(cc).multiply(cfromScalar(oneMinusK)));

		return GeneratedColorProducer.fromProducer(this, c);
	}

	/**
	 * @see Editable#getPropertyNames()
	 */
	public String[] getPropertyNames() { return BlendingShader.names; }

	/**
	 * @see Editable#getPropertyDescriptions()
	 */
	public String[] getPropertyDescriptions() { return BlendingShader.desc; }

	/**
	 * @see Editable#getPropertyTypes()
	 */
	public Class[] getPropertyTypes() { return BlendingShader.types; }

	/**
	 * @see Editable#getPropertyValues()
	 */
	public Object[] getPropertyValues() { return this.getInputPropertyValues(); }

	/**
	 * @see Editable#setPropertyValue(java.lang.Object, int)
	 */
	public void setPropertyValue(Object o, int index) { this.setInputPropertyValue(index, (Producer) o); }

	/**
	 * @see Editable#setPropertyValues(java.lang.Object[])
	 */
	public void setPropertyValues(Object values[]) {
		for (int i = 0; i < values.length; i++) this.setPropertyValue(values[i], i);
	}

	/** @see Editable#getInputPropertyValues() */
	public Producer[] getInputPropertyValues() { return new Producer[] {this.hotColor, this.coldColor}; }

	/**
	 * @see Editable#setInputPropertyValue(int, Producer)
	 * @throws IndexOutOfBoundsException  If the property index is out of bounds.
	 */
	public void setInputPropertyValue(int index, Producer p) {
		if (index == 0)
			this.hotColor = (Producer) p;
		else if (index == 1)
			this.coldColor = (Producer) p;
		else
			throw new IndexOutOfBoundsException("Property index out of bounds: " + index);
	}
	
	/** @return  "Blending Shader". */
	public String toString() { return "Blending Shader"; }
}
