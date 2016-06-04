/*
 * Copyright (C) 2004  Mike Murray
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

package com.almostrealism.raytracer.lighting;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.engine.Intersection;
import com.almostrealism.raytracer.engine.Ray;
import com.almostrealism.util.Vector;
import com.almostrealism.util.graphics.RGB;

/**
  A PointLightGrid object stores a grid of PointLight objects.
*/

public class PointLightGrid extends AbstractSurface implements Light {
  private double intensity;
  private RGB color;
  
  private PointLight lights[];

	/**
	  Constructs a new PointLightGrid object with the specified width, height,
	  and x and y spacing between lights and uses a default PointLight object
	  for each light in the grid. After the grid is created the total intensity
	  will be set to 1.0.
	*/
	
	public PointLightGrid(int width, int height, double xSpace, double ySpace) {
		this.updateGrid(width, height, xSpace, ySpace, new PointLight());
		this.setIntensity(1.0);
	}
	
	/**
	  Constructs a new PointLightGrid object with the specified width, height,
	  and x and y spacing between lights using the data from the specified
	  PointLight object for each light in the grid.
	*/
	
	public PointLightGrid(int width, int height, double xSpace, double ySpace, PointLight prototype) {
		this.updateGrid(width, height, xSpace, ySpace, prototype);
	}
	
	/**
	  Updates the light grid stored by this PointLightGrid object to have the specified width, height,
	  and x and y spaceing between lights using the data from the specified Point Light object for
	  each light in the grid.
	*/
	
	public void updateGrid(int width, int height, double xSpace, double ySpace, PointLight prototype) {
		this.lights = new PointLight[width * height];
		
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				Vector location = new Vector(i * xSpace, j * ySpace, 0.0);
				location = super.getTransform(true).transformAsLocation(location);
				
				this.lights[i * width + j] = new PointLight(location, prototype.getIntensity(), new RGB(prototype.getColor().getRed(), prototype.getColor().getGreen(), prototype.getColor().getBlue()));
			}
		}
		
		this.intensity = prototype.getIntensity() * width * height;
		
		if (this.color != null)
			this.setColor(color);
		else
			this.setColor(new RGB(1.0, 1.0, 1.0));
	}
	
	/**
	  Sets the total intensity of this PointLightGrid object (the sum of intensities of
	  all lights in the grid) to the specified double value.
	*/
	
	public void setIntensity(double intensity) {
		this.intensity = intensity;
		
		for (int i = 0; i < this.lights.length; i++) {
			this.lights[i].setIntensity(this.intensity / this.lights.length);
		}
	}
	
	/**
	  Sets the color of the PointLight objects stored by this PointLightGrid object
	  to the color represented by the specified RGB object.
	*/
	
	public void setColor(RGB color) {
		super.setColor(color);
		
		if (this.lights == null)
			return;
		
		for (int i = 0; i < this.lights.length; i++) {
			this.lights[i].setColor(super.getColor());
		}
	}
	
	/**
	  Returns the total intensity of this PointLightGrid
	  (the sum of the intensities of all lights in the grid).
	*/
	
	public double getIntensity() {
		return this.intensity;
	}
	
	/**
	  Returns the array of PointLight objects stored by this PointLightGrid object.
	*/
	
	public PointLight[] getLights() {
		return this.lights;
	}
	
	/**
	  Returns a zero vector.
	*/
	
	public Vector getNormalAt(Vector point) {
		return new Vector(0.0, 0.0, 0.0);
	}
	
	/**
	  Returns false.
	*/
	
	public boolean intersect(Ray ray) {
		return false;
	}
	
	/**
	  Returns null.
	*/
	
	public Intersection intersectAt(Ray ray) {
		return null;
	}
}
