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

package net.sf.j3d.ui.event;

/**
  A SceneEditEvent object represents the event of editing an element of the current scene.
*/

public abstract class SceneEditEvent extends SceneEvent {
	
	/**
	  Returns "SceneEditEvent".
	*/
	
	public String toString() {
		return "SceneEditEvent";
	}
}
