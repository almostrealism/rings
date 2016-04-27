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

package com.almostrealism.raytracer.engine;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.almostrealism.raytracer.primitives.Mesh;
import com.almostrealism.raytracer.shaders.DiffuseShader;
import com.almostrealism.raytracer.shaders.Shader;
import com.almostrealism.raytracer.shaders.ShaderParameters;
import com.almostrealism.raytracer.shaders.ShaderSet;

import net.sf.j3d.run.Settings;
import net.sf.j3d.util.TransformMatrix;
import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

/**
 * {@link AbstractSurface} is an abstract implementation of {@link Surface} that takes care of all of the
 * standard methods of {@link Surface} that are shared by all Surface implementations in the same way.
 * By default the location is at the origin, the size is 1.0, and the color is black. Also, an
 * {@link AbstractSurface} uses a {@link DiffuseShader} by default.
 * 
 * @author  Mike Murray
 */
public abstract class AbstractSurface implements Surface {
  private Vector location;
  private double size;
  
  private boolean shadeFront, shadeBack;
  
  private double scaleX, scaleY, scaleZ;
  private double rotateX, rotateY, rotateZ;
  
  private TransformMatrix transforms[];
  private TransformMatrix transform, completeTransform;
  private boolean transformCurrent;
  
  private RGB color;
  
  private double rindex = 1.0, reflectP = 1.0, refractP = 0.0;
  
  private Texture textures[];
  private ShaderSet shaders;
  
  private AbstractSurface parent;

	/**
	 * Sets all values of this AbstractSurface to the defaults specified above.
	 */
	public AbstractSurface() {
		this.setShadeFront(true);
		this.setShadeBack(false);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setTextures(new Texture[0]);
		this.setShaders(new Shader[] {DiffuseShader.defaultDiffuseShader});
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		this.setSize(1.0);
		
		this.setScaleCoefficients(1.0, 1.0, 1.0);
		this.setRotationCoefficients(0.0, 0.0, 0.0);
		
		this.setColor(new RGB(0.0, 0.0, 0.0));
	}
	
	/**
	 * Sets the location and size of this AbstractSurface to those specifed, and uses the defaults for the other values.
	 */
	public AbstractSurface(Vector location, double size) {
		this.setShadeFront(true);
		this.setShadeBack(false);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setTextures(new Texture[0]);
		this.setShaders(new Shader[] {DiffuseShader.defaultDiffuseShader});
		
		this.setLocation(location);
		this.setSize(size);
		
		this.setScaleCoefficients(1.0, 1.0, 1.0);
		this.setRotationCoefficients(0.0, 0.0, 0.0);
		
		this.setColor(new RGB(0.0, 0.0, 0.0));
	}
	
	/**
	 * Sets the location, size, and color of this AbstractSurface to those specified.
	 */
	public AbstractSurface(Vector location, double size, RGB color) {
		this.setShadeFront(true);
		this.setShadeBack(false);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setTextures(new Texture[0]);
		this.setShaders(new Shader[] {DiffuseShader.defaultDiffuseShader});
		
		this.setLocation(location);
		this.setSize(size);
		
		this.setScaleCoefficients(1.0, 1.0, 1.0);
		this.setRotationCoefficients(0.0, 0.0, 0.0);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setColor(color);
	}
	
	/**
	 * Sets the location, size, and color of this AbstractSurface to those specified.
	 */
	public AbstractSurface(Vector location, double size, RGB color, boolean addDefaultDiffuseShader) {
		this.setShadeFront(true);
		this.setShadeBack(false);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setTextures(new Texture[0]);
		if (addDefaultDiffuseShader)
			this.setShaders(new Shader[] {DiffuseShader.defaultDiffuseShader});
		
		this.setLocation(location);
		this.setSize(size);
		
		this.setScaleCoefficients(1.0, 1.0, 1.0);
		this.setRotationCoefficients(0.0, 0.0, 0.0);
		
		this.setTransforms(new TransformMatrix[0]);
		
		this.setColor(color);
	}
	
	/**
	 * Sets the parent surface group of this AbstractSurface to the specified SurfaceGroup object.
	 */
	public void setParent(SurfaceGroup parent) { this.parent = parent; }
	
	/**
	 * Returns the parent of this AbstractSurface as a SurfaceGroup object.
	 */
	public SurfaceGroup getParent() { return (SurfaceGroup)this.parent; }
	
	/**
	 * Sets the flag indicating that the front side of this AbstractSurface should be shaded
	 * to the specified boolean value.
	 */
	public void setShadeFront(boolean shade) { this.shadeFront = shade; }
	
	/**
	 * Sets the flag indicating that the back side of this AbstractSurface should be shaded
	 * to the specified boolean value.
	 */
	public void setShadeBack(boolean shade) { this.shadeBack = shade; }
	
	/**
	 * Returns true if the front side of this AbstractSurface should be shaded.
	 * The "front side" is the side that the Vector object returned by the getNormalAt()
	 * method for this AbstractSurface points outward from.
	 */
	public boolean getShadeFront() {
	    if (this.parent != null && this.parent.getShadeFront())
	        return true;
	    else
	        return this.shadeFront;
	}
	
	/**
	 * Returns true if the back side of this AbstractSurface should be shaded.
	 * The "back side" is the side that the vector opposite the Vector object
	 * returned by the getNormalAt() method for this AbstractSurface points outward from.
	 */
	public boolean getShadeBack() {
	    if (this.parent != null && this.parent.getShadeBack())
	        return true;
	    else
	        return this.shadeBack;
	}
	
	/**
	 * @return  A Mesh object with location, size, color, scale coefficients,
	 *          rotation coefficients, and transformations as this AbstractSurface.
	 */
	public Mesh triangulate() {
		Mesh m = new Mesh();
		
		m.setLocation(this.getLocation());
		m.setSize(this.getSize());
		m.setColor(this.getColor());
		m.setScaleCoefficients(this.scaleX, this.scaleY, this.scaleZ);
		m.setRotationCoefficients(this.rotateX, this.rotateY, this.rotateZ);
		m.setTransforms(this.getTransforms());
		
		return m;
	}
	
	public void setIndexOfRefraction(double n) { this.rindex = n; }
	public double getIndexOfRefraction() { return this.rindex; }
	public double getIndexOfRefraction(Vector p) { return this.rindex; }
	
	public void setReflectedPercentage(double p) { this.reflectP = p; }
	public void setRefractedPercentage(double p) { this.refractP = p; }
	
	public double getReflectedPercentage() { return this.reflectP; }
	public double getReflectedPercentage(Vector p) { return this.reflectP; }
	public double getRefractedPercentage() { return this.refractP; }
	public double getRefractedPercentage(Vector p) { return this.refractP; }
	
	/**
	 * Sets the location of this AbstractSurface to the specified Vector object.
	 * This method calls calulateTransform() after it is completed.
	 */
	public void setLocation(Vector location) {
		this.location = location;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the size of this AbstractSurface to the specified double value.
	 */
	public void setSize(double size) {
		this.size = size;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the values used to scale this AbstractSurface on the x, y, and z axes when it is rendered to the specified double values.
	 * This method calls calculateTransform() after it is completed.
	 */
	public void setScaleCoefficients(double x, double y, double z) {
		this.scaleX = x;
		this.scaleY = y;
		this.scaleZ = z;
		
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the angle measurements (in radians) used to rotate this AbstractSurface about the x, y, and z axes when it is rendered
	 * to the specified double values. This method calls calculateTransform() after it is completed.
	 */
	public void setRotationCoefficients(double x, double y, double z) {
		this.rotateX = x;
		this.rotateY = y;
		this.rotateZ = z;
		
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the TransformMatrix object at the specified index used to transform this Surface object when it is rendered
	 * to the TransformMatrix object specified. This method calls calculateTransform() after it is completed.
	 */
	public void setTransform(int index, TransformMatrix transform) {
		this.transforms[index] = transform;
		
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the TransformMatrix objects used to transform this AbstractSurface when it is rendered
	 * to those stored in the specified TransformMatrix object array. If the specified array is null,
	 * an IllegalArgumentException will be thrown. This method calls calculateTransform() after it
	 * is completed.
	 */
	public void setTransforms(TransformMatrix transforms[]) throws IllegalArgumentException {
		if (transforms == null)
			throw new IllegalArgumentException();
		
		this.transforms = transforms;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Applies the transformation represented by the specified TransformMatrix to this AbstractSurface when it is rendered.
	 * This method calls calculateTransform() after it is completed.
	 */
	public void addTransform(TransformMatrix transform) {
		TransformMatrix newTransforms[] = new TransformMatrix[this.transforms.length + 1];
		
		System.arraycopy(this.transforms, 0, newTransforms, 0, this.transforms.length);
		newTransforms[newTransforms.length - 1] = transform;
		
		this.transforms = newTransforms;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Removes the TransformMatrix object at the specified index from this Surface object.
	 * This method calls calculateTransform() after it is completed.
	 */
	public void removeTransform(int index) {
		TransformMatrix newTransforms[] = new TransformMatrix[this.transforms.length - 1];
		
		System.arraycopy(this.transforms, 0, newTransforms, 0, index);
		
		if (index != this.transforms.length - 1) {
			System.arraycopy(this.transforms, index + 1, newTransforms, index, this.transforms.length - (index + 1));
		}
		
		this.transforms = newTransforms;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Calculates the complete transformation that will be applied to this AbstractSurface when it is rendered
	 * and stores it for later use. The transformations are applied in the following order: translate (location),
	 * scale (size), rotate x, rotate y, rotate z. Other transforms are applied last and in the order they were added.
	 */
	public void calculateTransform() {
		if (this.transformCurrent) return;
		
		if (Settings.produceOutput && Settings.produceSurfaceOutput) {
			Settings.surfaceOut.println(this.toString() + ": Calculating transform...");
		}
		
		
		this.transform = new TransformMatrix();
		
		for(int i = 0; i < this.transforms.length; i++) {
			this.transform = this.transform.multiply(this.transforms[i]);
		}
		
		this.completeTransform = new TransformMatrix();
		
		if (this.location != null) {
			this.completeTransform =
				this.completeTransform.multiply(TransformMatrix.createTranslationMatrix(
						this.location.getX(), this.location.getY(), this.location.getZ()));
		}
		
		this.completeTransform = this.completeTransform.multiply(TransformMatrix.createScaleMatrix(this.scaleX * this.size, this.scaleY * this.size, this.scaleZ * this.size));
		
		if (this.rotateX != 0.0) {
			this.completeTransform = this.completeTransform.multiply(TransformMatrix.createRotateXMatrix(this.rotateX));
		}
		
		if (this.rotateY != 0.0) {
			this.completeTransform = this.completeTransform.multiply(TransformMatrix.createRotateYMatrix(this.rotateY));
		}
		
		if (this.rotateZ != 0.0) {
			this.completeTransform = this.completeTransform.multiply(TransformMatrix.createRotateZMatrix(this.rotateZ));
		}
		
		if (Settings.produceOutput && Settings.produceSurfaceOutput) {
			Settings.surfaceOut.println(this.toString() + ": Basic transform:");
			Settings.surfaceOut.println(this.completeTransform.toString());
		}
		
		if (this.transform != null) {
			this.completeTransform = this.completeTransform.multiply(this.transform);
		}
		
		this.transformCurrent = true;
		
		if (Settings.produceOutput && Settings.produceSurfaceOutput) {
			Settings.surfaceOut.println(this.toString() + ": Complete transform:");
			Settings.surfaceOut.println(this.completeTransform.toString());
		}
	}
	
	/**
	 * Sets the Texture object (used to color this AbstractSurface) at the specified index
	 * to the specified Texture object.
	 */
	public void setTexture(int index, Texture texture) {
		this.textures[index] = texture;
	}
	
	/**
	 * Sets the Texture objects (used to color this AbstractSurface) to those specified.
	 */
	public void setTextures(Texture textures[]) {
		this.textures = textures;
	}
	
	/**
	 * Appends the specified Texture object to the list of Texture objects used to color this AbstractSurface.
	 */
	public void addTexture(Texture texture) {
		Texture newTextures[] = new Texture[this.textures.length + 1];
		
		for (int i = 0; i < this.textures.length; i++) { newTextures[i] = this.textures[i]; }
		newTextures[newTextures.length - 1] = texture;
		
		this.textures = newTextures;
	}
	
	/**
	 * Removes the Texture object at the specified index from the list of Texture objects used
	 * to color this AbstractSurface.
	 */
	public void removeTexture(int index) {
		Texture newTextures[] = new Texture[this.textures.length - 1];
		
		for (int i = 0; i < index; i++) { newTextures[i] = this.textures[i]; }
		for (int i = index + 1; i < newTextures.length; i++) { newTextures[i] = this.textures[i]; }
		
		this.textures = newTextures;
        }
	
	/**
	 * Returns a Set object that maintains the Texture objects stored by this AbstractSurface.
	 */
	public Set getTextureSet() {
		Set textureSet = new Set() {
			/**
			 * @return  The number of elements stored by this set.
			 */
			public int size() { return textures.length; }
			
			/**
			 * @return  True if this set contains no elements, false otherwise.
			 */
			public boolean isEmpty() {
				if (textures.length <= 0)
					return true;
				else
					return false;
			}
			
			/**
			 * @return  An Iterator object using the elements stored by this set.
			 */
			public Iterator iterator() {
				Iterator itr = new Iterator() {
					int index = 0;

					public boolean hasNext() {
						if (index < textures.length)
							return true;
						else
							return false;
					}

					public Object next() throws NoSuchElementException {
						if (this.index >= textures.length)
							throw new NoSuchElementException("No element at " + this.index);
						return textures[this.index++];
					}

					public void remove() {
						removeTexture(this.index);
					}
				};

				return itr;
			}

			/**
			 * @return  An array containing all of the elements stored by this set.
			 */
			public Object[] toArray() { return textures; }
			
			/**
			 * @return  An array containing all of the elements stored by this set.
			 */
			public Object[] toArray(Object o[]) { return this.toArray(); }
			
			/**
			 * Adds the specified Object to this set and returns true.
			 * 
			 * 
			 * @throws IllegalArgumentException  If the specified Object is not an instance of Texture.
			 */
			public boolean add(Object o) {
				if (o instanceof Texture == false)
					throw new IllegalArgumentException("Illegal argument: " + o.toString());
				
				addTexture((Texture)o);
				
				return true;
			}
			
			/**
			 * Adds all of the elements stored by the specified Collection object to this set.
			 * @return  True if the set changed as a result.
			 * 
			 * @throws IllegalArgumentException  If an element in the specified Collection object is not
			 *                                   an instance of Texture. Note: Elements that have not yet been added
			 *                                   to the set at the time this error occurs will not be added.
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
			 * Removes all occurences specified element from this set and returns true
			 * if the set changed as a result.
			 */
			public boolean remove(Object o) {
				boolean removed = false;
				
				for (int i = 0; i < textures.length; i++) {
					if (o.equals(textures[i])) {
						removeTexture(i--);
						removed = true;
					}
				}
				
				return removed;
			}
			
			/**
			 * Removes all of the elements stored by the specified Collection object from this set.
			 * @return  True if the set changed as a result.
			 * 
			 * @throws NullPointerException  If the specified Collection object is null.
			 */
			public boolean removeAll(Collection c) {
				if (c == null)
					throw new NullPointerException();
				
				boolean removed = false;
				
				Iterator itr = c.iterator();
				
				while (itr.hasNext()) {
					if (this.remove(itr.next()))
						removed = true;
				}
				
				return removed;
			}
			
			/**
			 * Removes all elements stored by this set that are not contained in the specified Collection object.
			 * @return  True if the set changed as a result.
			 * 
			 * @throws NullPointerException  If the specified Collection object is null.
			 */
			public boolean retainAll(Collection c) {
				if (c == null)
					throw new NullPointerException();
				
				boolean removed = false;
				
				Iterator itr = this.iterator();
				
				while (itr.hasNext()) {
					if (c.contains(itr.next()) == false) {
						itr.remove();
						removed = true;
					}
				}
				
				return removed;
			}
			
			/**
			 * Removes all elements of this set.
			 */
			public void clear() { textures = new Texture[0]; }
			
			/**
			 * @return  True if this set contains the specified Object, false otherwise.
			 */
			public boolean contains(Object o) {
				if (o instanceof Texture != true)
					return false;
				
				for (int i = 0; i < textures.length; i++) {
					if (o == null ? textures[i] == null : o.equals(textures[i]))
						return true;
				}
				
				return false;
			}
			
			/**
			 * @return  True if this set contains all of the elements of the specified Collection object.
			 * 
			 * @throws NullPointerException  If the specified Collection object is null.
			 */
			public boolean containsAll(Collection c) {
				if (c == null)
					throw new NullPointerException();
				
				Iterator itr = c.iterator();
				
				while (itr.hasNext()) {
					if (this.contains(itr.next()) == false)
						return false;
				}
				
				return true;
			}
			
			/**
			 * @return  True if the specified object is also an instance of Set with elements that
			 *          are equal to those.
			 */
			public boolean equals(Object o) {
				if (o instanceof Set == false)
					return false;
				
				if (((Set)o).size() != this.size())
					return false;
				
				if (this.containsAll((Set)o))
					return true;
				else
					return false;
			}
			
			/**
			 * @return  An integer hash code for this set by adding the hash codes for all elements
			 *          it stores.
			 */
			public int hashCode() {
				int hash = 0;
				
				Iterator itr = this.iterator();
				
				while (itr.hasNext())
					hash += itr.next().hashCode();
				
				return hash;
			}
		};
		
		return textureSet;
	}
	
	/**
	 * Sets the Shader objects (used to shade this AbstractSurface) to those specified.
	 */
	public void setShaders(Shader shaders[]) {
	    if (this.shaders == null) this.shaders = new ShaderSet();
	    
		this.shaders.clear();
		
		for (int i = 0; i < shaders.length; i++) this.shaders.add(shaders[i]);
	}
	
	/**
	 * @param set  New ShaderSet object to use for shading.
	 */
	public void setShaders(ShaderSet set) { this.shaders = set; }
	
	/**
	 * Appends the specified Shader object to the list of Shader objects used to shade this AbstractSurface.
	 */
	public boolean addShader(Shader shader) {
		if (this.shaders == null) this.shaders = new ShaderSet();
		return this.shaders.add(shader);
	}
	
	/**
	 * Returns a Set object that maintains the Shader objects stored by this AbstractSurface.
	 */
	public ShaderSet getShaderSet() { return this.shaders; }
	
	/**
	 * Calculates a color value for this AbstractSurface using the sum of the values
	 * calculated by the Shader objects stored by this AbstractSurface and the parent
	 * of this AbstractSurface and returns this value as an RGB object.
	 */
	public RGB shade(ShaderParameters p) {
		p.setSurface(this);
		
		RGB color = null;
		
		if (this.shaders == null)
			color = new RGB(0.0, 0.0, 0.0);
		else
			color = this.shaders.shade(p);
		
		if (this.getParent() != null)
			color.addTo(this.getParent().shade(p));
		
		return color;
	}
	
	/**
	 * Sets the color of this AbstractSurface to the color represented by the specified RGB object.
	 */
	public void setColor(RGB color) { this.color = color; }
	
	/**
	 * Returns the location of this AbstractSurface as a Vector object.
	 */
	public Vector getLocation() { return this.location; }
	
	/**
	 * Returns the size of this AbstractSurface as a double value.
	 */
	public double getSize() { return this.size; }
	
	/**
	 * Returns an array of double values containing the values used to scale this AbstractSurface
	 * on the x, y, and z axes when it is rendered.
	 */
	public double[] getScaleCoefficients() {
		double scale[] = {this.scaleX, this.scaleY, this.scaleZ};
		
		return scale;
	}
	
	/**
	 * Returns an array of double values containing the angle measurements (in radians) used to rotate
	 * this AbstractSurface about the x, y, and z axes when it is rendered as an array of double values.
	 */
	public double[] getRotationCoefficients() {
		double rotation[] = {this.rotateX, this.rotateY, this.rotateZ};
		
		return rotation;
	}
	
	/**
	 * Returns the TransformMatrix object used to transform this AbstractSurface when it is rendered.
	 * This TransformMatrix does not represents the transformations due to fixed scaling and rotation.
	 */
	public TransformMatrix getTransform() { return this.getTransform(false); }
	
	/**
	 * Returns the TransformMatrix object used to transform this AbstractSurface when it is rendered.
	 * If the specified boolean value is true, this TransformMatrix includes the transformations due to fixed scaling and rotation.
	 */
	public TransformMatrix getTransform(boolean include) {
		this.calculateTransform();
		
		if (include) {
			return this.completeTransform;
		} else {
			return this.transform;
		}
	}
	
	/**
	 * Returns the TransformMatrix objects used to transform this Surface object when it is rendered
	 * as an array of TransformMatrix objects. This array does not include the TransformMatrix objects
	 * that account for fixed scaling and rotation.
	 */
	public TransformMatrix[] getTransforms() { return this.transforms; }
	
	/**
	 * Returns the Texture object at the specified index in the list of Texture objects used
	 * to color this AbstractSurface.
	 */
	public Texture getTexture(int index) { return this.textures[index]; }
	
	/**
	 * Returns the list of Texture objects used to color this AbstractSurface as an array of Texture objects.
	 */
	public Texture[] getTextures() { return this.textures; }
	
	/**
	 * Returns the color of this AbstractSurface as an RGB object.
	 */
	public RGB getColor() { return this.color; }
	
	/**
	 * @return  The color of this AbstractSurface at the specified point as an RGB object.
	 */
	public RGB getColorAt(Vector p) { return this.getColorAt(p, true); }
	
	/**
	 * @return  The color of this AbstractSurface at the specified point as an RGB object.
	 */
	public RGB getColorAt(Vector point, boolean transform) {
	    if (transform) point = this.completeTransform.getInverse().transformAsLocation(point);
	    
	    RGB colorAt = new RGB(0.0, 0.0, 0.0);
	    
	    if (textures.length > 0) {
	        for (int i = 0; i < this.textures.length; i++) {
	            colorAt.addTo(this.textures[i].getColorAt(point));
	        }
	        
	        colorAt.multiplyBy(this.color);
	    } else {
	        colorAt = (RGB)this.color.clone();
	    }
	    
	    if (this.parent != null)
	        colorAt.multiplyBy(this.parent.getColorAt(point));
		
		return colorAt;
	}
}
