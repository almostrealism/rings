package com.almostrealism.geometry;

import com.almostrealism.raytracer.primitives.Mesh;

import net.sf.j3d.run.Settings;
import net.sf.j3d.util.TransformMatrix;
import net.sf.j3d.util.Vector;

/**
 * Provides a simple mechanism to keep track of tranformation
 * parameters that are used with most types of geometry. This
 * type is convenient for extending or encapsulating.
 * 
 * @author  Michael Murray
 */
public class BasicGeometry implements Positioned, Oriented, Scaled, Triangulatable {
	private Vector location;
	private double size;
	
	private double scaleX, scaleY, scaleZ;
	private double rotateX, rotateY, rotateZ;

	private TransformMatrix transforms[];
	private TransformMatrix transform, completeTransform;
	private boolean transformCurrent;

	public BasicGeometry() {
		this.setTransforms(new TransformMatrix[0]);
		
		this.setLocation(new Vector(0.0, 0.0, 0.0));
		this.setSize(1.0);
		
		this.setScaleCoefficients(1.0, 1.0, 1.0);
		this.setRotationCoefficients(0.0, 0.0, 0.0);
	}
	
	/**
	 * Sets the location of this BasicGeometry to the specified Vector object.
	 * This method calls calulateTransform() after it is completed.
	 */
	public void setLocation(Vector location) {
		this.location = location;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the size of this BasicGeometry to the specified double value.
	 */
	public void setSize(double size) {
		this.size = size;
		this.transformCurrent = false;
		// this.calculateTransform();
	}
	
	/**
	 * Sets the values used to scale this BasicGeometry on the x, y, and z axes when it is rendered to the specified double values.
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
	 * Sets the angle measurements (in radians) used to rotate this BasicGeometry about the x, y, and z axes when it is rendered
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
	 * Returns the location of this BasicGeometry as a Vector object.
	 */
	public Vector getLocation() { return this.location; }
	
	/**
	 * Returns the size of this BasicGeometry as a double value.
	 */
	public double getSize() { return this.size; }
	
	/**
	 * Returns an array of double values containing the values used to scale this BasicGeometry
	 * on the x, y, and z axes when it is rendered.
	 */
	public double[] getScaleCoefficients() {
		double scale[] = {this.scaleX, this.scaleY, this.scaleZ};
		
		return scale;
	}
	
	/**
	 * Returns an array of double values containing the angle measurements (in radians) used to rotate
	 * this BasicGeometry about the x, y, and z axes when it is rendered as an array of double values.
	 */
	public double[] getRotationCoefficients() {
		double rotation[] = {this.rotateX, this.rotateY, this.rotateZ};
		
		return rotation;
	}
	
	/**
	 * Returns the TransformMatrix object used to transform this BasicGeometry when it is rendered.
	 * This TransformMatrix does not represents the transformations due to fixed scaling and rotation.
	 */
	public TransformMatrix getTransform() { return this.getTransform(false); }
	
	/**
	 * Returns the TransformMatrix object used to transform this BasicGeometry when it is rendered.
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

	/** Delegates to {@link #setLocation(Vector)} */
	@Override
	public void setPosition(float x, float y, float z) {
		setLocation(new Vector(x, y, z));
	}
	
	@Override
	public float[] getPosition() { return getLocation().toFloat(); }
	
	@Override
	public void setOrientation(float angle, float x, float y, float z) {
		throw new RuntimeException("Conversion from angle + xyx to anglex, angley, anglez not implemented");
	}
	
	@Override
	public float[] getOrientation() {
		throw new RuntimeException("Conversion from anglex, angley, anglez to angle + xyx not implemented");
	}
	
	/** Delegates to {@link #setScaleCoefficients(double, double, double)} */
	@Override
	public void setScale(float x, float y, float z) {
		setScaleCoefficients(x, y, z);
	}
	
	@Override
	public float[] getScale() { return new float[] { (float) scaleX, (float) scaleY, (float) scaleZ }; }
	
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
	 * Sets the TransformMatrix objects used to transform this BasicGeometry when it is rendered
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
	 * Applies the transformation represented by the specified TransformMatrix to this BasicGeometry when it is rendered.
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
	 * Calculates the complete transformation that will be applied to this BasicGeometry when it is rendered
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
	 * @return  A Mesh object with location, size, scale coefficients,
	 *          rotation coefficients, and transformations as this
	 *          {@link BasicGeometry}.
	 */
	public Mesh triangulate() {
		Mesh m = new Mesh();
		
		m.setLocation(this.getLocation());
		m.setSize(this.getSize());
		m.setScaleCoefficients(this.scaleX, this.scaleY, this.scaleZ);
		m.setRotationCoefficients(this.rotateX, this.rotateY, this.rotateZ);
		m.setTransforms(this.getTransforms());
		
		return m;
	}
}