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

/**
  The Dialog interface is implemented by classes that represent dialogs that can be displayed for a user.
*/

public interface Dialog {
	/**
	  Shows this dialog.
	*/
	
	public void showDialog();
	
	/**
	  Closes this dialog if it is open.
	*/
	
	public void closeDialog();
}
