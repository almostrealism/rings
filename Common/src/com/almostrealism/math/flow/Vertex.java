package com.almostrealism.math.flow;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class Vertex {
	private static final String numLabels[] =
			{"One", "Two", "Three", "Four", "Five",
			"Six", "Seven", "Eight", "Nine", "Ten"};
	private static final String aLabels[] =
			{"A", "B", "C", "D", "E", "F", "G", "H",
			"I", "J", "K", "L", "M", "N", "O", "P",
			"Q", "R", "S", "T", "U", "V", "W", "X",
			"Y", "Z"};
	
	private String label = "-";
	private double delta;
	private Vertex phi;
	
	private Set peers;
	private Hashtable min, max, flow;
	
	public Vertex() {
		this.peers = new HashSet();
		this.min = new Hashtable();
		this.max = new Hashtable();
		this.flow = new Hashtable();
	}
	
	public Vertex(String label) { this(); this.label = label; }
	
	public void addPeer(Vertex v) { this.peers.add(v); }
	public boolean hasPeer(Vertex v) { return this.peers.contains(v); }
	
	public Vertex hasPeer(Set s) {
		Iterator itr = s.iterator();
		
		while (itr.hasNext()) {
			Object o = itr.next();
			
			if (this.peers.contains(o))
				return (Vertex) o;
		}
		
		return null;
	}
	
	public double max(Vertex v) { return ((Number)this.max.get(v)).doubleValue(); }
	public double min(Vertex v) { return ((Number)this.min.get(v)).doubleValue(); }
	public double flow(Vertex v) { return ((Number)this.flow.get(v)).doubleValue(); }
	
	public void setFlow(Vertex v, double f) {
		this.flow.remove(v);
		this.flow.put(v, new Double(f));
	}
	
	public void setDelta(double d) { this.delta = d; }
	public double getDelta() { return this.delta; }
	public void setPhi(Vertex p) { this.phi = p; }
	public Vertex getPhi() { return this.phi; }
	
	public Path getPhiPath() {
		Path p = new Path();
		p.addVertex(this);
		
		if (this.phi != null) {
			Path l = this.phi.getPhiPath();
			p.append(l);
		}
		
		return p;
	}
	
	public static String getNumberLabel(int i) { return numLabels[i]; }
	public static String getAlphaLabel(int i) { return aLabels[i]; }
	
	public String toString() { return this.label; }
}
