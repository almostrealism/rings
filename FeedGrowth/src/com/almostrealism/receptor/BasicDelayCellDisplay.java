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

package com.almostrealism.receptor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.almostrealism.cells.delay.BasicDelayCell;
import org.almostrealism.cells.delay.BasicDelayCell.Position;

public class BasicDelayCellDisplay extends JPanel implements Updatable {
	private Timer timer;
	
	private BasicDelayCell cell;
	
	private BufferedImage image;
	
	private int centerX = 100;
	private int centerY = 100;
	
	private int w = 200;
	private int h = 200;
	
	private double index;
	private int len = 50;
	
	public BasicDelayCellDisplay(BasicDelayCell c) {
		cell = c;
		
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		timer = new Timer(5,  (e) -> repaint());
		
		setPreferredSize(new Dimension(w, h));
		
		beginDisplay();
	}
	
	public void beginDisplay() {
		timer.start();
	}
	
	public int getResolution() { return 100; }
	
	public synchronized void update() {
		Position p = cell.getPosition();
		
		index = p.pos * 2 * Math.PI;
		
		double x = Math.cos(index);
		double y = Math.sin(index);
		
		double value = p.value;
		
		int startX = (int) (centerX + x * len);
		int startY = (int) (centerY + y * len);
		int signalX = (int) (centerX + x * (len + value));
		int signalY = (int) (centerY + y * (len + value));
		int maxX = (int) (centerX + x * (2 * len));
		int maxY = (int) (centerY + y * (2 * len));
		
		Graphics g = image.getGraphics();
		
		g.setColor(getBackground());
		g.fillOval(centerX - len / 2, centerY - len / 2, len, len);
		
		g.setColor(Color.black);
		g.drawLine(startX, startY, signalX, signalY);

		g.setColor(getBackground());
		g.drawLine(signalX, signalY, maxX, maxY);
	}
	
	public synchronized void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}
}
