package com.almostrealism.physics.circles;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.MemoryImageSource;

import javax.swing.JFrame;

public class ParticleSetDisplay extends Component implements Runnable, MotionListener, MouseListener {
	private int pixels[];
	private ParticleSet particles;
	private CircleSet circles;
	
	private MemoryImageSource source;
	private Image image;
	private int w, h;
	private static int dot = 255 << 24 | 255 << 16 | 255 << 8 | 255;
	private static int blank = 255 << 24;
	static double scale = Math.pow(2.0, 1.0);
	
	private int fps = 20;
	private boolean stop;
	
	public ParticleSetDisplay(int w, int h, ParticleSet particles) {
		this.particles = particles;
		this.circles = new CircleSet();
		this.particles.addMotionListener(this);
		
		this.pixels = new int[w * h];
		this.w = w;
		this.h = h;
		
		for (int i = 0; i < this.pixels.length; i++) this.pixels[i] = this.blank;
		this.source = new MemoryImageSource(w, h, pixels, 0, w);
		this.source.setAnimated(true);
		this.image = super.createImage(this.source);
		
		super.addMouseListener(this);
		
		particles.start();
		new Thread(this).start();
	}
	
	public void setFPS(int fps) { this.fps = fps; }
	public int getFPS() { return this.fps; }
	public void stop() { this.stop = true; }
	
	public void run() {
		while (!stop) {
			try {
				Thread.sleep(1000 / this.fps);
			} catch (InterruptedException ie) { }
			
			this.source.newPixels();
			this.repaint();
		}
	}
	
	public void paint(Graphics g) {
		g.drawImage(this.image, 0, 0, this.w, this.h, this);
		this.circles.paint(g);
	}
	
	public Dimension getPreferredSize() { return new Dimension(this.w, this.h); }
	
	public void move(int x, int y, int nx, int ny) {
		x = (int) (x / scale);
		y = (int) (y / scale);
		nx = (int) (nx / scale);
		ny = (int) (ny / scale);
		
		if (x > 0 && x < this.w && y > 0 && y < this.h) this.pixels[y * this.w + x] = blank;
		if (nx > 0 && nx < this.w && ny > 0 && ny < this.h) this.pixels[ny * this.w + nx] = dot;
		this.circles.addCircle(nx, ny, 0);
	}

	public void mousePressed(MouseEvent e) {
		int pos[] = new int[3];
		pos[0] = (int) (e.getX() * scale);
		pos[1] = (int) (e.getY() * scale);
		this.particles.addParticle(pos);
	}
	
	public void mouseClicked(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	
	public static void main(String args[]) {
		ParticleSet set = new ParticleSet(100);
		
		JFrame pFrame = new JFrame("Particles");
		pFrame.getContentPane().add(new ParticleSetDisplay(500, 500, set));
		pFrame.setSize(500, 500);
		pFrame.setVisible(true);
		
		JFrame eFrame = new JFrame("Energy");
		eFrame.getContentPane().add(new PotentialDisplay(500, 500, set));
		eFrame.setSize(500, 500);
		eFrame.setLocation(500, 0);
		eFrame.setVisible(true);
	}
}
