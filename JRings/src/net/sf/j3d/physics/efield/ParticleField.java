/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */

package net.sf.j3d.physics.efield;

import java.util.Iterator;
import java.util.Set;

import net.sf.j3d.util.Vector;


public class ParticleField {
	public static final double mass = 1.0;
	public static final double delta = Math.pow(10.0, 10.0);
	private static final double h = 1.0; // DX * E
	private static final double c = 3.0 * Math.pow(10.0, 9.0);
	private static final double deltat = Math.pow(10.0, -10.0);
	private static final double deltact = c * deltat;
	private static final double deltax = 1.0 / delta;
	private static final double dmass = mass / delta;
	private static final double dmu = (ParticleField.dmass * EnergyField.dmass) /
									(ParticleField.dmass + EnergyField.dmass);
	private static final double dphi = (dmu * dmass * dmass) /
								(8 * h * h * EnergyField.perm * EnergyField.perm);
	
	private long t;
	private double p[][];
	private boolean ejected[];
	private long emit[];
	private EnergyField field;
	
	private Set photons;
	
	public ParticleField(EnergyField e, int tot) {
		this.p = new double[tot][6];
		this.ejected = new boolean[tot];
		this.emit = new long[tot];
		this.field = e;
	}
	
	public void addPhoton(double energy, double x, double y, double z,
							double dx, double dy, double dz) {
		for (int i = 0; i < ParticleField.delta; i++) {
			this.photons.add(new double[] {
							(x + (2 * Math.random() - 1.0) * dx),
							(y + (2 * Math.random() - 1.0) * dy),
							(z + (2 * Math.random() - 1.0) * dz),
							dx, dy, dz, energy / delta});
		}
	}
	
	protected void addPhoton(double photon[]) { this.photons.add(photon); }
	
	public void iterate() {
		for (int i = 0; i < this.p.length; i++) {
			if (!this.ejected[i] && emit[i] == this.t)
				this.photons.add(this.emit(i));
		}
		
		Iterator itr = this.photons.iterator();
		double photon[];
		
		while (itr.hasNext()) {
			photon = (double[]) itr.next();
			
			if (this.intersect(photon)) {
				itr.remove();
			} else {
				photon[0] += photon[3] * ParticleField.deltact;
				photon[1] += photon[4] * ParticleField.deltact;
				photon[2] += photon[5] * ParticleField.deltact;
			}
		}
	}
	
	public boolean intersect(double photon[]) {
		int index = this.getClosestParticle(photon[0], photon[1], photon[2]);
		double dx = this.p[index][0] - photon[0];
		double dy = this.p[index][1] - photon[1];
		double dz = this.p[index][2] - photon[2];
		double d = dx * dx + dy * dy + dz * dz;
		double u = (ParticleField.h / photon[6]) + deltax;
		
		if (d < (u * u)) {
			this.absorb(index, photon[6], photon[3], photon[4], photon[5]);
			return true;
		} else {
			return false;
		}
	}
	
	public double absorb(int index, double energy, double x, double y, double z) {
		double d = this.getDistance(index, x, y, z, energy);
		
		this.p[index][3] += d * x;
		this.p[index][4] += d * y;
		this.p[index][5] += d * z;
		
		if (this.getEnergy(index) > ParticleField.dphi) {
			this.ejected[index] = true;
		} else {
			double dd = this.p[index][3] * this.p[index][3] +
						this.p[index][4] * this.p[index][4] +
						this.p[index][5] * this.p[index][5];
			this.emit[index] = this.t + (long) Math.sqrt(
								dd * ParticleField.dmass / this.getEnergy(index));
		}
		
		return d;
	}
	
	public double[] emit(int index) {
		double photon[] = new double[7];
		photon[0] = this.p[index][0];
		photon[1] = this.p[index][1];
		photon[2] = this.p[index][2];
		
		Vector v = new Vector(this.p[index][3],
					this.p[index][4],
					this.p[index][5]).crossProduct(Vector.uniformSphericalRandom());
		double l = v.length();
		photon[3] = v.getX() / l;
		photon[4] = v.getY() / l;
		photon[5] = v.getZ() / l;
		
		photon[6] = this.getEnergy(index);
		
		this.p[index][3] = 0.0;
		this.p[index][4] = 0.0;
		this.p[index][5] = 0.0;
		
		return photon;
	}
	
	public double[] getParticle(int index) { return this.p[index]; }
	
	public double getEnergy(int index) {
		return 0.5 * ParticleField.dmass *
			this.field.getK(this.p[index][0], this.p[index][1], this.p[index][2],
							this.p[index][3], this.p[index][4], this.p[index][5]) *
							(this.p[index][3] * this.p[index][3] +
							this.p[index][4] * this.p[index][4] +
							this.p[index][5] * this.p[index][5]);
	}
	
	public double getDistance(int index, double x, double y, double z, double energy) {
		double b = 2.0 * this.getDisplacement(index, x, y, z);
		double k = this.field.getK(this.p[index][0], this.p[index][1], this.p[index][2],
									this.p[index][3], this.p[index][4], this.p[index][5],
									x, y, z);
		double disc = Math.sqrt(b * b + 8.0 * energy / (ParticleField.dmass * k));
		return (disc - b) / 2;
	}
	
	public double getDisplacement(int index, double x, double y, double z) {
		double d = this.p[index][3] * x +
					this.p[index][4] * y +
					this.p[index][5] * z;
		
		return d;
	}
	
	public int getClosestParticle(double x, double y, double z) {
		double min = -1.0, d, dx, dy, dz;
		int index = -1;
		
		i: for (int i = 0; i < this.p.length; i++) {
			if (this.ejected[i]) continue i;
			
			dx = (this.p[i][0] - x);
			dy = (this.p[i][1] - y);
			dz = (this.p[i][2] - z);
			
			d = dx * dx + dy * dy + dz * dz;
			
			if (d < min || min < 0) {
				min = d;
				index = i;
			}
		}
		
		return index;
	}
}
