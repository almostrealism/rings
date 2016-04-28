/*
 * Copyright (C) 2004-05  Mike Murray
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License (version 2)
 *  as published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 */

package com.almostrealism.raytracer.surfaceUI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.raytracer.shaders.ShaderParameters;
import net.sf.j3d.ui.dialogs.Dialog;
import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.GraphicsConverter;
import net.sf.j3d.util.graphics.RGB;

/**
 * AbstractSurfaceUI is an abstract implementation of the SurfaceUI interface
 * that takes care of all of the standard methods of SurfaceUI that all SurfaceUI implementations
 * use in the same way. The name is "Surface" by default.
 */
public abstract class AbstractSurfaceUI implements SurfaceUI {
  protected AbstractSurface surface;
  
  private String name;
  
  private Icon icon;
  private RGB lastColor;
  private Color background = Color.white;

	/**
	 * Sets all values to the default.
	 */
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
	 * Sets the name of this AbstractSurfaceUI to the specified String object.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the name of this AbstractSurfaceUI as a String object.
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * Returns false.
	 */
	public boolean hasDialog() {
		return false;
	}
	
	/**
	 * Returns null.
	 */
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
		this.surface.setParent(parent);
	}
	
	/**
	 * Sets the underlying AbstractSurface object stored by this AbstractSurfaceUI to the specified AbstractSurface object.
	 */
	public void setSurface(AbstractSurface surface) {
		this.surface = surface;
	}
	
	/**
	 * Returns the underlying AbstractSurface object stored by this AbstractSurfaceUI.
	 */
	public Surface getSurface() {
		return this.surface;
	}
	
	/**
	 * Returns the name of this AbstractSurfaceUI as a String object.
	 */
	public String toString() {
		return this.getName();
	}
	
	/**
	 * Returns the color of this AbstractSurfaceUI at the specified point as an RGB object.
	 */
	public RGB getColorAt(Vector point) {
		return this.surface.getColorAt(point);
	}
	
	/**
	 * Returns a Vector object that represents the vector normal to this surface at the point represented by the specified Vector object.
	 */
	public Vector getNormalAt(Vector point) {
		return this.surface.getNormalAt(point);
	}
	
	/**
	 * Returns true if the ray represented by the specified Ray object intersects the surface represented by this AbstractSurfaceUI in real space.
	 */
	public boolean intersect(Ray ray) {
		return this.surface.intersect(ray);
	}
	
	/**
	 * Returns an Intersection object representing the point along the ray represented by the specified Ray object
	 * that intersection between the ray and the surface represented by this AbstractSurfaceUI occurs.
	 */
	public Intersection intersectAt(Ray ray) {
		return this.surface.intersectAt(ray);
	}
	
	/**
	 * Returns the value of shade() obtained from the AbstractSurface object stored by this AbstractSurfaceUI.
	 */
	public RGB shade(ShaderParameters parameters) {
		return this.surface.shade(parameters);
	}
}
