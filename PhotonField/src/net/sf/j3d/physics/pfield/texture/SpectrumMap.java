/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.texture;

import java.io.IOException;

import net.sf.j3d.physics.pfield.util.ProbabilityDistribution;
import net.sf.j3d.util.graphics.RGB;

/**
 * A SpectrumMap instance combines a ProbabilityDistribution and an IntensityMap.
 * This functionality is provided through the getSample method which accepts a
 * position vector (u, v, w). The SpectrumMap object will first use the IntensityMap
 * instance that it stores internally to calculate an intensity value for the point
 * and then it will use the intensity value returned to locate a position on the
 * ProbabilityDistribution. The SpectrumMap will not function properly unless an
 * IntensityMap instance is specified. However, if no ProbabilityDistribution is
 * specified, the SpectrumMap class extends ProbabilityDistribution and will use the
 * methods of the super class if the internal instance is null.
 * 
 * @author  Mike Murray
 */
public class SpectrumMap extends ProbabilityDistribution implements IntensityMap {
	private IntensityMap map;
	private ProbabilityDistribution spectra;
	
	public SpectrumMap() { this(null, null); }
	public SpectrumMap(IntensityMap m) { this(m, null); }
	
	public SpectrumMap(IntensityMap m, ProbabilityDistribution spec) {
		this.map = m;
		this.spectra = spec;
	}
	
	public void setIntensityMap(IntensityMap m) { this.map = m; }
	public IntensityMap getIntensityMap() { return this.map; }
	public void setProbabilityDistribution(ProbabilityDistribution d) { this.spectra = d; }
	public ProbabilityDistribution getProbabilityDistribution() {
		if (this.spectra == null)
			return this;
		else
			return this.spectra;
	}
	
	public double getIntensity(double u, double v, double w) {
		return this.map.getIntensity(u, v, w);
	}
	
	public double getSample(double u, double v, double w) {
		return this.getSample(this.getIntensity(u, v, w));
	}
	
	public double getSample(double r) {
		if (this.spectra != null) {
			return this.spectra.getSample(r);
		} else {
			return super.getSample(r);
		}
	}
	
	public void addRange(double start, double end, double p) {
		if (this.spectra != null) {
			this.spectra.addRange(start, end, p);
		} else {
			super.addRange(start, end, p);
		}
	}
	
	protected double[][] getRanges() {
		if (this.spectra != null) {
			throw new RuntimeException(new IllegalAccessException("getRanges is protected."));
		} else {
			return super.getRanges();
		}
	}
	
	public void setRangeProbability(int index, double p) {
		if (this.spectra != null) {
			this.spectra.setRangeProbability(index, p);
		} else {
			super.setRangeProbability(index, p);
		}
	}
	
	public double integrate(double limit) {
		if (this.spectra != null) {
			return this.spectra.integrate(limit);
		} else {
			return super.integrate(limit);
		}
	}
	
	public RGB getIntegrated() {
		if (this.spectra != null) {
			return this.spectra.getIntegrated();
		} else {
			return super.getIntegrated();
		}
	}
	
	public double getProbability(double x) {
		if (this.spectra != null) {
			return this.spectra.getProbability(x);
		} else {
			return super.getProbability(x);
		}
	}
	
	public int getNodeCount() {
		if (this.spectra != null) {
			return this.spectra.getNodeCount();
		} else {
			return super.getNodeCount();
		}
	}
	
	public boolean contains(double start, double end) {
		if (this.spectra != null) {
			return this.spectra.contains(start, end);
		} else {
			return super.contains(start, end);
		}
	}
	
	public void loadFromFile(String file, String div) throws IOException {
		if (this.spectra != null) {
			this.spectra.loadFromFile(file, div);
		} else {
			super.loadFromFile(file, div);
		}
	}
}
