/*
 * Copyright (C) 2004-06  Mike Murray
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

package net.sf.j3d.io;

import java.beans.*;

/**
  A CameraPersistenceDelegate object adjusts the way the a Camera object is encoded into XML
  when using an XMLEncoder.
*/

public class CameraPersistenceDelegate extends DefaultPersistenceDelegate {
	/**
	  Properly encodes a Camera object.
	*/
	
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
		super.initialize(type, oldInstance, newInstance, out);
		
		out.writeStatement(new Statement(oldInstance, "updateUVW", new Object[0]));
	}
}
