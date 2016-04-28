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

package net.sf.j3d.run;

import java.io.*;

/**
  The WriteProperties class provides a main method that writes out a default properties file specified by the string argument.
*/

public class WriteProperties {
	/**
	  Writes a default properties to the file path specified by the first element of args.
	*/
	
	public static void main(String args[]) {
		try {
			Settings.getProperties().store(new FileOutputStream(new File(args[0])), "Properties file for Rings (Version " + Settings.version + ")");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
