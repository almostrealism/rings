package net.sf.j3d.physics.pfield.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class PriorityQueue {
	private static double c = Math.pow(10.0, 10.0);
	private SortedSet data;
	
	protected class StoredItem implements Comparable {
		Object o;
		double p;
		
		public StoredItem(Object o, double p) {
			this.o = o;
			this.p = p;
		}
		
		public boolean equals(Object o) {
			if (o instanceof StoredItem == false) return false;
			if (((StoredItem)o).o != this.o) return false;
			if (((StoredItem)o).p != this.p) return false;
			return true;
		}
		
		public int hashCode() {
			return (int) (p * c);
		}
		
		public int compareTo(Object o) {
			if (o instanceof StoredItem == false) return Integer.MIN_VALUE;
			int x = (int) ((((StoredItem) o).p - this.p) * c);
			return x;
		}
	}
	
	public PriorityQueue() {
		this.data = new TreeSet();
	}
	
	public int put(Object o, double p) {
		this.data.add(new StoredItem(o, p));
		return this.data.size();
	}
	
	public double peek() {
		if (this.data.size() <= 0) return Double.MAX_VALUE;
		StoredItem s = (StoredItem) this.data.last();
		StoredItem f = (StoredItem) this.data.first();
		if (f.p - s.p < 0)
			System.out.println("PriorityQueue: Last - Next = " + (f.p - s.p));
		return s.p;
	}
	
	public Object peekNext() {
		if (this.data.size() <= 0) return null;
		StoredItem s = (StoredItem) this.data.last();
		
		return s.o;
	}
	
	public Object next() {
		StoredItem s = (StoredItem) this.data.last();
		this.data.remove(s);
		return s.o;
	}
	
	public int size() { return this.data.size(); }
}
