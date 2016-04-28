package net.sf.j3d.math;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.j3d.util.graphics.GraphicsConverter;
import net.sf.j3d.util.graphics.RGB;


public class WaveSimulator {
	private static int sleep = 1000;
	
	public class DisplayPanel extends JPanel implements Runnable {
		public DisplayPanel() {
			super.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					Point p = e.getPoint();
					WaveSimulator.this.click(p.x, p.y);
				}
			});
		}
		
		public void paint(Graphics g) {
			RGB image[][] = WaveSimulator.this.getImage();
			
			g.drawImage(GraphicsConverter.convertToAWTImage(image),
						0, 0, image.length, image[0].length, this);
		}
		
		public void start() {
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(WaveSimulator.sleep);
						SwingUtilities.invokeAndWait(DisplayPanel.this);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}
			});
			
			t.start();
		}
		
		public void run() {
			WaveSimulator.this.iterate();
			this.invalidate();
		}
	}
	
	private int canvas[][][];
	private double cScale = 200.0;
	
	public WaveSimulator(int canvas[][][]) {
		this.canvas = canvas;
	}
	
	public synchronized void click(int x, int y) {
		this.canvas[x][y][0] += 20;
		System.out.println("Click: " + x + ", " + y + " = " + this.canvas[x][y][0]);
	}
	
	public synchronized void iterate() {
		int c[][][] = new int[this.canvas.length][0][0];
		
		for (int i = 0; i < c.length; i++) {
			c[i] = new int[this.canvas[i].length][0];
			
			for (int j = 0; j < c[i].length; j++) {
				c[i][j] = new int[this.canvas[i][j].length];
				System.arraycopy(this.canvas[i][j], 0, c[i][j], 0, this.canvas[i][j].length);
				if (c[i][j][0] > 0) c[i][j][0]--;
				
				for (int k = 1; k < c[i][j].length; k += 2) {
					int x = this.canvas[i][j][k];
					int y = this.canvas[i][j][k + 1];
					
					if (this.canvas[x][y][0] > 0) c[i][j][0]++;
				}
			}
		}
		
		this.canvas = c;
	}
	
	public synchronized RGB[][] getImage() {
		RGB image[][] = new RGB[this.canvas.length][this.canvas[0].length];
		
		for (int i = 0; i < this.canvas.length; i++) {
			for (int j = 0; j < this.canvas[i].length; j++) {
				double d = this.canvas[i][j][0] / this.cScale;
				image[i][j] = new RGB(d, d, d);
			}
		}
		
		return image;
	}
	
	public static WaveSimulator initRectCanvas(int w, int h) {
		int c[][][] = new int[w][h][9];
		
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (i <= 0)
					c[i][j][1] = w - 1;
				else
					c[i][j][1] = i - 1;
				
				c[i][j][2] = j;
				c[i][j][3] = i;
				
				if (j <= 0)
					c[i][j][4] = h - 1;
				else
					c[i][j][4] = j - 1;
				
				if (i >= w - 1)
					c[i][j][5] = 0;
				else
					c[i][j][5] = i + 1;
				
				
				c[i][j][6] = j;
				c[i][j][7] = i;
				
				if (j >= h - 1)
					c[i][j][8] = 0;
				else
					c[i][j][8] = j + 1;
			}
		}
		
		return new WaveSimulator(c);
	}
	
	public static void main(String args[]) {
		int w = 400;
		int h = 400;
		
		if (args.length > 0) w = Integer.parseInt(args[0]);
		if (args.length > 1) h = Integer.parseInt(args[1]);
		
		WaveSimulator sim = WaveSimulator.initRectCanvas(w, h);
		DisplayPanel panel = sim.new DisplayPanel();
		
		JFrame frame = new JFrame("Wave Simulator");
		frame.getContentPane().add(panel);
		frame.setSize(w, h);
		frame.setVisible(true);
		
		panel.start();
	}
}
