package com.almostrealism.photonfield.geometry;

import com.almostrealism.photonfield.util.VectorMath;

public class Elipse {
	private static double center[], major[], minor[];
	
	public static void loadConicSection(double x[], double p[], double n[], double theta) {
		double l[] = VectorMath.subtract(x, p);
		double nl = VectorMath.dot(n, l);
		double m[] = VectorMath.addMultiple(l, n, -nl);
		VectorMath.multiply(m, 1.0 / VectorMath.length(m));
		
		double ll = VectorMath.length(l);
		double lls = ll * Math.sin(theta);
		double nnl = nl / ll;
		double cnnl = Math.acos(nnl);
		
		double c1 = lls / Math.sin(0.5 * Math.PI - theta + cnnl);
		double c2 = lls / Math.sin(0.5 * Math.PI - theta - cnnl);
		
		Elipse.center = VectorMath.addMultiple(VectorMath.clone(p), m, c1 - c2);
		Elipse.major = VectorMath.multiply(m, 0.5 * (c1 + c2));
		
		double nm[] = VectorMath.cross(n, Elipse.major);
		VectorMath.multiply(nm, 1.0 / VectorMath.length(nm));
		double nml = VectorMath.dot(nm, l) / ll;
		double cnml = Math.acos(nml);
		
		Elipse.minor = VectorMath.multiply(nm, lls / Math.sin(Math.PI - theta - cnml));
	}
	
	public static double[] getSample() {
		double x = 1.0;
		double y = 1.0;
		
		while (x * x + y * y > 1.0) {
			x = Math.random();
			y = Math.random();
		}
		
		double a[] = VectorMath.multiply(Elipse.major, x, true);
		VectorMath.addMultiple(a, Elipse.minor, y);
		
		return a;
	}
}
