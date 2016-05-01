package com.almostrealism.photonfield.light;

import com.almostrealism.photonfield.util.Locatable;
import com.almostrealism.photonfield.util.VectorMath;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.lighting.PointLight;
import com.almostrealism.raytracer.lighting.SurfaceLight;

import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

public class PlanarLight extends LightBulb implements SurfaceLight, Locatable {
	private double w, h;
	private double normal[], up[], across[];
	private boolean lightProp = false;
	private double align = 0.0;
	
	private Vector location;
	
	/**
	 * @param w  The width of the planar light (usually measured in micrometers).
	 */
	public void setWidth(double w) { this.w = w; }
	
	/**
	 * Returns the width of the planar light (usually measured in micrometers).
	 */
	public double getWidth() { return this.w; }
	
	/**
	 * @param h  The height of the planar light (usually measured in micrometers).
	 */
	public void setHeight(double h) { this.h = h; }
	
	/**
	 * Returns the height of the planar light (usually measured in micrometers).
	 */
	public double getHeight() { return this.h; }
	
	/**
	 * @param p  {x, y, z} - The vector normal to the absorption plane.
	 */
	public void setSurfaceNormal(double p[]) { this.normal = p;	this.across = null; }
	
	/**
	 * @return  {x, y, z} - The vector normal to the absorption plane.
	 */
	public double[] getSurfaceNormal() { return this.normal; }
	
	/**
	 * @param p  {x, y, z} - The vector pointing upwards across the surface of this
	 *           absorption plane. This vector must be orthagonal to the surface normal.
	 */
	public void setOrientation(double p[]) { this.up = p; this.across = null; }
	
	/**
	 * @return  {x, y, z} - The vector pointing upwards across the surface of this
	 *           absorption plane.
	 */
	public double[] getOrientation() { return this.up; }
	
	/**
	 * @param t  true sets the direction of light to be in a uniform semisphere
	 * 			 normal to the plane. false sets the direction to be normal to
	 *           the plane.
	 *           
	 */
	public void setLightPropagation(boolean t) { this.lightProp = t; }
	
	public boolean getLightPropagation() { return this.lightProp; }
	
	public double[] emit() {
		super.last += super.delta;
		
		if (!this.lightProp)
			return VectorMath.clone(this.normal);
		
		double[] v = VectorMath.uniformSphericalRandom();
		if (VectorMath.dot(v, this.normal) < 0)
			VectorMath.multiply(v, -1.0);
		
		double[] r = VectorMath.normalize(VectorMath.addMultiple(v, this.normal, this.align));
		return r;
	}
	
	public double[] getEmitPosition() {
		if (this.across == null)
			this.across = VectorMath.cross(this.up, this.normal);
		
		double x[] = VectorMath.multiply(this.across, (Math.random() - 0.5) * this.w, true);
		return VectorMath.addMultiple(x, this.up, (Math.random() - 0.5) * this.h);
	}
	
	public void setIntensity(double intensity) { }
	public double getIntensity() { return 1.0; }
	
	public RGB getColorAt(Vector point) { return null; }
	public void setColor(RGB color) { }
	public RGB getColor() { return null; }
	
	public void setLocation(Vector l) { this.location = l; }

	public Light[] getSamples(int total) {
		Light l[] = new Light[total];
		
		double in = 1.0 / total;
		
		for (int i = 0; i < total; i++) {
			double x[] = this.getSpatialCoords(new double[] {Math.random(), Math.random()});
			Vector p = new Vector(x[0], x[1], x[2]);
			p.addTo(this.location);
			l[i] = new PointLight(p, in, new RGB(1.0, 1.0, 1.0));
		}
		
		return l;
	}

	public Light[] getSamples() { return this.getSamples(20); }
}
