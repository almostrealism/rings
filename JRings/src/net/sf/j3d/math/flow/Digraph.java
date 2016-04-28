package net.sf.j3d.math.flow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Digraph {
	public List vertices;
	
	public Digraph() {
		this.vertices = new ArrayList();
	}
	
	public int create() {
		int i = this.vertices.size();
		
		if (this.vertices.add(new Vertex(Vertex.getAlphaLabel(i))))
			return i;
		else
			return -1;
	}
	
	public Vertex get(int index) { return (Vertex) this.vertices.get(index); }
	public Vertex V(int i) { return this.get(i); }
	
	public Set into(Vertex v) {
		Set l = new HashSet();
		Iterator itr = this.vertices.iterator();
		
		while (itr.hasNext()) {
			Vertex x = (Vertex) itr.next();
			if (x != v && x.hasPeer(v)) l.add(x);
		}
		
		return l;
	}
	
	public Vertex into(Vertex v, Set s) {
		Set l = this.into(v);
		Iterator itr = l.iterator();
		
		while (itr.hasNext()) {
			Object o = itr.next();
			
			if (s.contains(o))
				return (Vertex) o;
		}
		
		return null;
	}
	
	public Set diff(Set l) {
		Set diff = new HashSet();
		Iterator itr = this.vertices.iterator();
		
		while (itr.hasNext()) {
			Object o = itr.next();
			if (!l.contains(o)) diff.add(o);
		}
		
		return diff;
	}
	
	public void clearPhi() {
		Iterator itr = this.vertices.iterator();
		while (itr.hasNext()) ((Vertex) itr.next()).setPhi(null);
	}
	
	public Path getAugmentingPath(Vertex v, Vertex w) {
		this.clearPhi();
		
		Set L = new HashSet();
		Set S = new HashSet();
		Set U = new HashSet();
		
		L.add(v);
		U.addAll(this.diff(L));
		
		v.setDelta(Double.POSITIVE_INFINITY);
		
		while (true) {
			if (L.isEmpty()) return null;
			
			Iterator itr = L.iterator();
			Vertex u = (Vertex) itr.next();
			itr.remove();
			
			t: while (true) {
				boolean done = false;
				
				d: if (!done) {
					Vertex e = u.hasPeer(U);
					if (e == null) break d;
					
					e.setPhi(u);
					e.setDelta(Math.min(u.getDelta(), u.max(e) - u.flow(e)));
					U.remove(e);
					L.add(e);
					
					done = true;
				}
				
				d: if (!done) {
					Vertex e = this.into(u, U);
					if (e == null) break d;
					
					e.setPhi(u);
					e.setDelta(Math.min(u.getDelta(), u.flow(e) - u.min(e)));
					U.remove(e);
					L.add(e);
					
					done = true;
				}
				
				if (L.contains(w)) return v.getPhiPath();
				
				if (!done) {
					S.add(u);
					break t;
				}
			}
		}
	}
}
