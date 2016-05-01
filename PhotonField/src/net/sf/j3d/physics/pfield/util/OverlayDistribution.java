/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.util;

import java.io.IOException;
import java.lang.reflect.Method;

import net.sf.j3d.util.graphics.RGB;

/**
 * @author  Mike Murray
 */
public class OverlayDistribution extends ProbabilityDistribution {
	private ProbabilityDistribution children[];
	
	public OverlayDistribution() {
		this(new ProbabilityDistribution[0]);
	}
	
	public OverlayDistribution(ProbabilityDistribution children[]) {
		this.children = children;
	}
	
	public void addRange(double start, double end, double p) {
		if (this.children.length <= 0) return;
		this.children[0].addRange(start, end, p);
	}
	
	protected double[][] getRanges() {
		if (this.children.length <= 0) return new double[0][3];
		return this.children[0].getRanges();
	}
	
	public double integrate(double limit) {
		double v = 0.0;
		
		for (int i = 0; i < this.children.length; i++)
			v = v + this.children[i].integrate(limit);
		
		return v;
	}
	
	public double getSample(double r) {
		if (this.children.length <= 0) return 0.0;
		if (this.children.length == 1) return this.children[0].getSample(r);
		
		double d = this.children[0].getSample(r);
		double v = this.integrate(d);
		
		while (Math.abs(v - r) > 0.0001) {
			d = d * r / v;
			v = this.integrate(d);
		}
		
		return v;
	}
	
	public RGB getIntegrated() {
		RGB c = new RGB();
		
		for (int i = 0; i < this.children.length; i++)
			c.addTo(this.children[i].getIntegrated());
		
		return c;
	}
	
	public double getProbability(double x) {
		double p = 0.0;
		
		for (int i = 0; i < this.children.length; i++)
			p += this.children[i].getProbability(x);
		
		return p;
	}
	
	public boolean contains(double start, double end) {
		for (int i = 0; i < this.children.length; i++)
			if (this.children[i].contains(start, end)) return true;
		return false;
	}
	
	public void loadFromFile(String file, String div) throws IOException {
		this.children = new ProbabilityDistribution[1];
		this.children[0] = new ProbabilityDistribution();
		this.children[0].loadFromFile(file, div);
	}
	
	public static Method getOverlayMethod() {
		try {
			return OverlayDistribution.class.getMethod("createOverlayDistribution",
								new Class[] {ProbabilityDistribution[].class});
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static OverlayDistribution
				createOverlayDistribution(ProbabilityDistribution children[]) {
		return new OverlayDistribution(children);
	}
}
