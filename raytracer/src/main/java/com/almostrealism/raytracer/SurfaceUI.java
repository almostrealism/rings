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

package com.almostrealism.raytracer;


import java.awt.Graphics;

import javax.swing.Icon;

import org.almostrealism.geometry.Camera;
import org.almostrealism.space.ShadableSurface;
import org.almostrealism.space.ShadableSurfaceWrapper;
import org.almostrealism.swing.Dialog;
import io.almostrealism.uml.Nameable;

/**
 * The SurfaceUI interface is implemented by classes that represent a Surface object that can be used
 * in an application with a user interface.
 */
public interface SurfaceUI extends ShadableSurface, ShadableSurfaceWrapper, Nameable {
	/**
	 * Returns the name of the type of surface that this SurfaceUI object represents as a String object.
	 */
	String getType();
	
	/**
	 * Returns true if extra information about this SurfaceUI object can be specified through a dialog,
	 * false otherwise.
	 */
	boolean hasDialog();
	
	/**
	 * Returns a Dialog object that can be used to specify extra information about this SurfaceUI object,
	 * or null if no such dialog is required.
	 */
	Dialog getDialog();
	
	/**
	 * @return  An Icon object that should be used to represent this surface.
	 */
	Icon getIcon();
	
	/**
	 * Draws a simple representation of the Surface represented by this SurfaceUI object on the specified Graphics object
	 * using the viewing settings of the specified Camera object.
	 */
	void draw(Graphics g, Camera camera);
}
