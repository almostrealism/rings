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

package com.almostrealism.ui.dialogs;


import javax.swing.*;

import com.almostrealism.raytracer.primitives.*;
import com.almostrealism.ui.event.*;

/**
  The PolynomialTermsListModel class extends AbstractListModel and provides a list model that dynamically displays
  the terms of a Polynomial object.
*/

public class PolynomialTermsListModel extends AbstractListModel implements EventListener {
  private Polynomial polynomial;

	/**
	  Constructs a new PolynomialTermsListModel that displays the terms of the specified Polynomial object.
	*/
	
	public PolynomialTermsListModel(Polynomial polynomial) {
		this.polynomial = polynomial;
	}
	
	/**
	  Returns a String representation of the term of the Polynomial object
	  stored by this PolynomialTermsListModel object at the specified index.
	*/
	
	public Object getElementAt(int index) {
		PolynomialTerm term = this.polynomial.getTerms()[index];
		
		return term.toString();
	}
	
	/**
	  Returns the total number of terms stored by the Polynomial object stored by this PolynomialTermsListModel object.
	*/
	
	public int getSize() {
		return this.polynomial.getTerms().length;
	}
	
	/**
	  Method called when an event has been fired.
	*/
	
	public void eventFired(Event event) {
		if (event instanceof SurfaceEditEvent) {
			SurfaceEditEvent editEvent = (SurfaceEditEvent)event;
			
			if (editEvent.getTarget() == this.polynomial && editEvent.isDataChangeEvent() == true) {
				this.fireContentsChanged(this, 0, this.getSize());
			}
		}
	}
}
