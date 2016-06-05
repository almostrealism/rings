package com.almostrealism.replicator.geometry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.almostrealism.geometry.BasicGeometry;
import com.almostrealism.raytracer.engine.Surface;

public class DefaultReplicant extends Replicant implements Iterable<BasicGeometry> {
	private Map<String, BasicGeometry> geo;
	
	public DefaultReplicant(Surface s) {
		geo = new HashMap<String, BasicGeometry>();
		setGeometry(this);
		setSurface(s);
	}
	
	public BasicGeometry get(String name) { return geo.get(name); }
	
	public void put(String name, BasicGeometry g) { geo.put(name, g); }
	
	public Iterator<BasicGeometry> iterator() { return geo.values().iterator(); }
	
	public void clear() { geo.clear(); }
}
