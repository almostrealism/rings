/*
 * Copyright (C) 2006  Mike Murray
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 *  
 */

package net.sf.j3d.physics.efield;

public class EnergyField {
	private static final double mass = 1.0;
	public static final double dmass = mass / ParticleField.delta;
	public static final double perm = 4 * Math.PI;
	private static final double k = 1.0 / perm;
	
	private double pos[][];
	
	public EnergyField(double positive[][]) {
		this.pos = positive;
	}
	
	public double getK(double ix, double iy, double iz,
						double x, double y, double z,
						double dx, double dy, double dz) {
		return this.getK(ix + x, iy + y, iz + z, dx, dy, dz);
	}
	
	public double getK(double nx, double ny, double nz,
						double dx, double dy, double dz) {
		double ilen2 = this.getSquaredDistance(nx, ny, nz);
		double nlen2 = this.getSquaredDistance(nx + dx, ny + dy, nz + dz);
		double ilen = Math.sqrt(ilen2);
		double nlen = Math.sqrt(nlen2);
		double dlen = nlen - ilen;
		
		double p = -2.0 * EnergyField.k * (dlen);
		double q = ilen * nlen * nlen2 +
					nlen * ilen * ilen2 -
					2.0 * ilen2 * nlen2;
		
		return p / q;
	}
	
	private double getSquaredDistance(double x, double y, double z) {
		double dx = 0.0, dy = 0.0, dz = 0.0;
		
		for (int i = 0; i < this.pos.length; i++) {
			dx += x - this.pos[i][0];
			dy += y - this.pos[i][1];
			dz += z - this.pos[i][2];
		}
		
		return dx * dx + dy * dy + dz * dz;
	}
	
	public double getPotential(double x, double y, double z) {
		double px = 0.0, py = 0.0, pz = 0.0;
		double dx = 0.0, dy = 0.0, dz = 0.0;
		
		for (int i = 0; i < this.pos.length; i++) {
			dx = x - this.pos[i][0];
			dy = y - this.pos[i][1];
			dz = z - this.pos[i][2];
			
			px += dx;
			py += dy;
			pz += dz;
		}
		
		double p = EnergyField.mass / (EnergyField.perm * Math.sqrt(px * px + py * py + pz * pz));
		return p;
	}
	
	public double getEnergy(double x, double y, double z,
							double dx, double dy, double dz,
							double mass) {
		return (this.getPotential(x + dx, y + dy, z + dz) -
			this.getPotential(x, y, z)) * mass;
	}
}
