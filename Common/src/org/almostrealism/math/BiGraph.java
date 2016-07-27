/*
 * Copyright 2016 Michael Murray
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.almostrealism.math;

public class BiGraph {
	private double w[][];
	private double x[], y[];
	private boolean T[], S[];
	private int m[];
	
	private boolean min;
	
	public static void main(String args[]) {
		BiGraph g = new BiGraph(4);
		
		// g.setMinimize(true);
		
		g.setWeight(0, 0, 90);
		g.setWeight(0, 1, 75);
		g.setWeight(0, 2, 75);
		g.setWeight(0, 3, 80);
		
		g.setWeight(1, 0, 35);
		g.setWeight(1, 1, 85);
		g.setWeight(1, 2, 55);
		g.setWeight(1, 3, 65);
		
		g.setWeight(2, 0, 125);
		g.setWeight(2, 1, 95);
		g.setWeight(2, 2, 90);
		g.setWeight(2, 3, 105);
		
		g.setWeight(3, 0, 45);
		g.setWeight(3, 1, 110);
		g.setWeight(3, 2, 95);
		g.setWeight(3, 3, 115);
		
		g.initLabels();
		int m[] = g.optimizeMatching();
		
		for (int i = 0; i < m.length; i++) {
			System.out.println(i + " -- " + m[i]);
		}
	}
	
	public BiGraph(int nodes) {
		this.w = new double[nodes][nodes];
		this.x = new double[nodes];
		this.y = new double[nodes];
		
		this.T = new boolean[nodes];
		this.S = new boolean[nodes];
		
		this.m = new int[nodes];
		for (int i = 0; i < this.m.length; i++) this.m[i] = -1;
	}
	
	public void setMinimize(boolean min) { this.min = min; }
	
	public int[] optimizeMatching() {
		if (this.isComplete()) return this.m;
		
		this.initS();
		this.initT();
		
		for (int i = 0; i < this.S.length; i++)
			if (this.S[i]) System.out.print(i + " ");
		System.out.println();
		
		w: while (true) {
			boolean N[] = this.neighbors();
			
			if (this.compare(N, this.T)) {
				double a = this.alpha();
				this.updateLabels(this.S, this.x, -a);
				this.updateLabels(this.T, this.y, a);
				
				System.out.print("Update x labels: ");
				for (int i = 0; i < this.x.length; i++)
					System.out.print(this.x[i] + " ");
				System.out.println();
				System.out.print("Update y labels: ");
				for (int i = 0; i < this.y.length; i++)
					System.out.print(this.y[i] + " ");
				System.out.println();
				
				N = this.neighbors();
			}
			
			i: for (int i = 0; i < N.length; i++) {
				if (!N[i] || T[i]) continue i;
				
				int j = this.matched(i);
				
				if (j >= 0) {
					System.out.println(j + " was matched to " + i);
					
					S[j] = true;
					T[i] = true;
					continue w;
				} else {
					System.out.println("Nothing was matched to " + i + ".");
					
					System.out.println("Augmenting...");
					this.augment(i);
					
					System.out.print("Matching: ");
					for (int k = 0; k < this.m.length; k++)
						System.out.print(this.m[k] + " ");
					System.out.println();
					
					return this.optimizeMatching();
				}
			}
		}
	}
	
	public void setWeight(int x, int y, double w) {
		if (this.min)
			this.w[x][y] = -w;
		else
			this.w[x][y] = w;
	}
	
	public void initLabels() {
		for (int i = 0; i < this.x.length; i++) {
			if (this.min)
				this.x[i] = -(Double.MAX_VALUE - 1);
			
			for (int j = 0; j < this.w[i].length; j++) {
				if (this.x[i] < this.w[i][j])
					this.x[i] = this.w[i][j];
			}
		}
		
		System.out.print("Init labels: ");
		for (int i = 0; i < this.x.length; i++)
			System.out.print(this.x[i] + " ");
		System.out.println();
	}
	
	public void initS() {
		this.S = new boolean[this.S.length];
		int i = this.unmatched();
		System.out.println("Init S: " + i);
		this.S[i] = true;
	}
	
	public void initT() { this.T = new boolean[this.T.length]; }
	
	public boolean inSubgraph(int x, int y) {
		return (this.x[x] + this.y[y] == this.w[x][y]);
	}
	
	public double alpha() {
		double a = Double.MAX_VALUE;
		
		i: for (int i = 0; i < this.w.length; i++) {
			if (!this.S[i]) continue i;
			
			j: for (int j = 0; j < this.w[i].length; j++) {
				if (this.T[j]) continue j;
				
				double b = this.x[i] + this.y[j] - this.w[i][j];
				
				if (b < a)
					a = b;
			}
		}
		
		System.out.println("Alpha = " + a);
		
		return a;
	}
	
	public void augment(int k) {
		this.augment(k, new boolean[this.S.length]);
	}
	
	public void augment(int k, boolean used[]) {
		i: for (int i = 0; i < this.S.length; i++) {
			if (used[i] || !this.S[i]) continue i;
			if (!this.inSubgraph(i, k)) continue i;
			
			if (this.m[i] < 0) {
				System.out.println("Augment: Found unmatched " + i + " in S.");
				this.m[i] = k;
				return;
			} else {
				System.out.println("Augment: Found matched " + i + " in S.");
				int j = this.m[i];
				this.m[i] = k;
				used[i] = true;
				this.augment(j, used);
				return;
			}
		}
	}
	
	public void updateLabels(boolean b[], double x[], double a) {
		for (int i = 0; i < x.length; i++) if (b[i]) x[i] += a;
	}
	
	public boolean[] neighbors() {
		boolean N[] = new boolean[this.S.length];
		
		System.out.print("Neighbors: ");
		
		i: for (int i = 0; i < this.S.length; i++) {
			if (!S[i]) continue i;
			
			for (int j = 0; j < this.x.length; j++) {
				if (this.inSubgraph(i, j)) {
					N[j] = true;
					System.out.print("[" + i + ", " + j + "] ");
				}
			}
		}
		
		System.out.println();
		
		return N;
	}
	
	public int matched(int y) {
		for (int i = 0; i < this.m.length; i++)
			if (this.m[i] == y) return i;
		
		return -1;
	}
	
	public int unmatched() {
		for (int i = 0; i < this.m.length; i++)
			if (this.m[i] < 0) return i;
		
		return -1;
	}
	
	public boolean isComplete() {
		for (int i = 0; i < this.w.length; i++) {
			for (int j = 0; j < this.w[i].length; j++) {
				if (this.inSubgraph(i, j) && this.m[i] < 0)
						return false;
			}
		}
		
		return true;
	}
	
	public boolean compare(boolean a[], boolean b[]) {
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i]) return false;
		
		return true;
	}
}
