/*
 * Copyright (C) 2007  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield.util.buffers;

/**
 * @author  Mike Murray
 */
public class AveragedVectorMap2D96Bit implements AveragedVectorMap2D {
	private static double shortmax = Short.MAX_VALUE;
	
	private double vector[];
	private int w, h;
	private int fcount[][], bcount[][];
	private int fxBuf[], fyBuf[], fzBuf[];
	private int bxBuf[], byBuf[], bzBuf[];
	
	public AveragedVectorMap2D96Bit(int w, int h) {
		this.fcount = new int[w][h];
		this.bcount = new int[w][h];
		this.w = w;
		this.h = h;
		int tot = w * h;
		this.fxBuf = new int[tot];
		this.fyBuf = new int[tot];
		this.fzBuf = new int[tot];
		this.bxBuf = new int[tot];
		this.byBuf = new int[tot];
		this.bzBuf = new int[tot];
	}
	
	public void setVector(double x, double y, double z) {
		this.vector = new double[] {x, y, z};
		this.fxBuf = null;
		this.fyBuf = null;
		this.fzBuf = null;
		this.bxBuf = null;
		this.byBuf = null;
		this.bzBuf = null;
	}
	
	public void addVector(double u, double v, double x, double y, double z, boolean front) {
		if (u >= 1.0 || v >= 1.0 || u < 0.0 || v < 0.0) {
			System.out.println("AveragedVectorMap2D96Bit: Invalid UV " + u + ", " + v);
			return;
		}
		
		int px = (int) (u * w);
		int py = (int) (v * h);
		int t = px + py * w;
		
		int count[][] = this.fcount;
		if (!front) count = this.bcount;
		
		count[px][py]++;
		if (count[px][py] >= Short.MAX_VALUE)
			System.out.print("AveragedVectorMap2D96Bit: Overflow.");
		
		if (this.vector != null) return;
		
		if (front) {
			this.fxBuf[t] += x * Short.MAX_VALUE;
			this.fyBuf[t] += y * Short.MAX_VALUE;
			this.fzBuf[t] += z * Short.MAX_VALUE;
		} else {
			this.fxBuf[t] += x * Short.MAX_VALUE;
			this.fyBuf[t] += y * Short.MAX_VALUE;
			this.fzBuf[t] += z * Short.MAX_VALUE;
		}
	}
	
	public double[] getVector(double u, double v, boolean front) {
		if (this.vector != null) return this.vector;
		
		if (u >= 1.0 || v >= 1.0 || u < 0.0 || v < 0.0) {
			System.out.println("AveragedVectorMap2D96Bit: Invalid UV " + u + ", " + v);
			return new double[3];
		}
		
		int px = (int) (u * w);
		int py = (int) (v * h);
		int t = px + py * w;
		
		if (front) {
			double xyz[] = {(this.fxBuf[t] / shortmax) / this.fcount[px][py],
							(this.fyBuf[t] / shortmax) / this.fcount[px][py],
							(this.fzBuf[t] / shortmax) / this.fcount[px][py]};
			return xyz;
		} else {

			double xyz[] = {(this.bxBuf[t] / shortmax) / this.bcount[px][py],
							(this.byBuf[t] / shortmax) / this.bcount[px][py],
							(this.bzBuf[t] / shortmax) / this.bcount[px][py]};
			return xyz;
		}
	}
	
	public int getSampleCount(double u, double v, boolean front) {
		if (u >= 1.0 || v >= 1.0 || u < 0.0 || v < 0.0) {
			System.out.println("AveragedVectorMap2D96Bit: Invalid UV " + u + ", " + v);
			return 0;
		}
		
		int px = (int) (u * w);
		int py = (int) (v * h);
		
		if (front)
			return this.fcount[px][py];
		else
			return this.bcount[px][py];
	}
}
