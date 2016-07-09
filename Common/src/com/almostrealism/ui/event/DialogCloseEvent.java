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

import com.almostrealism.ui.dialogs.*;

/**
  A DialogCloseEvent object represents the event of closing a dialog.
*/

public class DialogCloseEvent extends UIEvent {
  private Dialog dialog;

	/**
	  Constructs a new DialogCloseEvent object for the specified Dialog object.
	*/
	
	public DialogCloseEvent(Dialog dialog) {
		this.dialog = dialog;
	}
	
	/**
	  Returns the dialog that has been closed.
	*/
	
	public Dialog getDialog() {
		return this.dialog;
	}
	
	/**
	  Returns "DialogCloseEvent".
	*/
	
	public String toString() {
		return "DialogCloseEvent";
	}
}
