package com.almostrealism.physics.circles;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CircleSet implements Runnable {
	private static class Circle { int x, y, z, r; }
	
	private List circles;
	
	public CircleSet() {
		this.circles = new ArrayList();
		new Thread(this).start();
	}
	
	public void addCircle(int x, int y, int z) {
		Circle c = new Circle();
		c.x = x;
		c.y = y;
		c.z = z;
		synchronized (this.circles) { this.circles.add(c); }
	}
	
	public void paint(Graphics g) {
		synchronized (this.circles) {
			Iterator itr = this.circles.iterator();
			
			while (itr.hasNext()) {
				Circle c = (Circle) itr.next();
				g.setColor(Color.blue);
				g.drawOval(c.x - c.r, c.y - c.r, 2 * c.r, 2 * c.r);
			}
		}
	}
	
	public void run() {
		while (true) {
			synchronized (this.circles) {
				Iterator itr = this.circles.iterator();
				
				while (itr.hasNext()) {
					Circle c = (Circle) itr.next();
					c.r++;
					if (c.r > 100) itr.remove();
				}
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException ie) { }
		}
	}
}
