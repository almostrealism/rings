package com.almostrealism.raytracer.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.text.NumberFormatter;

import com.almostrealism.lighting.Light;
import com.almostrealism.lighting.PointLight;
import com.almostrealism.projection.PinholeCamera;
import org.almostrealism.algebra.Vector;
import org.almostrealism.color.RGB;
import org.almostrealism.texture.Animation;

import com.almostrealism.projection.OrthographicCamera;
import com.almostrealism.rayshade.DiffuseShader;
import com.almostrealism.rayshade.ShaderSet;
import com.almostrealism.raytracer.engine.RayMarchingEngine;
import com.almostrealism.raytracer.engine.RayTracedScene;
import com.almostrealism.raytracer.engine.RenderParameters;
import com.almostrealism.raytracer.primitives.Sphere;
import org.almostrealism.texture.ImageCanvas;

public class RayMarchingTest {
	public static final boolean enableAnimation = false;

	public static double minRadius2 = 0.25;

	public static double fixedRadius2 = 1.0;
	public static double foldingLimit = 1.0;
	public static int Iterations = 50;
//	public static double POWER = 9.53;
	public static double POWER = 7.80;
	public static Vector Offset = new Vector(1.0, 1.0, 1.0);
	
	private static NumberFormatter format = new NumberFormatter(NumberFormat.getPercentInstance());
	
	public static void main(String args[]) {
		ShaderSet s = new ShaderSet();
		s.add(DiffuseShader.defaultDiffuseShader);
		
//		RayMarchingEngine mandel = new RayMarchingEngine((ray) -> {
//			Vector z = ray.getOrigin();
//			double dr = 1.0;
//			double r = 0.0;
//
//			int steps;
//
//			for (steps = 0; steps < Iterations; steps++) {
//				r = z.length();
//
//				double theta = Math.acos(z.getZ() / r) ;
//				double phi = Math.atan2(z.getY() , z.getX()) ;
//				dr = Math.pow(r, POWER - 1.0) * POWER * dr + 1.0;
//
//				double zr = Math.pow(r, POWER);
//				theta = theta * POWER ;
//				phi = phi * POWER ;
//
//				z = new Vector(Math.sin(theta) * Math.cos(phi),
//									Math.sin(phi) *  Math.sin(theta),
//									Math.cos(theta)).multiply(zr);
//				z = z.add(ray.getOrigin());
//			}
//
//			return 0.5 * Math.log(r) * r / dr;
//		}, s);
		
		RayMarchingEngine mandel = new RayMarchingEngine(new RenderParameters(),
										new Sphere(new Vector(0.0, 0.0, 0.0), 1.0,
													new RGB(0.8, 0.8, 0.8)),
								new Light[] { new PointLight(new Vector(10.0, 10.0, -10.0), 0.8, new RGB(0.8, 0.9, 0.7))}, s);

		OrthographicCamera c = new OrthographicCamera(new Vector(0.0, 0.0, 10.0),
														new Vector(0.0, 0.0, -1.0),
														new Vector(0.0, 1.0, 0.0));

//		PinholeCamera c = new PinholeCamera(new Vector(0.0, 0.0, -1.0),
//											new Vector(0.0, 10.0, 1.0),
//											new Vector(0.0, 1.0, 0.0));

		RenderParameters params = new RenderParameters();
		params.width = (int) (400 * c.getProjectionWidth());
		params.height = (int) (400 * c.getProjectionHeight());
		params.dx = 400;
		params.dy = 400;

		if (enableAnimation) {
			Animation animation = new Animation(null) {
				@Override
				public String next() {
					POWER = POWER + 0.01;

					this.setImage(new RayTracedScene(mandel, c).realize(params));

					try {
						return "marching" + format.valueToString(POWER) + ".jpg";
					} catch (ParseException e) {
						e.printStackTrace();
						return null;
					}
				}
			};

			animation.render().start();
		} else {
			try {
				ImageCanvas.encodeImageFile(new RayTracedScene(mandel, c).realize(params).evaluate(null),
						new File("test.jpeg"),
						ImageCanvas.JPEGEncoding);
			} catch (FileNotFoundException fnf) {
				System.out.println("ERROR: Output file not found");
			} catch (IOException ioe) {
				System.out.println("IO ERROR");
			}
		}
	}
	
	// simply scale the dual vectors
	static void sphereFold(Vector z, Vector[] dz) {
		double r2 = z.dotProduct(z);
		
		if (r2 < minRadius2) {
			double temp = (fixedRadius2 / minRadius2);
			z.multiplyBy(temp);
			dz[0].multiplyBy(temp);
			dz[1].multiplyBy(temp);
			dz[2].multiplyBy(temp);
		} else if (r2 < fixedRadius2) {
			double temp = (fixedRadius2 / r2);
	        dz[0] = (dz[0].subtract(z.multiply(2.0).multiply(z.dotProduct(dz[0]) / r2))).multiply(temp);
	        dz[1] = (dz[1].subtract(z.multiply(2.0).multiply(z.dotProduct(dz[1]) / r2))).multiply(temp);
	        dz[2] = (dz[2].subtract(z.multiply(2.0).multiply(z.dotProduct(dz[2]) / r2))).multiply(temp);
			z.multiplyBy(temp);
			dz[0].multiplyBy(temp);
			dz[1].multiplyBy(temp);
			dz[2].multiplyBy(temp);
		}
	}
	 
	// reverse signs for dual vectors when folding
	static void boxFold(Vector z, Vector[] dz) {
		if (Math.abs(z.getX()) > foldingLimit) {
			dz[0].setX(dz[0].getX() * -1.0);
			dz[1].setX(dz[1].getX() * -1.0);
			dz[2].setX(dz[2].getX() * -1.0);
		}
		
		if (Math.abs(z.getY()) > foldingLimit) {
			dz[0].setY(dz[0].getY() * -1.0);
			dz[1].setY(dz[1].getY() * -1.0);
			dz[2].setY(dz[2].getY() * -1.0);
		}
		
	    if (Math.abs(z.getZ()) > foldingLimit) {
			dz[0].setZ(dz[0].getZ() * -1.0);
			dz[1].setZ(dz[1].getZ() * -1.0);
			dz[2].setZ(dz[2].getZ() * -1.0);
	    	}
	    
		z = clamp(z, -foldingLimit, foldingLimit).multiply(2.0).subtract(z);
	}
	
	static Vector clamp(Vector x, double min, double max) {
	    if (x.length() < min)
	        return x.multiply(min / x.length());
	    else if (x.length() > max)
	        return x.multiply(max / x.length());
	    
	    return x;
	}
}
