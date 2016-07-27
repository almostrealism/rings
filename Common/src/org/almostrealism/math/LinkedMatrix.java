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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinkedMatrix {
	public static boolean verbose = false, showSol = true;
	public static double logLevel = 0.001;
	
	public static interface SolutionOutput {
		public void nextColumn(int index);
		public void nextRow();
	}
	
	public static class Node {
		protected String name;
		protected int index;
		protected int size;
		protected Node u, d, l, r, c;
		
		public String toString() {
			if (this.name == null)
				return super.toString();
			else
				return this.name;
		}
	}
	
	int count = 0;
	
	private SolutionOutput output;
	
	private Node root;
	private int cols;
	private long updates, solutions;
	
	public static void main(String args[]) throws IOException {
		if (args.length < 1) {
			System.out.println("LinkedMatrix: Please specify a matrix file.");
			System.exit(1);
		}
		
		BufferedReader in = new BufferedReader(new FileReader(args[0]));
		List l = new ArrayList();
		String line = null;
		
		while ((line = in.readLine()) != null) {
			String row[] = line.split(" ");
			boolean data[] = new boolean[row.length];
			
			for (int i = 0; i < row.length; i++) {
				if (row[i].equals("0"))
					data[i] = false;
				else
					data[i] = true;
			}
			
			l.add(data);
		}
		
		boolean b[][] = (boolean[][]) l.toArray(new boolean[0][0]);
		b = transpose(b);
		LinkedMatrix m = new LinkedMatrix(b);
		
		if (args.length > 4) {
			int x = Integer.parseInt(args[1]);
			int y = Integer.parseInt(args[2]);
			int z = Integer.parseInt(args[3]);
			int off = Integer.parseInt(args[4]);
			System.out.println("LinkedMatrix: Loading Matrix3DSolutionOutput(" +
								x + ", " + y + ", " + z + ")[" + off + " pieces].");
			m.setSolutionOutput(new Matrix3DSolutionOutput(x, y, z, off));
		}
		
		m.search();
	}
	
	public static boolean[][] transpose(boolean m[][]) {
		boolean n[][] = new boolean[m[0].length][m.length];
		
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < m[i].length; j++) {
				n[j][i] = m[i][j];
			}
		}
		
		return n;
	}
	
	public LinkedMatrix(boolean m[][]) {
		this.root = new Node();
		
		Node col = this.root;
		Node c = null;
		int tot = 0;
		
		Node n[][] = new Node[m.length][m[0].length];
		
		for (int i = 0; i < m.length; i++) {
			Node nc = new Node();
			nc.index = tot;
			nc.name = String.valueOf(tot);
			tot++;
			nc.c = nc;
			col.r = nc;
			nc.l = col;
			col = nc;
			
			c = col;
			
			j: for (int j = 0; j < m[i].length; j++) {
				if (!m[i][j]) continue j;
				
				n[i][j] = new Node();
				n[i][j].index = col.index;
				n[i][j].name = col.name + "," + j;
				n[i][j].u = c;
				n[i][j].c = col;
				c.d = n[i][j];
				c = n[i][j];
				col.size++;
			}
			
			c.d = col;
			col.u = c;
		}
		
		this.cols = tot;
		
		col.r = this.root;
		this.root.l = col;
		
		Node first[] = new Node[n[0].length];
		Node r[] = new Node[n[0].length];
		
		for (int i = 0; i < n.length; i++) {
			j: for (int j = 0; j < n[i].length; j++) {
				if (n[i][j] == null) continue j;
				
				n[i][j].l = r[j];
				if (r[j] != null) r[j].r = n[i][j];
				if (first[j] == null) first[j] = n[i][j];
				r[j] = n[i][j];
			}
		}
		
		i: for (int i = 0; i < r.length; i++) {
			if (r[i] == null) continue i;
			r[i].r = first[i];
			first[i].l = r[i];
		}
	}
	
	public void search() { this.search(0, new ArrayList()); }
	
	public void search(int k, List o) {
		if (this.root.r == this.root) {
			Iterator itr = o.iterator();
			
			int t = 0;
			
			while (itr.hasNext()) {
				Node f = (Node) itr.next();
				Node n = f.r;
				
				if (this.output != null)
					this.output.nextColumn(f.index);
				else
					System.out.print(f.index + " ");
				
				t++;
				
				while (n != f) {
					if (this.output != null)
						this.output.nextColumn(n.index);
					else
						System.out.print(n.index + " ");
					
					t++;
					n = n.r;
				}
				
				if (this.output != null)
					this.output.nextRow();
				else
					System.out.println();
			}
			
			this.solutions += t;
			return;
		}
		
		if (verbose) System.out.println("Search (" + k + ") begins...");
		
		Node c = this.nextColumn();
		
		this.cover(c);
		
		Node i = c.d;
		Node j = i.r;
		
		while (i != c) {
			o.add(i);
			
			while (j != i) {
				this.cover(j.c);
				j = j.r;
			}
			
			this.search(k + 1, o);
			
			o.remove(o.size() - 1);
			
			j = j.l;
			
			while (j != i) {
				this.uncover(j.c);
				j = j.l;
			}
			
			i = i.d;
			j = i.r;
		}
		
		this.uncover(c);
		
		if (k == 1) {
			int u = (int) (this.updates * Math.pow(10.0, -7.0));
			double billions = u / 100.0;
			
			System.out.println("Search (" + k + "): So far performed " +
								billions + " billion updates and found " +
								(this.solutions / this.cols) + " solutions.");
		}
		
		if (verbose) System.out.println("Search (" + k + ") ends.");
	}
	
	public Node nextColumn() {
		Node n = this.root.r;
		Node x = null;
		int s = Integer.MAX_VALUE;
		
		while (n != this.root) {
			if (n.size < s) {
				x = n;
				s = n.size;
			}
			
			n = n.r;
		}
		
		if (x == this.root)
			return null;
		else
			return x;
	}
	
	public void cover(Node n) {
		if (verbose) System.out.println("LinkedMatrix: Covering column " + n);
		
		n.r.l = n.l;
		n.l.r = n.r;
		
		this.updates++;
		if (verbose) System.out.println("LinkedMatrix: Performed column update.");
		
		Node i = n.d;
		Node j = i.r;
		
		int tot = 0;
		
		while (i != n) {
			while (j != i) {
				if (verbose)
					System.out.println("LinkedMatrix: Removing " + j + " from " + j.c);
				
				j.d.u = j.u;
				j.u.d = j.d;
				j.c.size--;
				
				j = j.r;
				tot++;
			}
			
			i = i.d;
			j = i.r;
		}
		
		this.updates += tot;
		if (verbose) System.out.println("LinkedMatrix: Performed " + tot + " updates.");
	}
	
	public void uncover(Node n) {
		if (verbose) System.out.println("LinkedMatrix: Uncovering column " + n);
		
		Node i = n.u;
		Node j = i.l;
		
		int tot = 0;
		
		while (i != n) {
			while (j != i) {
				if (verbose)
					System.out.println("LinkedMatrix: Connecting " + j.d +
										" and " + j.u + " to " + j);
				
				j.d.u = j;
				j.u.d = j;
				j.c.size++;
				
				j = j.l;
				tot++;
			}
			
			i = i.u;
			j = i.l;
		}
		
		this.updates += tot;
		if (verbose) System.out.println("LinkedMatrix: Performed " + tot + " updates.");
		
		n.l.r = n;
		n.r.l = n;
		
		this.updates++;
		if (verbose) System.out.println("LinkedMatrix: Performed column update.");
		
		count++;
	}
	
	public void setSolutionOutput(SolutionOutput output) {
		this.output = output;
	}
	
	public String toString() {
		Node i = this.root.r;
		
		StringBuffer c = new StringBuffer();
		StringBuffer s = new StringBuffer();
		
		while (i != this.root) {
			c.append(i);
			s.append(i.size);
			
			while (c.length() > s.length()) s.append(" ");
			while (s.length() > c.length()) c.append(" ");
			
			c.append(" ");
			s.append(" ");
		}
		
		return c + "\n" + s;
	}
}
