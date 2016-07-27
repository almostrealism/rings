package com.almostrealism.metamerise.ui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * The Boundary Input Panel displays bounding boxes.
 */
public class BoundaryInputPanel extends JPanel {
	private int x, y;
	private int w, h;
	private double scale = 1.0;
	
	/**
	 * Constructs a new Boundary Input Panel.
	 */
	public BoundaryInputPanel() { }
	
	/**
	 * Paints the bounding box onto this panel.
	 */
	public void paint(Graphics g) {
		g.setColor(Color.blue);
		g.fillRect(0, 0, super.getWidth(), super.getHeight());
		
		int dw = (int) (w * scale);
		int dh = (int) (h * scale);
		
		g.setColor(Color.yellow);
		g.drawRect(x - dw / 2, y - dh / 2, dw, dh);
	}
	
	public void setBoundaryX(int x) { this.x = x; repaint(); }
	public void setBoundaryY(int y) { this.y = y; repaint(); }
	public void setBoundaryW(int w) { this.w = w; repaint(); }
	public void setBoundaryH(int h) { this.h = h; repaint(); }
	public void setBoundaryScale(double s) { this.scale = s; repaint(); }
	
	public int getBoundaryX() { return this.x; }
	public int getBoundaryY() { return this.y; }
	public int getBoundaryW() { return this.w; }
	public int getBoundaryH() { return this.h; }
	public double getBoundaryScale() { return this.scale; }
}
