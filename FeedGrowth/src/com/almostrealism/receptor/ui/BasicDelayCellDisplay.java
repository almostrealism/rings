package com.almostrealism.receptor.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import com.almostrealism.feedgrow.delay.BasicDelayCell;
import com.almostrealism.feedgrow.delay.BasicDelayCell.Position;

public class BasicDelayCellDisplay extends JPanel {
	private Timer timer;
	
	private BasicDelayCell cell;
	
	private BufferedImage image;
	
	private int centerX = 100;
	private int centerY = 100;
	
	private int w = 200;
	private int h = 200;
	
	private double index;
	private int len = 50;
	
	public BasicDelayCellDisplay(BasicDelayCell c, int resolution) {
		cell = c;
		
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		timer = new Timer(50,  (e) -> update());
		
		setPreferredSize(new Dimension(w, h));
		
		beginDisplay();
	}
	
	public void beginDisplay() {
		timer.start();
		
		Thread t = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(50);
				} catch (Exception e) { }
				
				frame();
			}
		});
		t.start();
	}
	
	public void update() { repaint(); }
	
	public synchronized void frame() {
		Position p = cell.getPosition();
		
		index = p.pos * 2 * Math.PI;
		
		double x = Math.cos(index);
		double y = Math.sin(index);
		
		double value = p.value;
		
		int startX = (int) (centerX + x * len);
		int startY = (int) (centerY + y * len);
		int signalX = (int) (centerX + x * (len + value));
		int signalY = (int) (centerY + y * (len + value));
		
		Graphics g = image.getGraphics();
		
		g.setColor(getBackground());
		g.fillOval(0, 0, w, h);
		
		g.setColor(Color.black);
		g.drawLine(startX, startY, signalX, signalY);
	}
	
	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}
}
