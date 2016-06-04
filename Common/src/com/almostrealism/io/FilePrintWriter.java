/*
 * Copyright (C) 2005  Mike Murray
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

package com.almostrealism.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class FilePrintWriter implements PrintWriter {
	private StringBuffer indent;
	private java.io.PrintWriter out;
	
	public FilePrintWriter(File f) throws FileNotFoundException { 
		this.out = new java.io.PrintWriter(new FileOutputStream(f), true);
	}
	
	public void close() { this.out.close(); }
	
	public void moreIndent() { this.indent.append("    "); }
	public void lessIndent() { this.indent.delete(this.indent.length() - 4, this.indent.length()); }
	public void print(String s) { this.out.print(this.indent.toString() + s); }
	public void println(String s) { this.out.println(this.indent.toString() + s); }
	public void println() { this.out.println(); }
}
