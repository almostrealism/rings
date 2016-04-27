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


import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Statement;

import com.almostrealism.raytracer.engine.AbstractSurface;
import com.almostrealism.raytracer.primitives.Triangle;
import com.almostrealism.raytracer.shaders.Shader;

// TODO  Add more efficient encoding of Mesh (avoid storing so many vector objects = decreased file size)

/**
 * A SurfacePersistenceDelegate object adjusts the way the an AbstractSurface or AbstractSurfaceUI object
 * is encoded into XML when using an XMLEncoder.
 */
public class SurfacePersistenceDelegate extends DefaultPersistenceDelegate {
	/**
	 * Properly encodes an AbstractSurface or AbstractSurfaceUI object.
	 */
	public void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {
		super.initialize(type, oldInstance, newInstance, out);
		
		System.out.println(newInstance);
		
		AbstractSurface s = null;
		
		if (oldInstance instanceof AbstractSurface) {
			s = (AbstractSurface)oldInstance;
			
			if (s instanceof Triangle) {
				out.writeStatement(new Statement(oldInstance, "setVertices", ((Triangle)s).getVertices()));
			}
			
//			if (s instanceof Mesh) {
//				Object o = ((Mesh)s).encode();
//				
//				if (!(o instanceof Mesh)) {
//					try {
//						this.initialize(o.getClass(), o, o.getClass().newInstance(), out);
//					} catch (InstantiationException e) {
//						e.printStackTrace();
//					} catch (IllegalAccessException e) {
//						e.printStackTrace();
//					}
//					
//					return;
//				}
//			}
			
			out.writeStatement(new Statement(s, "calculateTransform", new Object[0]));
			
			if (s.getShaderSet() != null)
				out.writeStatement(new Statement(s, "setShaders",
								new Object[] {s.getShaderSet().toArray(new Shader[0])}));
//		} else if (oldInstance instanceof AbstractSurfaceUI) {
//			s = (AbstractSurface)((AbstractSurfaceUI)oldInstance).getSurface();
//			
//			out.writeStatement(new Statement(oldInstance, "setSurface",
//					new Object[] {s}));
		} else {
			return;
		}
	}
}
