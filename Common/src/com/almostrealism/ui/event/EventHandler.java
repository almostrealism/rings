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

package com.almostrealism.ui.event;


import java.util.*;

import com.almostrealism.raytracer.Settings;
import com.almostrealism.ui.dialogs.*;

import net.sf.j3d.run.*;

/**
  An EventHandler object provides an interface for comunication between EventGenerators and EventListeners.
*/

public class EventHandler {
  private Vector listeners;

	/**
	  Constructs a new EventHandler object with no listeners.
	*/
	
	public EventHandler() {
		this.listeners = new Vector();
	}
	
	/**
	 * Adds the specified EventListener to this EventHandler. The listener will be notified through its
	 * eventFired method when an event has been fired.
	 */
	public void addListener(EventListener listener) { this.listeners.addElement(listener); }
	
	/**
	 * Removes the specified EventListener from this EventHandler.
	 */
	public void removeListener(int index) { this.listeners.removeElementAt(index); }
	
	/**
	 * Removes the specified EventListener from this EventHandler.
	 */
	public void removeListener(EventListener listener) { this.listeners.remove(listener); }
	
	/**
	 * Returns the specified EventListener.
	 */
	public EventListener getListener(int index) { return (EventListener)this.listeners.elementAt(index); }
	
	/**
	 * Returns the number of EventListeners currently registered with this EventHandler.
	 */
	public int getTotalListeners() { return this.listeners.size(); }
	
	/**
	 * Notifies all current EventListeners that an event has been fired. If the event is an instance of
	 * DialogCloseEvent and the Dialog object stored by the event is a registered as a listener with this
	 * EventHandler object, the dialog will be removed. If the dialog is registered more than once,
	 * only the first instance will be removed.
	 */
	public void fireEvent(Event event) {
		if (Settings.produceOutput && Settings.produceEventHandlerOutput) {
			Settings.eventOut.println("EventHandler (" + this.toString() + "): Event Fired (" + event.toString() + "); " +
						"Notifying listeners (" + this.getTotalListeners() + ")");
		}
		
		if (event instanceof DialogCloseEvent) {
			Dialog dialog = ((DialogCloseEvent)event).getDialog();
			
			int index = -1;
			int i = 0;
			
			Enumeration en = this.listeners.elements();
			
			i: while (en.hasMoreElements()) {
				if (en.nextElement() == dialog) {
					index = i;
					break i;
				}
				
				i++;
			}
			
			if (index >= 0)
				this.removeListener(index);
		}
		
		for(int i = 0; i < this.getTotalListeners(); i++) {
			this.getListener(i).eventFired(event);
		}
	}
}
