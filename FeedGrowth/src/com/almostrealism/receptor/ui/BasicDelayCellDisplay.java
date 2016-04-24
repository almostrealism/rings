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
	private int len = 100;
	
	public BasicDelayCellDisplay(BasicDelayCell c, int resolution) {
		cell = c;
		
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		timer = new Timer(c.getDelay() / resolution,  (e) -> update());
		
		setPreferredSize(new Dimension(w, h));
		
		beginDisplay();
	}
	
	public void beginDisplay() { timer.start(); }
	
	public void update() {
		Position p = cell.getPosition();
		
		Graphics g = image.getGraphics();
		g.setColor(Color.black);

		index = p.pos * 2 * Math.PI;
		g.drawLine(centerX, centerY,
				(int) (centerX + Math.cos(index) * len),
				(int) (centerY + Math.sin(index) * len));
		
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}
}
