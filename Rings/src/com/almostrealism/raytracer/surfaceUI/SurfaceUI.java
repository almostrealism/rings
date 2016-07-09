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


import java.awt.Graphics;

import javax.swing.Icon;

import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.engine.*;
import com.almostrealism.ui.dialogs.Dialog;

/**
 * The SurfaceUI interface is implemented by classes that represent a Surface object that can be used
 * in an application with a user interface.
 */
public interface SurfaceUI extends Surface, SurfaceWrapper {
	/**
	 * Sets the name of this SurfaceUI object to the name specified.
	 */
	public void setName(String name);
	
	/**
	 * Returns the name of this SurfaceUI object as a String object.
	 */
	public String getName();
	
	/**
	 * Returns the name of the type of surface that this SurfaceUI object represents as a String object.
	 */
	public String getType();
	
	/**
	 * Returns true if extra information about this SurfaceUI object can be specified through a dialog,
	 * false otherwise.
	 */
	public boolean hasDialog();
	
	/**
	 * Returns a Dialog object that can be used to specify extra information about this SurfaceUI object,
	 * or null if no such dialog is required.
	 */
	public Dialog getDialog();
	
	/**
	 * @return  An Icon object that should be used to represent this surface.
	 */
	public Icon getIcon();
	
	/**
	 * Draws a simple representation of the Surface represented by this SurfaceUI object on the specified Graphics object
	 * using the viewing settings of the specified Camera object.
	 */
	public void draw(Graphics g, Camera camera);
}
