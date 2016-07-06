package com.almostrealism.physics.test;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.almostrealism.io.FileEncoder;
import com.almostrealism.util.graphics.GraphicsConverter;
import com.almostrealism.util.graphics.RGB;

public class WaveGrid {
	private double max = 10.0, delta = 1.0;
	private double k = 0.05;
	private double a[][], v[][], x[][];
	private boolean available;
	private RGB image[][];
	
	private JPanel display;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WaveGrid g = new WaveGrid(500, 500);
		
		JFrame frame = new JFrame("Wave Test");
		frame.getContentPane().add(g.getDisplay());
		frame.setSize(500, 500);
		frame.setVisible(true);
		
		while (true) { g.tick(0.5); }
	}
	
	public WaveGrid(int w, int h) {
		this.a = new double[w][h];
		this.v = new double[w][h];
		this.x = new double[w][h];
		this.image = new RGB[w][h];
	}
	
	public void displace(int x, int y, double d) { this.x[x][y] += d; }
	
	public double neighbors(int x, int y) {
		return this.neighbors(x, y, false);
	}
	
	public double neighbors(int x, int y, boolean diag) {
		double tot = 0.0;
		
		if (x > 0) {
			tot += this.x[x - 1][y];
			if (diag && y > 0) tot += this.x[x - 1][y - 1];
			if (diag && y < this.x[x].length - 1) tot += this.x[x - 1][y + 1];
		} else {
			tot += this.x[this.x.length - 1][y];
			if (diag && y > 0) tot += this.x[this.x.length - 1][y - 1];
			if (diag && y < this.x[x].length - 1) tot += this.x[this.x.length - 1][y + 1];
		}
		
		if (x < this.x.length - 1) {
			tot += this.x[x + 1][y];
			if (diag && y > 0) tot += this.x[x + 1][y - 1];
			if (diag && y < this.x[x].length - 1) tot += this.x[x + 1][y + 1];
		} else {
			tot += this.x[0][y];
			if (diag && y > 0) tot += this.x[0][y - 1];
			if (diag && y < this.x[x].length - 1) tot += this.x[0][y + 1];
		}
		
		if (y > 0)
			tot += this.x[x][y - 1];
		else
			tot += this.x[x][this.x[x].length - 1];
		
		if (y < this.x[x].length - 1)
			tot += this.x[x][y + 1];
		else
			tot += this.x[x][0];
		
		if (diag)
			tot -= 8 * this.x[x][y];
		else
			tot -= 4 * this.x[x][y];
		
		return tot;
	}
	
	public void drawImage(Graphics g) {
		g.drawImage(GraphicsConverter.convertToAWTImage(
				this.getImage()), 0, 0, this.display);
	}
	
	public void writeImage(OutputStream out) throws IOException {
		FileEncoder.writeImage(this.getImage(), out, FileEncoder.PPMEncoding);
	}
	
	public void saveImage(String file) throws IOException {
		if (file.endsWith("ppm"))
			FileEncoder.encodeImageFile(this.getImage(), new File(file),
										FileEncoder.PPMEncoding);
		else
			FileEncoder.encodeImageFile(this.getImage(), new File(file),
										FileEncoder.JPEGEncoding);
	}
	
	public boolean imageAvailable() { return this.available; }
	
	public RGB[][] getImage() {
		if (!this.available) return new RGB[1][0];
		return this.image;
	}
	
	public JPanel getDisplay() {
		if (this.display != null) return this.display;
		
		this.display = new JPanel() {
			public void paint(Graphics g) {
				WaveGrid.this.drawImage(g);
			}
		};
		
		this.display.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent me) {
				int x = me.getX();
				int y = me.getY();
				if (x < 0 || x >= WaveGrid.this.x.length) return;
				if (y < 0 || y >= WaveGrid.this.x[x].length) return;
				
				WaveGrid.this.x[x][y] += WaveGrid.this.delta;
			}
			
			public void mousePressed(MouseEvent me) { }
			public void mouseReleased(MouseEvent me) { }
		});
		
		return this.display;
	}
	
	public void tick() { this.tick(1.0); }
	
	public void tick(double t) {
		for (int i = 0; i < this.x.length; i++) {
			for (int j = 0; j < this.x[i].length; j++) {
				this.x[i][j] += t * this.v[i][j];
			}
		}
		
		for (int i = 0; i < this.v.length; i++) {
			for (int j = 0; j < this.v[i].length; j++) {
				this.v[i][j] += t * this.a[i][j];
			}
		}
		
		double da[][] = new double[this.a.length][this.a[0].length];
		
		for (int i = 0; i < da.length; i++) {
			for (int j = 0; j < da[i].length; j++) {
				da[i][j] = -2.0 * this.x[i][j] + this.neighbors(i, j);
				da[i][j] *= this.k;
			}
		}
		
		this.a = da;
		
		for (int i = 0; i < this.image.length; i++) {
			for (int j = 0; j < this.image[i].length; j++) {
				double alpha = t * this.x[i][j] / this.max;
				this.image[i][j] = new RGB(alpha, alpha, alpha);
			}
		}
		
		this.available = true;
		
		if (this.display != null && this.display.getGraphics() != null) {
			Graphics g = this.display.getGraphics();
			this.drawImage(g);
		}
	}
}
