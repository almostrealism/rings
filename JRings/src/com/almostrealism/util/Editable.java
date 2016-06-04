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

package com.almostrealism.util;

/**
 * Classes that implement the Editable interface can have editable
 * properties modified with a general set of methods.
 * 
 * @author Mike Murray
 */
public interface Editable {
    /**
     * An Editable.Selection object stores a set of options and a selection.
     */
    public class Selection {
        private String options[];
        private int selected;
        
        public Selection(String options[]) {
            this.options = options;
            this.selected = 0;
        }
        
        public String[] getOptions() { return this.options; }
        public void setSelected(int index) { this.selected = index; }
        public int getSelected() { return this.selected; }
        
        public String toString() { return this.options[this.selected]; }
    }
    
	/**
	 * Returns an array of String objects with names for each editable property
	 * of this Editable object.
	 */
	public String[] getPropertyNames();
	
	/**
	 * Returns an array of String objects with descriptions for each editable property
	 * of this Editable object.
	 */
	public String[] getPropertyDescriptions();
	
	/**
	 * Returns an array of Class objects representing the class types of each editable
	 * property of this Editable object.
	 */
	public Class[] getPropertyTypes();
	
	/**
	 * Returns the values of the properties of this Editable object as an Object array.
	 */
	public Object[] getPropertyValues();
	
	/**
	 * Sets the value of the property of this Editable object at the specified index
	 * to the specified value.
	 */
	public void setPropertyValue(Object value, int index);
	
	/**
	 * Sets the values of properties of this Editable object to those specified.
	 */
	public void setPropertyValues(Object values[]);
	
	/**
	 * @return  An array of Producer objects containing the property values of those
	 *          properties that are repeatedly evaluated.
	 */
	public Producer[] getInputPropertyValues();
	
	/**
	 * @param index  Index of input property (array index from this.getInputPropertyValue).
	 * @param p  Producer object to use for input property.
	 */
	public void setInputPropertyValue(int index, Producer p);
}
