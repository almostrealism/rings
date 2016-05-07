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

public interface PrintWriter {
	/**
	 * Increases the indent.
	 */
	public void moreIndent();
	
	/**
	 * Reduces the indent.
	 */
	public void lessIndent();
	
	/**
	 * Appends the specified String.
	 */
	public void print(String s);
	
	/**
	 * Appends the specified String, followed by a new line character.
	 */
	public void println(String s);
	
	/**
	 * Appends a new line character.
	 */
	public void println();
}
