/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package net.sf.j3d.physics.pfield;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.j3d.physics.pfield.util.PhysicalConstants;
import net.sf.j3d.physics.pfield.util.VectorMath;
import net.sf.j3d.util.Graph;


// TODO  Consider creating a custom list for photon set (tick creates many many double[][]).
public class DefaultPhotonField implements PhotonField {
	public static double verbose = 0.0000000;
	public static boolean checkLength = true;
	public static double ep = Math.pow(10.0, -10.0);
	
	private Clock clock;
	private Set photons;
	private Absorber absorber;
	private long delta = 1;
	private double lifetime = Double.MAX_VALUE - 1.0;
	private boolean trace = true;
	
	private Graph sizeGraph, timeGraph;
	private long tot = 0, log = 500;
	private boolean first;
	private long start;
	private String file;
	
	public DefaultPhotonField() {
		this.photons = new HashSet();
		this.sizeGraph = new Graph(500000);
		this.sizeGraph.setScale(200);
		this.sizeGraph.setDivisions(500);
		
		this.timeGraph = new Graph(500000);
		this.sizeGraph.setScale(200);
		this.sizeGraph.setDivisions(500);
	}
	
	public void addPhoton(double[] x, double[] p, double energy) {
		this.photons.add(new double[][] {x, p, {energy}, {0.0}});
	}

	public void setAbsorber(Absorber absorber) { this.absorber = absorber; }
	public Absorber getAbsorber() { return this.absorber; }
	public void setGranularity(long delta) { this.delta = delta; }
	public long getGranularity() { return this.delta; }
	
	public void setLogFrequency(long ticks) { this.log = ticks; }
	public long getLogFrequency() { return this.log; }
	
	public void setLogFile(String file) { this.file = file; }
	public String getLogFile() { return this.file; }
	
	public Graph getSizeGraph() { return this.sizeGraph; }
	public Graph getTimeGraph() { return this.timeGraph; }
	
	public void setRayTracing(boolean trace) { this.trace = trace; }
	public boolean getRayTracing() { return this.trace; }
	
	public double getEnergy(double x[], double radius) {
		Iterator itr = this.photons.iterator();
		double e = 0.0;
		
		while (itr.hasNext()) {
			double p[][] = (double[][]) itr.next();
			
			if (VectorMath.length(VectorMath.subtract(p[0], x)) < radius)
				e += p[2][0];
		}
		
		return e;
	}
	
	public long getSize() { return this.photons.size(); }
	
	public void setMaxLifetime(double l) { this.lifetime = l * PhysicalConstants.C; }
	public double getMaxLifetime() { return this.lifetime / PhysicalConstants.C; }
	
	public int removePhotons(double[] x, double radius) {
		// TODO  Implement removePhotons method.
		return 0;
	}

	public void setClock(Clock c) {
		this.clock = c;
		if (this.absorber != null) this.absorber.setClock(this.clock);
	}
	
	public Clock getClock() { return this.clock; }
	
	public void tick(double s) {
		if (this.first) {
			this.start = System.currentTimeMillis();
			this.first = false;
		}
		
		double r = 1.0;
		if (this.verbose > 0.0) r = Math.random();
		
		if (r < this.verbose)
			System.out.println("Photons: " + this.photons.size());
		
		Iterator itr = this.photons.iterator();
		
		boolean o = true;
		
		double delta = PhysicalConstants.C * s;
		
		i: while (itr.hasNext()) {
			double p[][] = (double[][]) itr.next();
			
			p[0][0] += p[1][0] * delta;
			p[0][1] += p[1][1] * delta;
			p[0][2] += p[1][2] * delta;
			
			if (p[3][0] > 0.0) p[3][0] -= delta;
			
			if (trace && p[3][0] > this.lifetime) {
				itr.remove();
				continue i;
			}
			
			if (o && r < this.verbose) {
				System.out.println("PhotonMoved: " + VectorMath.length(
									new double[] {
										p[1][0] * delta,
										p[1][1] * delta,
										p[1][2] * delta
									}));
				o = false;
			}
			
			double dist = 0.0;
			
			if (this.trace && p[3][0] < 0.0 && this.absorber instanceof AbsorberSet)
				dist = ((AbsorberSet) this.absorber).getDistance(p[0], p[1]);
			
			if (r < this.verbose)
				System.out.println("DefaultPhotonField: Distance = " + dist);
			
			p[3][0] = dist;
			
			if (this.absorber instanceof AbsorberSet &&
				VectorMath.length(p[0]) >
				((AbsorberSet)this.absorber).getMaxProximity()) {
					itr.remove();
			} else if ((!trace || p[3][0] <= delta) &&
						this.absorber.absorb(p[0], p[1], p[2][0])) {
				itr.remove();
			}
		}
		
		double next;
		
		w: while ((next = this.absorber.getNextEmit()) < s) {
			if (r < this.verbose)
				System.out.println("Next Emit: " + next);
			
			double d = this.absorber.getEmitEnergy();
			double x[] = this.absorber.getEmitPosition();
			double y[] = this.absorber.emit();
			
			if (x == null) {
				System.out.println("DefaultPhotonField: " + this.absorber +
									" returned null emit position.");
				continue w;
			}
			
			if (y == null) {
				System.out.println("DefaultPhotonField: " + this.absorber +
									" returned null emit direction.");
				continue w;
			}
			
			if (this.checkLength) {
				double l = VectorMath.length(y);
				if (l > 1.0 + this.ep || l < 1.0 - this.ep)
					System.out.println("DefaultPhotonField: Length was " +
										VectorMath.length(y));
			}
			
			if (this.trace)
				this.photons.add(new double[][] {x, y, {d}, {-1.0}});
			else
				this.photons.add(new double[][] {x, y, {d}, {0.0}});
		}
		
		this.tot++;
		
		if (this.log > 0 && this.tot % this.log == 0) {
			double rate = (System.currentTimeMillis() - this.start) /
							(60 * 60000 * this.clock.getTime());
			this.timeGraph.addEntry(rate);
			
			this.sizeGraph.addEntry(this.getSize());
			
			if (this.file != null) {
				StringBuffer b = new StringBuffer();
				this.sizeGraph.print(b);
				
				try (BufferedWriter out = new BufferedWriter(new FileWriter(this.file))) {
					out.write(b.toString());
				} catch (IOException e) {
					System.out.println("DefaultPhotonField: " + e.getMessage());
				}
			}
		}
	}
}
