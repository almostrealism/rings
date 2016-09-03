/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.almostrealism.rayshade;

import java.util.HashSet;
import java.util.Iterator;

import org.almostrealism.texture.ColorProducer;
import org.almostrealism.texture.RGB;

/**
 * @author Mike Murray
 */
public class ShaderSet extends HashSet<Shader> implements Shader {
    /**
     * @return  The sum of the values given by the shade method for each Shader object stored by this ShaderSet object.
     */
    public ColorProducer shade(ShaderParameters p) {
        RGB color = new RGB(0.0, 0.0, 0.0);
        
        Iterator<Shader> itr = super.iterator();
        while (itr.hasNext()) color.addTo(itr.next().shade(p));
        
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
	
	/** @return  False. */
	public boolean equals(Object o) { return false; }
	
	/** @return  "ShaderSet". */
	public String toString() { return "ShaderSet"; }
}
