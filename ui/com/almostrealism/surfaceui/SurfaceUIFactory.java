/*
 * Copyright 2018 Michael Murray
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.almostrealism.surfaceui;

import java.awt.Graphics;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.almostrealism.graph.mesh.Mesh;
import org.almostrealism.graph.mesh.Triangle;
import org.almostrealism.space.AbstractSurface;
import org.almostrealism.space.Plane;
import org.almostrealism.space.SurfaceGroup;
import org.almostrealism.swing.Dialog;

import com.almostrealism.primitives.Cone;
import com.almostrealism.primitives.Cylinder;
import com.almostrealism.primitives.Sphere;
import com.almostrealism.primitives.SurfaceUI;


// TODO  Add Mesh dialog that allows user to configure space partition.

/**
 * The SurfaceUIFactory class provides static methods for creating SurfaceUI instances.
 * 
 * @author Michael Murray
 */
public class SurfaceUIFactory {
	// TODO  Add getIcon method.
	public static class SurfaceUIImpl extends AbstractSurfaceUI {
		private String type;
		private Class dialog;
		
		public SurfaceUIImpl() { }
		
		public SurfaceUIImpl(AbstractSurface s, String type, Class dialog) {
			super(s, "Surface");
			
			this.type = type;
			this.dialog = dialog;
		}
		
		public void setType(String type) { this.type = type; }
		
		public void setDialogClass(Class dialog) { this.dialog = dialog; }
		
		public Class getDialogClass() { return this.dialog; }
		
		/**
		 * @see SurfaceUI#hasDialog()
		 */
		public boolean hasDialog() { return (this.dialog != null); }
		
		public Dialog getDialog() {
		    try {
		        Constructor c = this.dialog.getConstructor(SurfaceUI.class);
		        return (Dialog)c.newInstance(this);
		    } catch (IllegalArgumentException iae) {
		        System.out.println("SurfaceUIImpl.getDialog: " + iae);
            } catch (InvocationTargetException ite) {
                System.out.println("SurfaceUIImpl.getDialog: " + ite);
            } catch (Exception e) {}
            
            try {
                return (Dialog)this.dialog.newInstance();
            } catch (Exception e) {
                return null;
            }
		}
		
		/**
		 * @see SurfaceUI#getType()
		 */
		public String getType() { return this.type; }
		
		/**
		 * Does nothing.
		 * Perhaps someday this will be a useful method...
		 */
		public void draw(Graphics g, Camera camera) { }
	}
	
  private static final Class typeClasses[] = {Mesh.class,
  												Cone.class,
  												Cylinder.class,
												Plane.class,
												Polynomial.class,
												Sphere.class,
												Triangle.class,
												SurfaceGroup.class};
  private static final String typeNames[] = {"Mesh",
  												"Primitive Cone",
  												"Primitive Cylinder",
												"Primitive Plane",
												"Primitive Polynomial",
												"Primitive Sphere",
												"Primitive Triangle",
												"Group"};
  private static final Class dialogTypes[] = {null,
          										null,
          										null,
          										/* EditPlaneDialog.class, */ null,
          										/* EditPolynomialDialog.class,*/ null,
          										null,
          										/* EditTriangleDialog.class, */ null,
          										null};
	
	/**
	 * Private constructor to insure that this class is not instanciated.
	 */
	private SurfaceUIFactory() {}
	
	/**
	 * @param s  AbstractSurface object to wrap.
	 * @return  An AbstractSurfaceUI implementation that wraps the AbstractSurface specified.
	 */
	public static AbstractSurfaceUI createSurfaceUI(AbstractSurface s) {
		String type = "Surface";
		Class dialog = null;
		
		i: for (int i = 0; i < SurfaceUIFactory.typeClasses.length; i++) {
			if (s.getClass().equals(SurfaceUIFactory.typeClasses[i])) {
				type = SurfaceUIFactory.typeNames[i];
				dialog = SurfaceUIFactory.dialogTypes[i];
				break i;
			}
		}
		
		return new SurfaceUIImpl(s, type, dialog);
	}
	
	/**
	 * Creates a new Surface implementation of the type given by the specified index
	 * into the array returned by the getSurfaceTypeNames method.
	 * 
	 * @param nameIndex  Index into array given by getSurfaceTypeNames that corresponds to the Surface object to construct.
	 * @return  An AbstractSurfaceUI implementation that wraps the surface specified.
	 */
	public static AbstractSurfaceUI createSurfaceUI(int nameIndex) {
		try {
			return new SurfaceUIImpl((AbstractSurface)SurfaceUIFactory.typeClasses[nameIndex].newInstance(),
										SurfaceUIFactory.typeNames[nameIndex], SurfaceUIFactory.dialogTypes[nameIndex]);
		} catch (InstantiationException ie) {
			System.out.println("SurfaceUIFactory: " + ie);
		} catch (IllegalAccessException iae) {
			System.out.println("SurfaceUIFactory: " + iae);
		}
		
		return null;
	}
	
	/**
	 * @return  An array containing the surface type names in order.
	 */
	public static String[] getSurfaceTypeNames() {
		return SurfaceUIFactory.typeNames;
	}
}
