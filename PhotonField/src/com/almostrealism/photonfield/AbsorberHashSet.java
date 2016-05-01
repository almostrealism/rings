/*
 * Copyright (C) 2006  Almost Realism Software Group
 *
 *  All rights reserved.
 *  This document may not be reused without
 *  express written permission from Mike Murray.
 */

package com.almostrealism.photonfield;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.almostrealism.photonfield.distribution.BRDF;
import com.almostrealism.photonfield.distribution.SphericalProbabilityDistribution;
import com.almostrealism.photonfield.geometry.Elipse;
import com.almostrealism.photonfield.network.PhotonFieldSceneLoader;
import com.almostrealism.photonfield.raytracer.AbsorberSetRayTracer;
import com.almostrealism.photonfield.raytracer.PinholeCameraAbsorber;
import com.almostrealism.photonfield.util.Colorable;
import com.almostrealism.photonfield.util.Fast;
import com.almostrealism.photonfield.util.Locatable;
import com.almostrealism.photonfield.util.PhysicalConstants;
import com.almostrealism.photonfield.util.Transparent;
import com.almostrealism.photonfield.util.VectorMath;
import com.almostrealism.photonfield.util.buffers.ArrayColorBuffer;
import com.almostrealism.photonfield.util.buffers.AveragedVectorMap2D;
import com.almostrealism.photonfield.util.buffers.AveragedVectorMap2D96Bit;
import com.almostrealism.photonfield.util.buffers.BufferListener;
import com.almostrealism.photonfield.util.buffers.ColorBuffer;
import com.almostrealism.photonfield.util.buffers.TriangularMeshColorBuffer;
import com.almostrealism.photonfield.util.color.Spectrum;
import com.almostrealism.raytracer.camera.Camera;
import com.almostrealism.raytracer.engine.Intersection;
import com.almostrealism.raytracer.engine.Ray;
import com.almostrealism.raytracer.engine.RayTracingEngine;
import com.almostrealism.raytracer.engine.Surface;
import com.almostrealism.raytracer.lighting.Light;
import com.almostrealism.raytracer.shaders.ShaderParameters;

import net.sf.j3d.util.Nameable;
import net.sf.j3d.util.Vector;
import net.sf.j3d.util.graphics.RGB;

/**
 * An AbsorberHashSet object is an implementation of AbsorberSet that uses a HashSet
 * to store the child absorbers.
 * 
 * @author Mike Murray
 */
public class AbsorberHashSet extends HashSet implements AbsorberSet, Surface, Colorable {
	private interface SetListener { public void noteUpdate(); }
	
	public static class StoredItem {
		public Absorber absorber;
		private Volume volume;
		public double position[];
		boolean checked, fast, highlight;
		
		SphericalProbabilityDistribution brdf;
		
		private BufferListener listener;
		private ColorBuffer buf;
		private AveragedVectorMap2D incidence, exitance;
		private int wh[];
		
		public StoredItem(Absorber a, double p[]) {
			this.absorber = a;
			this.position = p;
		}
		
		public void setBufferListener(BufferListener listener) { this.listener = listener; }
		
		public void setColorBufferSize(int w, int h, double m) {
			if (AbsorberHashSet.solidColorBuffer) {
				this.buf = new ArrayColorBuffer();
				((ArrayColorBuffer) this.buf).setColorBufferSize(1, 1, 1.0);
			} else if (AbsorberHashSet.triangularColorBuffer) {
				this.buf = new TriangularMeshColorBuffer();
			} else {
				this.buf = new ArrayColorBuffer();
				((ArrayColorBuffer) this.buf).setColorBufferSize(w, h, m);
			}
			
			this.incidence = new AveragedVectorMap2D96Bit(w, h);
			this.exitance = new AveragedVectorMap2D96Bit(w, h);
			// TODO Remove next line.
			((AveragedVectorMap2D96Bit) this.exitance).setVector(0, 0, 0);
			
			this.wh = new int[] {w, h};
		}
		
		public int[] getColorBufferDimensions() {
			if (this.buf instanceof ArrayColorBuffer)
				return ((ArrayColorBuffer) this.buf).getColorBufferDimensions();
			else if (this.wh != null)
				return this.wh;
			else
				return new int[2];
		}
		
		public double getColorBufferScale() {
			if (this.buf == null)
				return 0.0;
			else
				return this.buf.getScale();
		}
		
		public RGB getColorAt(double u, double v, boolean front, boolean direct) {
			return this.getColorAt(u, v, front, direct, null);
		}
		
		public RGB getColorAt(double u, double v, boolean front, boolean direct, Vector n) {
			RGB c = null;
			
			if (direct) {
				if (this.absorber instanceof Spectrum) {
					c = ((Spectrum) this.absorber).getSpectra().getIntegrated();
					
//					if (n != null) {
//						double vec[] = this.incidence.getVector(u, v, front);
//						double d = n.getX() * vec[0] + n.getY() * vec[1] + n.getZ() * vec[2];
//						c.multiplyBy(d);
//					}
				}
			} else {
				c = this.buf.getColorAt(u, v, front);
				c.multiplyBy(1.0 / this.exitance.getSampleCount(u, v, front));
			}
			
			// TODO Multiply by dot product of exitance and incidence.
			
			return c;
		}
		
		public void addColor(double u, double v, boolean front, RGB c) {
			this.buf.addColor(u, v, front, c);
			if (this.listener != null)
				this.listener.updateColorBuffer(u, v, this.getVolume(), this.buf, front);
		}
		
		public void addIncidence(double u, double v, double x, double y, double z, boolean front) {
			this.incidence.addVector(u, v, x, y, z, front);
			if (this.listener != null)
				this.listener.updateIncidenceBuffer(u, v, this.getVolume(), this.incidence, front);
		}
		public void addExitance(double u, double v, double x, double y, double z, boolean front) {
			this.exitance.addVector(u, v, x, y, z, front);
			if (this.listener != null)
				this.listener.updateExitanceBuffer(u, v, this.getVolume(), this.exitance, front);
		}
		
		public Volume getVolume() {
			if (this.volume == null)
				this.volume = AbsorberHashSet.getVolume(this.absorber);
			return this.volume;
		}
		
		public String toString() { return "StoredItem[" + this.absorber + "]"; }
	}
	
	public static final int DEFAULT_ORDER = 1;
	public static final int RANDOM_ORDER = 2;
	public static final int POPULAR_ORDER = 4;
	public static boolean solidColorBuffer = false, triangularColorBuffer = false;
	
	public int colorDepth = 48;
	
	private Clock clock;
	private PotentialMap map;
	private StoredItem emitter, rclosest, closest, lastAdded;
	private BufferListener listener;
	
	private Set sList;
	
	private RGB rgb = new RGB(this.colorDepth, 0.0, 0.0, 0.0);
	
	private int order = 1;
	private boolean fast = true;
	private double delay, e;
	private double spreadAngle;
	private int spreadCount;
	
	private double max = Double.MAX_VALUE, bound = Double.MAX_VALUE;
	private boolean cleared = false;
	
	private Iterator items;
	private Thread itemsUser;
	private boolean itemsNow, itemsEnabled = false;
	
	public AbsorberHashSet() {
		this.sList = new HashSet();
	}
	
	public int addAbsorber(Absorber a, double x[]) {
		return this.addAbsorber(a, x, a instanceof Fast);
	}
	
	public int addAbsorber(Absorber a, double x[], boolean fast) {
		a.setClock(this.clock);
		StoredItem item = new StoredItem(a, x);
		item.setBufferListener(this.listener);
		item.fast = fast;
		
		if (a instanceof BRDF) item.brdf = ((BRDF)a).getBRDF();
		
		if (this.add(item)) {
			this.lastAdded = item;
			this.notifySetListeners();
			return this.size();
		} else {
			return -1;
		}
	}

	public int removeAbsorbers(double x[], double radius) {
		Iterator itr = this.iterator();
		int tot = 0;
		
		while (itr.hasNext()) {
			StoredItem it = (StoredItem) itr.next();
			
			if (VectorMath.distance(x, it.position) <= radius) {
				this.remove(it);
				tot++;
			}
		}
		
		return tot;
	}

	public int removeAbsorber(Absorber a) {
		Iterator itr = this.iterator();
		
		while (itr.hasNext()) {
			StoredItem it = (StoredItem) itr.next();
			if (it.equals(a)) this.remove(it);
			this.notifySetListeners();
		}
		
		return this.size();
	}
	
	public void init() {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			
			if (n.absorber instanceof BRDF)
				n.brdf = ((BRDF)n.absorber).getBRDF();
			if (n.absorber instanceof AbsorberHashSet)
				((AbsorberHashSet)n.absorber).init();
		}
		
		this.itemsEnabled = true;
	}
	
	public void clearColorBuffers() {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			if (n.buf != null) n.buf.clear();
		}
	}
	
	public void storeColorBuffers(PhotonFieldSceneLoader loader) throws IOException {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			if (n.buf != null) this.storeColorBuffer(loader, n);
		}
	}
	
	public void storeColorBuffer(PhotonFieldSceneLoader loader, StoredItem it) throws IOException {
		String name = it.absorber.toString();
		if (it.absorber instanceof Nameable) name = ((Nameable)it.absorber).getName();
		if (it.buf != null) it.buf.store(loader, name);
	}
	
	public void loadColorBuffers(PhotonFieldSceneLoader loader) throws IOException {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			if (n.buf != null) this.loadColorBuffer(loader, n);
		}
	}
	
	public void loadColorBuffer(PhotonFieldSceneLoader loader, StoredItem it) throws IOException {
		String name = it.absorber.toString();
		if (it.absorber instanceof Nameable) name = ((Nameable)it.absorber).getName();
		if (it.buf != null) it.buf.load(loader, name);
	}
	
	public void setBRDF(SphericalProbabilityDistribution brdf) {
		this.setBRDF(this.lastAdded.absorber, brdf);
	}
	
	public void setColorBufferDimensions(int w, int h, double m) {
		this.setColorBufferDimensions(this.lastAdded.absorber, w, h, m);
	}
	
	public void setBRDF(Absorber a, SphericalProbabilityDistribution brdf) {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			
			if (n.absorber == a) {
				n.brdf = brdf;
				if (n.absorber instanceof BRDF) ((BRDF)n.absorber).setBRDF(brdf);
				return;
			}
		}
	}
	
	public void setColorBufferDimensions(Absorber a, int w, int h, double m) {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			
			if (n.absorber == a) {
				n.setColorBufferSize(w, h, m);
				return;
			}
		}
	}
	
	public void setBufferListener(BufferListener l) {
		this.listener = l;
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			n.setBufferListener(this.listener);
		}
	}
	
	public double[] getLocation(Absorber a) {
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			StoredItem n = (StoredItem) itr.next();
			if (n.absorber == a) return n.position;
		}
		
		return null;
	}
	
	public void setOrderMethod(int order) { this.order = order; }
	public int getOrderMethod() { return this.order; }
	
	public void setSpreadAngle(double angle) { this.spreadAngle = angle; }
	public double getSpreadAngle() { return this.spreadAngle; }
	
	public void setSpreadCount(int count) { this.spreadCount = count; }
	public int getSpreadCount() { return this.spreadCount; }

	public void setPotentialMap(PotentialMap m) { this.map = m; }
	public PotentialMap getPotentialMap() { return this.map; }
	public void setMaxProximity(double radius) { this.max = Math.min(2*this.bound, radius); }
	public double getMaxProximity() { return this.max; }
	
	public void setFastAbsorption(boolean fast) { this.fast = fast; }
	public boolean getFastAbsorption() { return this.fast; }
	
	public boolean absorb(double x[], double p[], double energy) {
		if (this.fast && this.closest != null && this.closest.fast) {
			StoredItem as = this.closest;
			Absorber a = this.closest.absorber;
			double nx[] = VectorMath.subtract(x, this.closest.position);
			double y[] = VectorMath.clone(nx);
			VectorMath.addMultiple(y, p, this.delay + this.e);
			double d = this.getDistance(
						VectorMath.add(x, VectorMath.multiply(p, this.delay, true)), p, false);
			
			if (a instanceof Fast) {
				double t = this.delay / PhysicalConstants.C;
				((Fast)a).setAbsorbDelay(t);
				((Fast)a).setOrigPosition(nx);
			}
			
			Absorber b = null;
			if (this.closest != null) b = this.closest.absorber;
			this.closest = null;
			
			if (a.absorb(y, p, energy)) {
				Volume v = this.getVolume(a);
				
				if (v != null) {
					double uv[] = v.getSurfaceCoords(y);
					boolean front = VectorMath.dot(p, v.getNormal(y)) < 0.0;
					as.addIncidence(uv[0], uv[1], -p[0], -p[1], -p[2], front);
					this.spread(a, v.getNormal(y), nx, y, p, energy, as);
				}
				
				return true;
			}
			
			VectorMath.addMultiple(y, p, -2.0 * this.e);
			
			if (a.absorb(x, p, energy)) {
				Volume v = this.getVolume(a);
				
				if (v != null) {
					double uv[] = v.getSurfaceCoords(x);
					boolean front = VectorMath.dot(p, v.getNormal(y)) < 0.0;
					as.addIncidence(uv[0], uv[1], -p[0], -p[1], -p[2], front);
					this.spread(a, v.getNormal(y), nx, y, p, energy, as);
				}
				
				return true;
			}
		}
		
		Iterator itr = this.iterator(true);
		
		w: while (itr.hasNext()) {
			StoredItem it = (StoredItem) itr.next();
			if (it.absorber instanceof Transparent) continue w;
			double l[] = VectorMath.subtract(x, it.position);
			if (VectorMath.length(l) > this.max) continue w;
			if (it.absorber.absorb(l, p, energy)) {
				Volume v = this.getVolume(it.absorber);
				
				if (v != null) {
					double uv[] = v.getSurfaceCoords(l);
					boolean front = VectorMath.dot(p, v.getNormal(l)) < 0.0;
					it.addIncidence(uv[0], uv[1], -p[0], -p[1], -p[2], front);
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	protected static Volume getVolume(Absorber a) {
		Volume v = null;
		
		if (a instanceof VolumeAbsorber)
			v = ((VolumeAbsorber)a).getVolume();
		else if (a instanceof Volume)
			v = (Volume) a;
		
		return v;
	}
	
	protected void spread(Absorber a, double n[], double ox[],
							double x[], double p[], double energy, StoredItem it) {
		if (this.spreadAngle <= 0.0) return;
		Volume v = this.getVolume(a);
		Elipse.loadConicSection(ox, x, n, this.spreadAngle);
		
		for (int i = 0; i < this.spreadCount; i++) {
			double s[] = Elipse.getSample();
			
			// Consider changing p to s - ox
			if (!a.absorb(s, p, energy) && v != null) {
				double in = v.intersect(s, p);
				VectorMath.addMultiple(s, p, in + e);
				
				if (a.absorb(s, p, energy)) {
					double uv[] = v.getSurfaceCoords(s);
					boolean front = VectorMath.dot(p, v.getNormal(s)) < 0.0;
					it.addIncidence(uv[0], uv[1], -p[0], -p[1], -p[2], front);
				}
			} else if (v != null) {
				double uv[] = v.getSurfaceCoords(s);
				boolean front = VectorMath.dot(p, v.getNormal(s)) < 0.0;
				it.addIncidence(uv[0], uv[1], -p[0], -p[1], -p[2], front);
			}
		}
	}
	
	protected void selectEmitter() {
		Iterator itr = this.iterator(true);
		
		while (itr.hasNext()) {
			StoredItem it = (StoredItem) itr.next();
			double e = it.absorber.getNextEmit();
			
			if (e < this.clock.getTickInterval()) {
				this.emitter = it;
				return;
			}
		}
	}
	
	public double[] emit() {
		if (this.emitter == null) {
			return null;
		} else {
			double d[] = null;
			
			v: if (this.emitter.buf != null) {
				Volume vol = this.getVolume(this.emitter.absorber);
				if (vol == null) break v;
				
				double p[] = this.emitter.absorber.getEmitPosition();
				double norm[] = vol.getNormal(p);
				
				if (norm == null)
					System.out.println("AbsorberHashSet: No normal for " + vol);
				
				double energy = this.emitter.absorber.getEmitEnergy();
				double n = 1000 * PhysicalConstants.HC / energy;
				
				d = this.emitter.absorber.emit();
				
				if (d == null)
					System.out.println("AbsorberHashSet: " +
										this.emitter.absorber +
										" emitted null.");
				
				boolean front = VectorMath.dot(d, norm) >= 0.0;
				
				double uv[] = vol.getSurfaceCoords(p);
				RGB c = new RGB(this.colorDepth, n);
				this.emitter.addColor(uv[0], uv[1], front, c);
				this.emitter.addExitance(uv[0], uv[1], d[0], d[1], d[2], front);
			} else {
				d = this.emitter.absorber.emit();
			}
			
			this.emitter = null;
			return d;
		}
	}
	
	public double getEmitEnergy() {
		if (this.emitter == null) this.selectEmitter();
		if (this.emitter == null) return 0.0;
		return this.emitter.absorber.getEmitEnergy();
	}

	public double getNextEmit() {
		if (this.emitter == null) this.selectEmitter();
		if (this.emitter == null) return Integer.MAX_VALUE;
		return this.emitter.absorber.getNextEmit();
	}
	
	public double[] getEmitPosition() {
		if (this.emitter == null) this.selectEmitter();
		if (this.emitter == null) return null;
		double x[] = this.emitter.absorber.getEmitPosition();
		x = VectorMath.add(x, this.emitter.position);
		return x;
	}

	public void setClock(Clock c) {
		this.clock = c;
		
		if (this.clock != null)
			this.e = this.clock.getTickInterval() * PhysicalConstants.C / 100.0;
		
		Iterator itr = this.iterator();
		while (itr.hasNext()) ((StoredItem)itr.next()).absorber.setClock(this.clock);
	}

	public Clock getClock() { return this.clock; }
	
	public Iterator absorberIterator() {
		Iterator itr = new Iterator() {
			private Iterator itr = AbsorberHashSet.super.iterator();
			
			public boolean hasNext() { return this.itr.hasNext(); }
			public Object next() { return ((StoredItem)this.itr.next()).absorber; }
			public void remove() { this.itr.remove(); }
		};
		
		return itr;
	}
	
	public void notifySetListeners() {
		Iterator itr = this.sList.iterator();
		while (itr.hasNext()) ((SetListener)itr.next()).noteUpdate();
	}
	
	public boolean addSetListener(SetListener l) {
		this.sList.add(l);
		return true;
	}
	
	public Iterator iterator() { return this.iterator(false); }
	
	public Iterator iterator(boolean shuffle) {
		if (this.itemsNow || !this.itemsEnabled)
			return super.iterator();
		
		if ((this.itemsUser == Thread.currentThread()
				|| this.itemsUser == null)
				&& this.items != null) {
			this.itemsUser = Thread.currentThread();
			return this.items;
		} else if (this.items != null) {
			System.out.println("AbsorberHashSet: Needed extra iterator for " +
								Thread.currentThread() + " (" + this.itemsUser + ")");
		}
		
		if (!shuffle || this.order != RANDOM_ORDER) {
			this.itemsNow = true;
			
			this.items = new Iterator() {
				private SetListener l = new SetListener() {
					public void noteUpdate() { myNoteUpdate(); }
				};
				
				private int index = 0;
				private StoredItem data[] = (StoredItem[])
						AbsorberHashSet.this.toArray(new StoredItem[0]);
				private boolean b = AbsorberHashSet.this.addSetListener(l);
				
				public boolean hasNext() {
					if (this.index >= data.length) {
						this.index = 0;
						AbsorberHashSet.this.itemsUser = null;
						return false;
					} else {
						return true;
					}
				}
				
				public Object next() { return this.data[this.index++]; }
				public void remove() { }
				
				public void myNoteUpdate() {
					AbsorberHashSet.this.itemsNow = true;
					this.data = (StoredItem[])
						AbsorberHashSet.this.toArray(new StoredItem[0]);
					AbsorberHashSet.this.itemsNow = false;
				}
			};
			
			this.itemsNow = false;
			
			return this.items;
		}
		
		// this.clearChecked();
		
		final StoredItem itrs[] = new StoredItem[this.size()];
		
		Iterator itr = super.iterator();
		
		int i = 0;
		
		while (itr.hasNext() && i < itrs.length) {
			int start = (int) (Math.random() * itrs.length);
			boolean first = true;
			
			j: for (int j = start; first || j != start; j = (j + 1) % itrs.length) {
				first = false;
				if (itrs[j] != null) continue j;
				
				itrs[j] = (StoredItem) itr.next();
				i++;
				break j;
			}
		}
		
		this.cleared = false;
		
		return new Iterator() {
			private int index = 0;
			
			public boolean hasNext() { return index < itrs.length; }
			public Object next() { return itrs[this.index++]; }
			public void remove() { }
		};
	}
	
	protected void clearChecked() {
		if (this.cleared) return;
		
		Iterator itr = this.iterator(false);
		
		while (itr.hasNext()) {
			((StoredItem)itr.next()).checked = false;
		}
		
		this.cleared = true;
	}

	public boolean add(Object o) {
		if (o instanceof StoredItem == false)
			throw new IllegalArgumentException(o +
					" not instance of AbsorberHashSet.StoredItem");
		
		return super.add((StoredItem) o);
	}

	public boolean addAll(Collection c) {
		Iterator itr = c.iterator();
		while (itr.hasNext()) this.add(itr.next());
		return true;
	}
	
	public void setBound(double bound) {
		this.bound = bound;
		this.max = Math.min(this.max, 2*this.bound);
	}
	
	public double getBound() { return this.bound; }

	public double getDistance(double p[], double d[]) {
		return this.getDistance(p, d, true, false);
	}
	
	public double getDistance(double p[], double d[], boolean fast) {
		return this.getDistance(p, d, fast, false);
	}
	
	public double getDistance(double p[], double d[], boolean fast, boolean excludeCamera) {
		Iterator itr = this.iterator(false);
		
		double l = Double.MAX_VALUE - 1.0;
		this.closest = null;
		
		w: while (itr.hasNext()) {
			StoredItem s = (StoredItem) itr.next();
			double dist = 0.0;
			
			double x[] = VectorMath.subtract(p, s.position);
			
			if (s.absorber instanceof Transparent) {
				continue w;
			} else if (excludeCamera && s.absorber instanceof Camera) {
				continue w;
			} else if (s.absorber instanceof AbsorberSet) {
				dist = ((AbsorberSet) s.absorber).getDistance(x, d);
			} else if (s.absorber instanceof Volume) {
				dist = ((Volume) s.absorber).intersect(x, d);
			} else if (s.absorber instanceof VolumeAbsorber) {
				dist = ((VolumeAbsorber) s.absorber).getVolume().intersect(x, d);
			}
			
			if (dist < l) {
				l = dist;
				this.closest = s;
			}
		}
		
		if (fast && this.closest != null && this.fast && this.closest.fast) {
			this.delay = l;
			return 0.0;
		} else {
			return l;
		}
	}
	
	public AbsorberSetRayTracer getRayTracer() {
		try {
			return this.getRayTracer(null);
		} catch (IOException e) {
			System.out.println("AbsorberHashSet: " + e);
		}
		
		return null;
	}
	
	public AbsorberSetRayTracer getRayTracer(PhotonFieldSceneLoader loader) throws IOException {
		if (loader != null)
			this.storeColorBuffers(loader);
		
		Iterator itr = this.iterator(false);
		
		Camera camera = null;
		List lights = new ArrayList();
		int w = 0, h = 0;
		
		while (itr.hasNext()) {
			StoredItem it = (StoredItem) itr.next();
			Absorber a = it.absorber;
			if (a instanceof Camera && camera == null) camera = (Camera) a;
			if (a instanceof Light) lights.add((Light) a);
			
			if (a instanceof AbsorptionPlane) {
				w = (int) ((AbsorptionPlane) a).getWidth();
				h = (int) ((AbsorptionPlane) a).getHeight();
			}
			
			if (a instanceof PinholeCameraAbsorber) {
				PinholeCameraAbsorber ca = (PinholeCameraAbsorber) a;
				// ca.setColorable(this);
				ca.setLocation(new Vector(it.position[0],
											it.position[1],
											it.position[2]));
				w = (int) ca.getWidth();
				h =  (int) ca.getHeight();
			}
			
			if (a instanceof Locatable) {
				((Locatable)a).setLocation(new Vector(it.position[0],
														it.position[1],
														it.position[2]));
			}
		}
		
//		if (w == 0) return null;
//		if (h == 0) return null;
//		if (camera == null) return null;
		
		RayTracingEngine.castShadows = false;
		RayTracingEngine.premultiplyIntensity = false;
		
		AbsorberSetRayTracer tracer =
				new AbsorberSetRayTracer(camera, new Surface[] {this},
										(Light[]) lights.toArray(new Light[0]),
										w, h);
		
		return tracer;
	}
	
	public void setColor(double r, double g, double b) {
		this.rgb = new RGB(this.colorDepth, r, g, b);
	}
	
	public RGB getColorAt(Vector point) { return this.rgb; }
	
	public Vector getNormalAt(Vector point) {
		Volume v = this.getVolume(this.rclosest.absorber);
		if (v == null) return new Vector();
		double p[] = {point.getX(), point.getY(), point.getZ()};
		double x[] = VectorMath.subtract(p, this.rclosest.position);
		double n[] = v.getNormal(x);
		return new Vector(n[0], n[1], n[2]);
	}
	
	public boolean getShadeBack() { return false; }
	public boolean getShadeFront() { return true; }
	
	public boolean intersect(Ray ray) {
		double c[] = ray.getCoords();
		double x[] = {c[0], c[1], c[2]};
		double d[] = {c[3], c[4], c[5]};
		return (this.getDistance(x, d, false, true) < Double.MAX_VALUE - 2);
	}
	
	public Intersection intersectAt(Ray ray) {
		double c[] = ray.getCoords();
		double x[] = {c[0], c[1], c[2]};
		double d[] = {c[3], c[4], c[5]};
		double dist = this.getDistance(x, d, false, true);
		
		double di[];
		
		if (dist < Double.MAX_VALUE - 2 && dist > 0 && this.closest != null)
			di = new double[] {dist};
		else
			di = new double[0];
		
		this.rclosest = this.closest;
		
		return new Intersection(ray, this, di);
	}
	
	public RGB shade(ShaderParameters p) {
		if (this.rclosest == null) return new RGB(this.colorDepth, 0.0, 0.0, 0.0);
		
		if (this.rclosest.highlight) {
			if (Math.random() < 0.1)
				System.out.println("AbsorberHashSet: " + this.rclosest + " was highlighted.");
			return new RGB(this.colorDepth, 1.0, 1.0, 1.0);
		}
		
		Vector point = p.getPoint();
		double po[] = {point.getX(), point.getY(), point.getZ()};
		Vector n = this.getNormalAt(point);
		double norm[] = {n.getX(), n.getY(), n.getZ()};
		double d = n.dotProduct(p.getLightDirection());
		boolean front = p.getViewerDirection().dotProduct(n) >= 0.0;
		
		if (d < 0) {
			if (this.getShadeBack())
				d = 0;
			else
				d = -d;
		}
		
		Volume vol = this.rclosest.getVolume();
		
		RGB r = (RGB) this.rgb.clone(); // Absorption plane
		
		RGB b = null; // Indirect
		RGB c = null; // Raytraced
		
		if (vol != null && d >= 0) {
			double uv[] = vol.getSurfaceCoords(
							VectorMath.subtract(po, this.rclosest.position));
			c = this.rclosest.getColorAt(uv[0], uv[1], front, true, n); // Base color
			b = this.rclosest.getColorAt(uv[0], uv[1], front, false, n);
			double v[] = this.rclosest.incidence.getVector(uv[0], uv[1], front);
			v[0] *= -1;
			v[1] *= -1;
			v[2] *= -1;

			double vd[] = p.getViewerDirection().getData();
			
			if (this.rclosest.brdf != null)
				b.multiplyBy(VectorMath.dot(vd, this.rclosest.brdf.getSample(v, norm)));
			else
				b.multiplyBy(VectorMath.dot(this.rclosest.exitance.getVector(uv[0], uv[1], front), vd));
		}
		
		// Transmitted or reflected
		
		if (this.rclosest.brdf != null && p.getReflectionCount() < 2) {
			Vector viewer = p.getViewerDirection();
			double v[] = {viewer.getX(), viewer.getY(), viewer.getZ()};
			double s[] = this.rclosest.brdf.getSample(v, norm);
			
			p.addReflection();
				
			Vector vpo = new Vector(po[0], po[1], po[2]);
			Vector vs = new Vector(s[0], s[1], s[2]);
			c = RayTracingEngine.lightingCalculation(vpo, vs, this,
													p.getOtherSurfaces(),
													p.getAllLights(), p);
			if (c != null)
				c.multiplyBy(p.getLight().getIntensity() * d);
			
//			if (c != null && c.length() > 0.05) System.out.println(c);
//			
//			double dist = this.getDistance(po, s, false, true);
//			
//			v: if (this.closest != null) {
//				vol = this.getVolume(this.closest.absorber);
//				if (vol == null) break v;
//				VectorMath.addMultiple(po, s, dist);
//				po = VectorMath.subtract(po, this.closest.position);
//				
//				norm = vol.getNormal(po);
//				front = VectorMath.dot(norm, s) <= 0.0;
//				
//				double uv[] = vol.getSurfaceCoords(po);
//				
//				if (uv[0] < 0.0 || uv[0] > 1.0 || uv[1] < 0.0 || uv[1] > 1.0) {
//					if (Math.random() < 0.1)
//						System.out.println("AbsorberHashSet: Invalid surface coords from " +
//											vol + " (" + uv[0] + ", " + uv[1] + ")");
//				} else {
//					c = this.closest.getColorAt(uv[0], uv[1], front, false);
//				}
//			}
		}
		
		if (b != null) r.addTo(b);
		if (c != null) r.addTo(c);
		
		if (Math.random() < 0.0000001) {
			System.out.println("****************");
			System.out.println("AbsorberHashSet: " + this.rclosest + " " + this.closest);
			System.out.println("AbsorberHashSet: Colors = " + this.rgb + " " + b + " " + c );
			System.out.println("AbsorberHashSet: Final = " + r);
			System.out.println();
		}
		
		return r;
	}
}
