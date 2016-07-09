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

/**
  The EventGenerator interface is implemented by classes that fire events.
*/

public interface EventGenerator {
	
	/**
	  Sets the EventHandler that is used by this EventGenerator.
	*/
	
	public void setEventHandler(EventHandler handler);
	
	/**
	  Returns the EventHandler that is being used by this EventGenerator.
	*/
	
	public EventHandler getEventHandler();
}
