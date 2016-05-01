package com.almostrealism.photonfield.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import com.wolfram.jlink.MathLinkFactory;

public class MathematicaLoader {
	protected static class Node {
		int x, y;
		List children = new ArrayList();
	}
	
	private String args[];
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public MathematicaLoader(String args[]) {
		this.args = args;
	}
	
	public void generateSpanningTree(String exp, OutputStream out) {
		PrintWriter xml = new PrintWriter(new OutputStreamWriter(out));
		double density[][] = this.execute(exp);
		boolean used[][] = new boolean[density.length][density[0].length];
		List leafs = new ArrayList();
		
		int x = density.length / 2;
		int y = density[0].length / 2;
		
		Node root = new Node();
		root.x = x;
		root.y = y;
		leafs.add(root);
		
		w: while (true) {
			List next = new ArrayList();
			Iterator itr = leafs.iterator();
			while (itr.hasNext()) this.printXML(xml, (Node) itr.next());
			
			itr = leafs.iterator();
			while (itr.hasNext()) {
				Node n = (Node) itr.next();
				n = this.nextNode(n, density);
				if (n == null) continue w;
				next.add(n);
			}
			
			if (next.size() <= 0)
				break w;
			else
				leafs = next;
		}
	}
	
	protected void printXML(PrintWriter out, Node n) {
		
	}
	
	protected Node nextNode(Node n, double density[][]) {
		return this.nextNode(n, density, n.x, n.y);
	}
	
	protected Node nextNode(Node n, double density[][], int x, int y) {
		return null;
	}
	
	public double[][] execute(String exp) {
		KernelLink ml = null;

		try {
			ml = MathLinkFactory.createKernelLink(this.args);
		} catch (MathLinkException e) {
			System.out.println("MathematicaLoader: Could not open kernel link (" +
								e.getMessage() + ")");
			return null;
		}

		try {
			ml.discardAnswer();

			ml.evaluate(exp);
			ml.waitForAnswer();

			return ml.getDoubleArray2();
		} catch (MathLinkException e) {
			System.out.println("MathematicaLoader: Math link error (" + e.getMessage() + ")");
		} finally {
			ml.close();
		}
		
		return null;
	}
}
