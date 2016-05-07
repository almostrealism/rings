/*
 * Copyright (C) 2004-05  Mike Murray
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

import javax.swing.*;


/**
 * A JTextAreaPrintWriter object can be used as an interface for printing text into a JTextArea object.
 */
public class JTextAreaPrintWriter implements PrintWriter {
  private StringBuffer indent;
  
  private JTextArea textArea;

	/**
	 * Constructs a new JTextAreaPrintWriter using a new default JTextArea object.
	 */
	public JTextAreaPrintWriter() {
		this.indent = new StringBuffer();
		this.setTextArea(new JTextArea());
	}
	
	/**
	 * Constructs a new JTextAreaPrintWriter using the specified JTextArea object.
	 */
	public JTextAreaPrintWriter(JTextArea textArea) {
		this.indent = new StringBuffer();
		this.setTextArea(textArea);
	}
	
	/**
	 * Sets the JTextArea object used by this JTextAreaPrintWriter to the specified JTextArea object.
	 */
	public void setTextArea(JTextArea textArea) { this.textArea = textArea; }
	
	/**
	 * Returns the JTextArea object used by this JTextAreaPrintWriter.
	 */
	public JTextArea getTextArea() { return this.textArea; }
	
	/**
	 * Increases the indent used for this JTextAreaPrintWriter object.
	 */
	public void moreIndent() { this.indent.append("    "); }
	
	/**
	 * Reduces the indent used for this JTextAreaPrintWriter object.
	 *
	 */
	public void lessIndent() { this.indent.delete(this.indent.length() - 4, this.indent.length()); }
	
	/**
	 * Appends the specified String to the JTextArea used by this JTextAreaPrintWriter.
	 */
	public void print(String s) { this.textArea.append(this.indent + s); }
	
	/**
	 * Appends the specified String, followed by a new line character, to the JTextArea used by this JTextAreaPrintWriter.
	 */
	public void println(String s) { this.textArea.append(this.indent + s + "\n"); }
	
	/**
	 * Appends a new line character to the JTextArea used by this JTextAreaPrintWriter.
	 */
	public void println() { this.textArea.append("\n"); }
}
