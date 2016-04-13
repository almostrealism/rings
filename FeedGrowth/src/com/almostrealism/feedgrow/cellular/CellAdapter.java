package com.almostrealism.feedgrow.cellular;

import com.almostrealism.feedgrow.content.ProteinCache;

public abstract class CellAdapter<T> implements Cell<T> {
	private ProteinCache<T> o;
	private Receptor<T> r;
	private Receptor<T> meter;
	
	public void setReceptor(Receptor<T> r) { this.r = r; }
	
	public Receptor<T> getReceptor() { return this.r; }
	
	public void setProteinCache(ProteinCache<T> p) { this.o = p; }
	
	public long addProtein(T p) { return o.addProtein(p); }
	
	public T getProtein(long index) { return o.getProtein(index); }
	
	public void setMeter(Receptor<T> m) { this.meter = m; }
	
	/**
	 * Push to the {@link Receptor}.
	 */
	public void push(long proteinIndex) {
		if (meter != null) meter.push(proteinIndex);
		if (r != null) r.push(proteinIndex);
	}
}
