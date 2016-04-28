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
  A ProgressMonitor object can be used to detect and monitor internal progress.
*/

public interface ProgressMonitor {
	/**
	  Returns the increment size used by this ProgressMonitor object.
	*/
	
	public int getIncrementSize();
	
	/**
	  Returns the total size of this ProgressMonitor object.
	*/
	
	public int getTotalSize();
	
	/**
	  Returns the increment of this ProgressMonitor object.
	*/
	
	public int getIncrement();
	
	/**
	  Increments this ProgressMonitor object.
	*/
	
	public void increment();
}
