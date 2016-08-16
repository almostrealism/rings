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

package com.almostrealism.raytracer.ui;


import javax.swing.AbstractListModel;

import org.almostrealism.swing.Event;
import org.almostrealism.swing.EventListener;

import com.almostrealism.raytracer.primitives.Polynomial;
import com.almostrealism.raytracer.primitives.PolynomialTerm;

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
