package com.almostrealism.util.graphics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AverageColor implements ColorProducer {
	private static class Color {
		double p;
		RGB c;
	}
	
	private List colors;
	private double tot;
	private boolean invert;
	
	public AverageColor() {
		this.colors = new ArrayList();
	}
	
	public void addColor(double p, RGB c) {
		if (this.invert) p = 1.0 / p;
		
		Color color = new Color();
		color.p = p;
		color.c = c;
		this.colors.add(color);
		this.tot += p;
	}
	
	public void setInvert(boolean invert) { this.invert = invert; }
	
	public RGB evaluate(Object args[]) {
		RGB c = new RGB(0.0, 0.0, 0.0);
		Iterator itr = this.colors.iterator();
		
		w: while (itr.hasNext()) {
			Color n = (Color) itr.next();
			if (n.c == null) continue w;
			c.addTo(n.c.multiply(n.p / this.tot));
		}
		
		return c;
	}
}
