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

package net.sf.j3d.ui.dialogs;



import javax.swing.*;

import com.almostrealism.raytracer.engine.*;
import com.almostrealism.util.*;

import net.sf.j3d.ui.event.*;

/**
  The TransformationsListModel class extends AbstractListModel and provides a list model that dynamically displays
  the transformations applied to an AbstractSurface object.
*/

public class TransformationsListModel extends AbstractListModel implements EventListener {
  private AbstractSurface surface;

	/**
	  Constructs a new TransformationsListModel that displays the transformations of the specified AbstractSurface object.
	*/
	
	public TransformationsListModel(AbstractSurface surface) {
		this.surface = surface;
	}
	
	/**
	  Returns a String representation of the transformation applied to the AbstractSurface object
	  stored by this TransformationsListModel object at the specified index.
	*/
	
	public Object getElementAt(int index) {
		TransformMatrix transform = this.surface.getTransforms()[index];
		
		return transform.toString();
	}
	
	/**
	  Returns the total number of transformations applied to the AbstractSurface object stored
	  by this TransformationsListModel object.
	*/
	
	public int getSize() {
		return this.surface.getTransforms().length;
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SurfaceEditEvent) {
			SurfaceEditEvent editEvent = (SurfaceEditEvent)event;
			
			if (editEvent.getTarget() == this.surface && editEvent.isTransformationChangeEvent() == true) {
				this.fireContentsChanged(this, 0, this.getSize());
			}
		}
	}
}
