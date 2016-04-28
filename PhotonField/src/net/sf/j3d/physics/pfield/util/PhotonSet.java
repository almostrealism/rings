package net.sf.j3d.physics.pfield.util;

public class PhotonSet {
	private int max, last = -1;
	private PriorityQueue queue;
	private double x[][], p[][], e[];
	
	public PhotonSet(int max) {
		this.max = max;
		this.queue = new PriorityQueue();
		this.x = new double[max][3];
		this.p = new double[max][3];
		this.e = new double[max];
	}
	
	public int addPhoton(double x[], double p[], double e, double d) {
		int index[] = {this.nextIndex()};
		this.x[index[0]] = x;
		this.p[index[0]] = p;
		this.e[index[0]] = e;
		this.queue.put(index, d);
		return this.queue.size();
	}
	
	protected int nextIndex() {
		while (e[this.last] >= 0) this.last = (this.last + 1) % (this.max);
		return this.last;
	}
}
