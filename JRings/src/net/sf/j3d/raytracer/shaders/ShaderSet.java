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

package net.sf.j3d.raytracer.shaders;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import net.sf.j3d.util.graphics.RGB;


/**
 * @author Mike Murray
 */
public class ShaderSet extends HashSet implements Shader {
    /**
     * Adds the specified Object to this set and returns true.
     * 
     * @throws IllegalArgumentException  If the specified Object is not an instance of Shader.
     */
    public boolean add(Object o) {
        if (o instanceof Shader == false)
            throw new IllegalArgumentException("Illegal argument: " + o.toString());
        
        return super.add(o);
    }
    
    /**
     * Adds all of the elements stored by the specified Collection object to this set.
     * Returns true if the set changed as a result.
     * 
     * @throws IllegalArgumentException  If an element in the specified Collection object is not
     * 									an instance of Shader. Note: Elements that have not yet been added
     * 									to the set at the time this error occurs will not be added.
     * @throws NullPointerException  If the specified Collection object is null.
     */
    public boolean addAll(Collection c) {
        boolean added = false;
        
        Iterator itr = c.iterator();
        
        while (itr.hasNext()) {
            this.add(itr.next());
            added = true;
        }
        
        return added;
    }
    
    /**
     * @return  The sum of the values given by the shade method for each Shader object stored by this ShaderSet object.
     */
    public RGB shade(ShaderParameters p) {
        RGB color = new RGB(0.0, 0.0, 0.0);
        
        Iterator itr = super.iterator();
        while (itr.hasNext()) color.addTo(((Shader)itr.next()).shade(p));
        
        return color;
    }
    
	/**
	 * @throws IllegalArgumentException  If args[0] is not a ShaderParameters object.
	 * @return  this.shade(args[0]).
	 */
	public RGB evaluate(Object args[]) {
	    if (args[0] instanceof ShaderParameters) {
	        return this.shade((ShaderParameters)args[0]);
	    } else {
	        throw new IllegalArgumentException("Illegal argument: " + args[0]);
	    }
	}
	
	/**
	 * @return  False.
	 */
	public boolean equals(Object o) { return false; }
	
	/**
	 * @return  "ShaderSet".
	 */
	public String toString() { return "ShaderSet"; }
}
