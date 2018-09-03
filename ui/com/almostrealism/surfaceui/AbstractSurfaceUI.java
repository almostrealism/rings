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

package com.almostrealism.surfaceui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import io.almostrealism.code.Scope;
import org.almostrealism.algebra.*;
import org.almostrealism.color.ColorProducer;
import org.almostrealism.color.RGB;
import org.almostrealism.color.ShaderContext;
import org.almostrealism.geometry.Ray;
import org.almostrealism.relation.Operator;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.BoundingSolid;
import org.almostrealism.space.ShadableIntersection;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.space.SurfaceGroup;
import org.almostrealism.swing.Dialog;
import org.almostrealism.texture.GraphicsConverter;

import com.almostrealism.primitives.SurfaceUI;
import org.almostrealism.util.Producer;

/**
 * AbstractSurfaceUI is an abstract implementation of the {@link SurfaceUI} interface
 * that takes care of all of the standard methods of {@link SurfaceUI} that all
 * {@link SurfaceUI} implementations use in the same way. The name is "Surface"
 * by default.
 */
public abstract class AbstractSurfaceUI implements SurfaceUI {
	protected AbstractSurface surface;
  
	private String name;
  
	private Icon icon;
	private RGB lastColor;
	private Color background = Color.white;

	/** Sets all values to the default. */
	public AbstractSurfaceUI() {
		this.setSurface(null);
		this.setName("Surface");
	}
	
	/**
	 * Sets the underlying AbstractSurface object of this AbstractSurfaceUI to the specified AbstractSurface object.
	 */
	public AbstractSurfaceUI(AbstractSurface surface) {
		this.setSurface(surface);
		this.setName("Surface");
	}
	
	/**
	 * Sets the underlying AbstractSurface object of this AbstractSurfaceUI to the specified AbstractSurface object and
	 * sets the name to the specified String object.
	 */
	public AbstractSurfaceUI(AbstractSurface surface, String name) {
		this.setSurface(surface);
		this.setName(name);
	}
	
	/**
	 * Returns the value returned by the getShadeFront() method of the underlying AbstractSurface
	 * stored by this AbstractSurfaceUI.
	 */
	public boolean getShadeFront() {
		return this.surface.getShadeFront();
	}
	
	/**
	 * Returns the value returned by the getShadeBack() method of the underlying AbstractSurface
	 * stored by this AbstractSurfaceUI.
	 */
	public boolean getShadeBack() {
		return this.surface.getShadeBack();
	}

	/**
	 * Returns the value returned by the calculateBoundingSolid() method of the underlying AbstractSurface
	 * stored by this AbstractSurfaceUI.
	 */
	@Override
	public BoundingSolid calculateBoundingSolid()
	{
		return this.surface.calculateBoundingSolid();
	}

	/** Sets the name of this AbstractSurfaceUI to the specified String. */
	public void setName(String name) {
		this.name = name;
	}
	
	/** Returns the name of this AbstractSurfaceUI as a String. */
	public String getName() {
		return this.name;
	}
	
	/** Returns false. */
	public boolean hasDialog() {
		return false;
	}
	
	/** Returns null. */
	public Dialog getDialog() {
		return null;
	}
	
	/**
	 * @return  An icon that shows the color of the underlying surface object.
	 */
	public Icon getIcon() {
		if (this.lastColor != null && this.surface.getColor().equals(this.lastColor)) return this.icon;
		
		int w = 20, h = 20;
		
		BufferedImage b = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics g = b.createGraphics();
		
		RGB rgb = this.surface.getColor();
		
		g.setColor(this.background);
		g.fillRect(0, 0, w, h);
		g.setColor(GraphicsConverter.convertToAWTColor(rgb));
		g.fillRoundRect(0, 0, w, h, 5, 5);
		
		if ((rgb.getRed() + rgb.getGreen() + rgb.getBlue()) >= 2.8) {
			g.setColor(Color.black);
			g.drawRoundRect(0, 0, w - 1, h - 1, 1, 1);
		}
		
		this.lastColor = this.surface.getColor();
		this.icon = new ImageIcon(b);
		
		return this.icon;
	}
	
	/**
	 * Sets the parent of the underlying AbstractSurface object stored by this AbstractSurfaceUI.	
	 * 
	 * @param parent  The new parent
	 */
	public void setParent(SurfaceGroup parent) {
		((AbstractSurface) getSurface()).setParent(parent);
	}
	
	/**
	 * Sets the underlying AbstractSurface object stored by this AbstractSurfaceUI to the specified AbstractSurface object.
	 */
	public void setSurface(AbstractSurface surface) {
		this.surface = surface;
	}
	
	/** Returns the underlying {@link AbstractSurface} stored by this {@link AbstractSurfaceUI}. */
	@Override
	public ShadableSurface getSurface() {
		return this.surface;
	}
	
	/** Returns the {@link ColorProducer} for this AbstractSurfaceUI. */
	@Override
	public ColorProducer getColorAt() { return getSurface().getColorAt(); }
	
	/**
	 * Returns a Vector object that represents the vector normal to this surface
	 * at the point represented by the specified {@link Vector}.
	 */
	@Override
	public Producer<Vector> getNormalAt(Vector point) {
		return getSurface().getNormalAt(point);
	}
	
	/**
	 * Returns true if the ray represented by the specified Ray object intersects the surface represented by this AbstractSurfaceUI in real space.
	 */
	@Override
	public boolean intersect(Ray ray) {
		return getSurface().intersect(ray);
	}
	
	/**
	 * Returns a {@link Producer} for a {@link ShadableIntersection} representing the point along the ray produced by
	 * the specified {@link Ray} {@link Producer} that intersection between the ray and the surface occurs.
	 */
	@Override
	public Producer<ShadableIntersection> intersectAt(Producer ray) {
		return getSurface().intersectAt(ray);
	}

	@Override
	public Producer<RGB> call() throws Exception { return getSurface().call();  }

	@Override
	public Vector operate(Triple triple) {
		return getSurface().operate(triple);
	}

	@Override
	public Scope getScope(String prefix) {
		return getSurface().getScope(prefix);
	}

	@Override
	public Operator<Scalar> expect() {
		return getSurface().expect();
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return getSurface().cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return getSurface().isCancelled();
	}

	@Override
	public boolean isDone() {
		return getSurface().isDone();
	}

	@Override
	public Operator<Scalar> get() throws InterruptedException, ExecutionException {
		return getSurface().get();
	}

	@Override
	public Operator<Scalar> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return getSurface().get(timeout, unit);
	}

	/**
	 * Returns the value of shade() obtained from the AbstractSurface object stored by this AbstractSurfaceUI.
	 */
	@Override
	public Producer<RGB> shade(ShaderContext parameters) {
		return this.surface.shade(parameters);
	}

	/** Returns the name of this AbstractSurfaceUI as a String object. */
	@Override
	public String toString() {
		return this.getName();
	}
}
