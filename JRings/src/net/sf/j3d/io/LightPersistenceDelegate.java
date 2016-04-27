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

package net.sf.j3d.io;


import java.beans.*;

import com.almostrealism.raytracer.lighting.*;

/**
  A LightPersistenceDelegate object adjusts the way the a Light object is encoded into XML
  when using an XMLEncoder.
*/

public class LightPersistenceDelegate extends DefaultPersistenceDelegate {
	/**
	  Properly encodes a Light object.
	*/
	
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
		super.initialize(type, oldInstance, newInstance, out);
		
		if (oldInstance instanceof PointLight) {
			double a[] = ((PointLight)oldInstance).getAttenuationCoefficients();
			
			out.writeStatement(new Statement(oldInstance, "setAttenuationCoefficients",
						new Object[] {new Double(a[0]), new Double(a[1]), new Double(a[2])}));
		}
	}
}
