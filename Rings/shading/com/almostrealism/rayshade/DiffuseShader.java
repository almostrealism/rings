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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.security.auth.callback.Callback;

import org.almostrealism.algebra.DiscreteField;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.ColorProduct;
import org.almostrealism.color.ColorSum;
import org.almostrealism.color.RGB;
import org.almostrealism.util.Editable;
import org.almostrealism.util.Producer;

import com.almostrealism.raytracer.engine.RayTracer;
import com.almostrealism.raytracer.engine.ShadableSurface;

/**
 * A {@link DiffuseShader} provides a shading method for diffuse surfaces.
 * The {@link DiffuseShader} class uses a lambertian shading algorithm.
 * 
 * @author Michael Murray
 */
public class DiffuseShader implements Shader, Editable {
	public static DiffuseShader defaultDiffuseShader = new DiffuseShader();
	public static boolean produceOutput = false;

	/** Constructs a new {@link DiffuseShader}. */
	public DiffuseShader() { }
	
	/** Method specified by the {@link Shader} interface. */
	public ColorProducer shade(ShaderParameters p, DiscreteField normals) {
		Vector point;
		
		try {
			point = p.getIntersection().get(0).call().getOrigin();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		ColorProducer lightColor = p.getLight().getColorAt(point);
		
		Vector n;
		
		try {
			n = normals.iterator().next().call().getDirection();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		ColorSum color = new ColorSum();
		
		Future<ColorProducer> surfaceColor = RayTracer.getExecutorService().submit(p.getSurface());
		
		ColorProducer realized;
		
		try {
			point = p.getIntersection().get(0).call().getOrigin();
			realized = surfaceColor.get().operate(point);
		} catch (Exception e) {
			e.printStackTrace();
			return color;
		}
		
		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeFront()) {
			double scale = n.dotProduct(p.getLightDirection());
			color.add((Future) new ColorProduct(lightColor, realized, new RGB(scale, scale, scale)));
		}
		
		if (p.getSurface() instanceof ShadableSurface == false || ((ShadableSurface) p.getSurface()).getShadeBack()) {
			double scale = n.minus().dotProduct(p.getLightDirection());
			color.add((Future) new ColorProduct(lightColor, realized, new RGB(scale, scale, scale)));
		}
		
		return color;
	}
	
	/** Returns a zero length array. */	
	public String[] getPropertyNames() { return new String[0]; }
	
	/** Returns a zero length array. */
	public String[] getPropertyDescriptions() { return new String[0]; }
	
	/** Returns a zero length array. */
	public Class[] getPropertyTypes() { return new Class[0]; }
	
	/** Returns a zero length array. */
	public Object[] getPropertyValues() { return new Object[0]; }
	
	/** @throws IndexOutOfBoundsException */
	public void setPropertyValue(Object value, int index) {
		throw new IndexOutOfBoundsException("Index out of bounds: " + index);
	}
	
	/**
	 * Does nothing.
	 */
	public void setPropertyValues(Object values[]) {}
	
	/**
	 * @return  An empty array.
	 */
	public Producer[] getInputPropertyValues() { return new Producer[0]; }
	
	/** Does nothing. */
	public void setInputPropertyValue(int index, Producer p) {}
	
	/** Returns "Diffuse Shader". */
	public String toString() { return "Diffuse Shader"; }
}
