package com.almostrealism.physics.circles;

public class ParticleSet implements Runnable {
	int force = -10000;
	private int x[][], v[][], a[][];
	private int tot, max;
	private boolean clean = true;
	
	private MotionListener mlisten;
	private IterationListener ilisten;
	
	public ParticleSet(int max) {
		this.max = max;
		this.x = new int[max][3];
		this.v = new int[max][3];
		this.a = new int[max][3];
	}
	
	public void addParticle(int pos[]) {
		if (tot == max) return;
		this.x[tot] = pos;
		this.tot++;
	}
	
	public void start() { if (clean) new Thread(this).start(); clean = false; }
	
	public int[] force(int x, int y, int z) {
		int f[] = new int[3];
		
		for (int i = 0; i < this.tot; i++) {
			double dx = x - this.x[i][0];
			double dy = y - this.x[i][1];
			double dz = z - this.x[i][2];
			
			double r = dx * dx + dy * dy + dz * dz;
			double rr = r * Math.sqrt(r);
			rr = rr / force;
			f[0] += dx / rr;
			f[1] += dy / rr;
			f[2] += dz / rr;
		}
		
		return f;
	}
	
	public void run() {
		while (true) {
			for (int i = 0; i < this.tot; i++) {
				j: for (int j = 0; j < this.tot; j++) {
					if (i == j) continue j;
					
					double dx = this.x[j][0] - this.x[i][0];
					double dy = this.x[j][1] - this.x[i][1];
					double dz = this.x[j][2] - this.x[i][2];
					
					double r = dx * dx + dy * dy + dz * dz;
					if (r < 0) continue j;
					double rr = r * Math.sqrt(r);
					rr = rr / force;
					this.v[i][0] += dx / rr;
					this.v[i][1] += dy / rr;
					this.v[i][2] += dz / rr;
					
//					System.out.println(r + " " + rr + " " + dx + " " + this.v[i][0]);
				}
			}
			
			for (int i = 0; i < this.tot; i++) {
				int x = this.x[i][0];
				int y = this.x[i][1];
				this.x[i][0] += this.v[i][0];
				this.x[i][1] += this.v[i][1];
				this.x[i][2] += this.v[i][2];
				
				if (this.mlisten != null) this.mlisten.move(x, y, this.x[i][0], this.x[i][1]);
			}
			
			if (this.ilisten != null) this.ilisten.iterationComplete();
		}
	}
	
	public void addMotionListener(MotionListener l) { this.mlisten = l; }
	public void addIterationListener(IterationListener l) { this.ilisten = l; }
}